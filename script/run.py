import requests
import json
import pymysql
from datetime import datetime
import time
import logging

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

class RoadTrafficAPI:
    def __init__(self, ak, db_config):
        """
        初始化API客户端
        
        Args:
            ak: 百度API密钥
            db_config: 数据库配置字典
        """
        self.ak = ak
        self.base_url = "https://api.map.baidu.com/traffic/v1/road"
        self.db_config = db_config
        
    def get_connection(self):
        """获取数据库连接"""
        try:
            connection = pymysql.connect(
                host=self.db_config['host'],
                user=self.db_config['user'],
                password=self.db_config['password'],
                database=self.db_config['database'],
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor
            )
            return connection
        except Exception as e:
            logging.error(f"数据库连接失败: {e}")
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
            logging.error(f"API请求失败: {e}")
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

        print(f"api_data: {api_data}")
            
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
                
                connection.commit()
                logging.info(f"数据保存成功，整体ID: {overall_id}")
                return True
                
        except Exception as e:
            logging.error(f"数据保存失败: {e}")
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
            logging.error(f"API返回错误: {api_data.get('message')}")
            return False
            
        # 保存到数据库
        return self.save_to_database(road_name, city, api_data)

# 使用示例
if __name__ == "__main__":
    # 数据库配置
    db_config = {
        'host': 'localhost',
        'user': 'root',
        'password': 'Lsp20041225',
        'database': 'carpool'
    }
    
    # 百度API密钥
    BAIDU_AK = "MU5oUiBPhetOfK4EO62VrIgxtM3gw4EI"
    
    # 创建API客户端
    traffic_client = RoadTrafficAPI(BAIDU_AK, db_config)
    
    # 要监控的道路列表
    roads_to_monitor = [
        {"road_name": "四平路", "city": "上海市"},
        {"road_name": "中山北二路", "city": "上海市"},
        {"road_name": "国权路", "city": "上海市"},
        {"road_name": "彰武路", "city": "上海市"},
        {"road_name": "汶水东路", "city": "上海市"},
        {"road_name": "海伦路", "city": "上海市"},
        {"road_name": "淞沪路", "city": "上海市"},
        {"road_name": "中环路", "city": "上海市"},
        {"road_name": "赤峰路", "city": "上海市"},
        {"road_name": "浙江中路", "city": "上海市"},
        {"road_name": "昌吉东路", "city": "上海市"},
        {"road_name": "嘉松北路", "city": "上海市"},
        # 添加更多道路...
    ]
    
    # 定期获取路况数据（示例：每5分钟一次）
    while True:
        for road_info in roads_to_monitor:
            logging.info(f"获取 {road_info['city']} - {road_info['road_name']} 路况数据...")
            success = traffic_client.get_and_save_traffic(
                road_info['road_name'], 
                road_info['city']
            )
            
            if success:
                logging.info(f"{road_info['road_name']} 数据获取并保存成功")
            else:
                logging.error(f"{road_info['road_name']} 数据获取失败")
            
            # 短暂间隔，避免请求过于频繁
            time.sleep(2)
        
        logging.info("一轮数据获取完成，等待5分钟后继续...")
        time.sleep(300)  # 5分钟