import requests
import json
import pymysql
from datetime import datetime
import time
import logging
import os
import sys

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('/app/logs/collector.log', encoding='utf-8')
    ]
)

class RoadTrafficAPI:
    def __init__(self, config_path='config.json'):
        """
        初始化API客户端，从配置文件读取配置

        Args:
            config_path: 配置文件路径
        """
        self.config = self.load_config(config_path)
        self.db_config = self.config['database']
        self.ak = self.config['baidu_api']['ak']
        self.base_url = "https://api.map.baidu.com/traffic/v1/road"

        # 创建日志目录
        os.makedirs('/app/logs', exist_ok=True)

        logging.info("数据采集服务初始化完成")

    def load_config(self, config_path):
        """从文件加载配置"""
        try:
            with open(config_path, 'r', encoding='utf-8') as f:
                config = json.load(f)
            logging.info(f"配置文件加载成功: {config_path}")
            return config
        except Exception as e:
            logging.error(f"配置文件加载失败: {e}")
            sys.exit(1)

    def get_connection(self):
        """获取数据库连接"""
        max_retries = 3
        for attempt in range(max_retries):
            try:
                connection = pymysql.connect(
                    host=self.db_config['host'],
                    user=self.db_config['user'],
                    password=self.db_config['password'],
                    database=self.db_config['database'],
                    charset=self.db_config.get('charset', 'utf8mb4'),
                    cursorclass=pymysql.cursors.DictCursor
                )
                return connection
            except Exception as e:
                logging.error(f"数据库连接失败 (尝试 {attempt + 1}/{max_retries}): {e}")
                if attempt < max_retries - 1:
                    time.sleep(5)
                else:
                    return None

    def request_traffic_data(self, road_name, city):
        """
        请求路况数据

        Args:
            road_name: 道路名称
            city: 城市名称

        Returns:
            API响应数据
        """
        params = {
            'road_name': road_name,
            'city': city,
            'ak': self.ak
        }

        try:
            response = requests.get(self.base_url, params=params, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            logging.error(f"API请求失败 [{city} - {road_name}]: {e}")
            return None

    def save_to_database(self, road_name, city, api_data):
        """
        保存数据到数据库

        Args:
            road_name: 道路名称
            city: 城市名称
            api_data: API返回数据
        """
        if not api_data:
            logging.error("API数据为空，无法保存")
            return False

        connection = self.get_connection()
        if not connection:
            return False

        try:
            with connection.cursor() as cursor:
                # 1. 插入整体路况信息
                overall_sql = """
                INSERT INTO road_traffic_overall
                (road_name, city, api_status, message, description, evaluation_status, evaluation_status_desc)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
                """

                evaluation_status = api_data.get('evaluation', {}).get('status')
                evaluation_status_desc = api_data.get('evaluation', {}).get('status_desc')

                cursor.execute(overall_sql, (
                    road_name,
                    city,
                    api_data.get('status'),
                    api_data.get('message'),
                    api_data.get('description'),
                    evaluation_status,
                    evaluation_status_desc
                ))

                overall_id = cursor.lastrowid

                # 2. 插入拥堵路段详细信息
                road_traffic = api_data.get('road_traffic', [])
                sections_count = 0

                for road in road_traffic:
                    congestion_sections = road.get('congestion_sections', [])

                    for section in congestion_sections:
                        section_sql = """
                        INSERT INTO congestion_sections
                        (overall_id, road_name, section_desc, status, speed, congestion_distance, congestion_trend)
                        VALUES (%s, %s, %s, %s, %s, %s, %s)
                        """

                        cursor.execute(section_sql, (
                            overall_id,
                            road.get('road_name'),
                            section.get('section_desc'),
                            section.get('status'),
                            section.get('speed'),
                            section.get('congestion_distance'),
                            section.get('congestion_trend')
                        ))
                        sections_count += 1

                connection.commit()
                logging.info(f"数据保存成功 [{city} - {road_name}] - 整体ID: {overall_id}, 路段数: {sections_count}")
                return True

        except Exception as e:
            logging.error(f"数据保存失败 [{city} - {road_name}]: {e}")
            connection.rollback()
            return False
        finally:
            connection.close()

    def get_and_save_traffic(self, road_name, city):
        """
        获取并保存路况数据

        Args:
            road_name: 道路名称
            city: 城市名称

        Returns:
            bool: 是否成功
        """
        # 请求API数据
        api_data = self.request_traffic_data(road_name, city)

        if not api_data:
            return False

        # 检查API响应状态
        if api_data.get('status') != 0:
            logging.error(f"API返回错误 [{city} - {road_name}]: {api_data.get('message')}")
            return False

        # 保存到数据库
        return self.save_to_database(road_name, city, api_data)

    def run(self):
        """运行数据采集服务"""
        roads_to_monitor = self.config['roads']
        collection_interval = self.config['schedule']['collection_interval_seconds']
        request_interval = self.config['schedule']['request_interval_seconds']

        logging.info(f"开始监控 {len(roads_to_monitor)} 条道路")
        logging.info(f"采集间隔: {collection_interval}秒, 请求间隔: {request_interval}秒")

        # 定期获取路况数据
        while True:
            logging.info("=" * 50)
            logging.info("开始新一轮数据采集...")
            start_time = time.time()

            success_count = 0
            fail_count = 0

            for idx, road_info in enumerate(roads_to_monitor, 1):
                road_name = road_info['road_name']
                city = road_info['city']

                logging.info(f"[{idx}/{len(roads_to_monitor)}] 获取 {city} - {road_name} 路况数据...")

                success = self.get_and_save_traffic(road_name, city)

                if success:
                    success_count += 1
                else:
                    fail_count += 1

                # 短暂间隔，避免请求过于频繁
                if idx < len(roads_to_monitor):
                    time.sleep(request_interval)

            elapsed_time = time.time() - start_time
            logging.info("=" * 50)
            logging.info(f"本轮采集完成 - 成功: {success_count}, 失败: {fail_count}, 耗时: {elapsed_time:.2f}秒")
            logging.info(f"等待 {collection_interval} 秒后继续...")

            time.sleep(collection_interval)


# 使用示例
if __name__ == "__main__":
    try:
        # 从环境变量读取配置文件路径（Docker 环境）
        config_path = os.getenv('CONFIG_PATH', 'config.json')

        # 创建API客户端
        traffic_client = RoadTrafficAPI(config_path)

        # 运行采集服务
        traffic_client.run()

    except KeyboardInterrupt:
        logging.info("收到中断信号，服务停止")
    except Exception as e:
        logging.error(f"服务异常退出: {e}", exc_info=True)
        sys.exit(1)
