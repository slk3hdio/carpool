import requests
import os
import re
from dataclasses import dataclass, field
from typing import Dict


url = 'http://localhost:8080/api/log/level-count'
response = requests.get(url)


@dataclass
class LogLevelStats:
    """日志等级统计"""
    info: int = 0
    warn: int = 0
    error: int = 0
    debug: int = 0
    trace: int = 0
    
    def to_dict(self) -> Dict[str, int]:
        return {
            'INFO': self.info,
            'WARN': self.warn,
            'ERROR': self.error,
            'DEBUG': self.debug,
            'TRACE': self.trace
        }
    
    def __str__(self) -> str:
        lines = ['【日志等级统计Map】']
        lines.append(f'INFO: {self.info}')
        lines.append(f'WARN: {self.warn}')
        lines.append(f'ERROR: {self.error}')
        lines.append(f'DEBUG: {self.debug}')
        lines.append(f'TRACE: {self.trace}')
        return '\n'.join(lines)


@dataclass
class StartupMetrics:
    """启动耗时统计"""
    total_time: float = 0.0
    root_context_init_time: int = 0
    
    def __str__(self) -> str:
        lines = ['【启动耗时与关键阶段耗时】']
        lines.append(f'服务启动总耗时: {self.total_time} 秒')
        lines.append(f'Root WebApplicationContext initialization: {self.root_context_init_time} ms')
        return '\n'.join(lines)


@dataclass
class DatabasePoolStats:
    """数据库连接池统计"""
    connection_created: int = 0
    connection_closed: int = 0
    pool_started: int = 0
    pool_closed: int = 0
    connection_errors: int = 0
    
    def __str__(self) -> str:
        lines = ['【数据库连接池（HikariCP）状态统计】']
        lines.append(f'连接创建次数: {self.connection_created}')
        lines.append(f'连接关闭次数: {self.connection_closed}')
        lines.append(f'连接池启动次数: {self.pool_started}')
        lines.append(f'连接池关闭次数: {self.pool_closed}')
        lines.append(f'连接相关异常/错误: {self.connection_errors}')
        return '\n'.join(lines)


@dataclass
class QpsMetrics:
    """QPS和数据流量统计"""
    info: float = 0.0
    warn: float = 0.0
    error: float = 0.0
    debug: float = 0.0
    trace: float = 0.0
    data_throughput: float = 0.0
    
    def __str__(self) -> str:
        lines = ['【QPS (每个Source)】']
        lines.append(f'INFO: {self.info:.2f}')
        lines.append(f'WARN: {self.warn:.2f}')
        lines.append(f'ERROR: {self.error:.2f}')
        lines.append(f'DEBUG: {self.debug:.2f}')
        lines.append(f'TRACE: {self.trace:.2f}')
        lines.append(f'数据流量（B/s）：{self.data_throughput:.2f}')
        return '\n'.join(lines)


@dataclass
class PackageLogStats:
    """包名日志数量统计"""
    packages: Dict[str, int] = field(default_factory=dict)
    
    def __str__(self) -> str:
        lines = ['【各包名日志数量】']
        for package_name, count in self.packages.items():
            lines.append(f'{package_name}: {count}')
        return '\n'.join(lines)


@dataclass
class ExceptionStats:
    """异常类型统计"""
    exception_counts: Dict[str, int] = field(default_factory=dict)
    stack_traces: Dict[str, str] = field(default_factory=dict)
    
    @property
    def total_exceptions(self) -> int:
        return sum(self.exception_counts.values())
    
    def __str__(self) -> str:
        lines = ['【异常类型统计】']
        for exception_type, count in self.exception_counts.items():
            lines.append(f'{exception_type}: {count}')
        
        if self.stack_traces:
            lines.append('\n【异常堆栈摘要（每种类型首例）】')
            for exception_type, trace in self.stack_traces.items():
                lines.append(f'{exception_type}:')
                lines.append(trace)
        
        return '\n'.join(lines)


@dataclass
class LogStatistics:
    """日志统计总类"""
    log_levels: LogLevelStats = field(default_factory=LogLevelStats)
    startup: StartupMetrics = field(default_factory=StartupMetrics)
    database_pool: DatabasePoolStats = field(default_factory=DatabasePoolStats)
    qps: QpsMetrics = field(default_factory=QpsMetrics)
    packages: PackageLogStats = field(default_factory=PackageLogStats)
    exceptions: ExceptionStats = field(default_factory=ExceptionStats)
    
    def __str__(self) -> str:
        parts = [
            str(self.log_levels),
            str(self.startup),
            str(self.database_pool),
            str(self.qps),
            str(self.packages),
            str(self.exceptions)
        ]
        return '\n\n'.join(parts)


# 日志级别统计
def get_log_level_count():
    log_path = os.path.join(os.path.dirname(__file__), 'carpool-b', 'log_level_count.txt')
    
    with open(log_path, encoding='utf-8') as f:
        content = f.read()
    
    # 创建统计对象
    stats = LogStatistics()
    
    # === 1. 日志等级统计Map ===
    log_level_section = re.search(r'--- 日志等级统计Map ---\s*\n(.*?)(?=\n---|\Z)', content, re.DOTALL)
    if log_level_section:
        section_text = log_level_section.group(1)
        info_match = re.search(r'INFO:\s*(\d+)', section_text)
        warn_match = re.search(r'WARN:\s*(\d+)', section_text)
        error_match = re.search(r'ERROR:\s*(\d+)', section_text)
        debug_match = re.search(r'DEBUG:\s*(\d+)', section_text)
        trace_match = re.search(r'TRACE:\s*(\d+)', section_text)
        
        if info_match:
            stats.log_levels.info = int(info_match.group(1))
        if warn_match:
            stats.log_levels.warn = int(warn_match.group(1))
        if error_match:
            stats.log_levels.error = int(error_match.group(1))
        if debug_match:
            stats.log_levels.debug = int(debug_match.group(1))
        if trace_match:
            stats.log_levels.trace = int(trace_match.group(1))
    
    # === 2. 启动耗时与关键阶段耗时 ===
    startup_section = re.search(r'--- 启动耗时与关键阶段耗时 ---\s*\n(.*?)(?=\n---|\Z)', content, re.DOTALL)
    if startup_section:
        section_text = startup_section.group(1)
        total_match = re.search(r'服务启动总耗时:\s*([\d.]+)\s*秒', section_text)
        root_match = re.search(r'Root WebApplicationContext initialization:\s*(\d+)\s*ms', section_text)
        
        if total_match:
            stats.startup.total_time = float(total_match.group(1))
        if root_match:
            stats.startup.root_context_init_time = int(root_match.group(1))
    
    # === 3. 数据库连接池（HikariCP）状态统计 ===
    db_section = re.search(r'--- 数据库连接池（HikariCP）状态统计 ---\s*\n(.*?)(?=\n---|\Z)', content, re.DOTALL)
    if db_section:
        section_text = db_section.group(1)
        created_match = re.search(r'连接创建次数:\s*(\d+)', section_text)
        closed_match = re.search(r'连接关闭次数:\s*(\d+)', section_text)
        started_match = re.search(r'连接池启动次数:\s*(\d+)', section_text)
        pool_closed_match = re.search(r'连接池关闭次数:\s*(\d+)', section_text)
        errors_match = re.search(r'连接相关异常/错误:\s*(\d+)', section_text)
        
        if created_match:
            stats.database_pool.connection_created = int(created_match.group(1))
        if closed_match:
            stats.database_pool.connection_closed = int(closed_match.group(1))
        if started_match:
            stats.database_pool.pool_started = int(started_match.group(1))
        if pool_closed_match:
            stats.database_pool.pool_closed = int(pool_closed_match.group(1))
        if errors_match:
            stats.database_pool.connection_errors = int(errors_match.group(1))
    
    # === 4. QPS (每个Source) ===
    qps_section = re.search(r'--- QPS \(每个Source\) ---\s*\n(.*?)(?=\n---|\Z)', content, re.DOTALL)
    if qps_section:
        section_text = qps_section.group(1)
        qps_info_match = re.search(r'INFO:\s*([\d.]+)', section_text)
        qps_warn_match = re.search(r'WARN:\s*([\d.]+)', section_text)
        qps_error_match = re.search(r'ERROR:\s*([\d.]+)', section_text)
        qps_debug_match = re.search(r'DEBUG:\s*([\d.]+)', section_text)
        qps_trace_match = re.search(r'TRACE:\s*([\d.]+)', section_text)
        throughput_match = re.search(r'数据流量（B/s）[：:]\s*([\d.]+)', section_text)
        
        if qps_info_match:
            stats.qps.info = float(qps_info_match.group(1))
        if qps_warn_match:
            stats.qps.warn = float(qps_warn_match.group(1))
        if qps_error_match:
            stats.qps.error = float(qps_error_match.group(1))
        if qps_debug_match:
            stats.qps.debug = float(qps_debug_match.group(1))
        if qps_trace_match:
            stats.qps.trace = float(qps_trace_match.group(1))
        if throughput_match:
            stats.qps.data_throughput = float(throughput_match.group(1))
    
    # === 5. 各包名日志数量 ===
    package_section = re.search(r'--- 各包名日志数量 ---\s*\n(.*?)(?=\n---|\Z)', content, re.DOTALL)
    if package_section:
        section_text = package_section.group(1)
        for line in section_text.strip().split('\n'):
            match = re.match(r'(.+?):\s*(\d+)', line.strip())
            if match:
                package_name = match.group(1)
                count = int(match.group(2))
                stats.packages.packages[package_name] = count
    
    # === 6. 异常类型统计 ===
    exception_section = re.search(r'--- 异常类型统计 ---\s*\n(.*?)(?=\n---|\Z)', content, re.DOTALL)
    if exception_section:
        section_text = exception_section.group(1)
        for line in section_text.strip().split('\n'):
            match = re.match(r'(.+?):\s*(\d+)', line.strip())
            if match:
                exception_type = match.group(1)
                count = int(match.group(2))
                stats.exceptions.exception_counts[exception_type] = count
    
    # === 7. 异常堆栈摘要 ===
    stack_section = re.search(r'--- 异常堆栈摘要（每种类型首例） ---\s*\n(.*)', content, re.DOTALL)
    if stack_section:
        section_text = stack_section.group(1).strip()
        # 按异常类型分割
        current_exception = None
        current_trace = []
        
        for line in section_text.split('\n'):
            # 检查是否是新的异常类型（不包含空格开头的行）
            if line and not line.startswith(' ') and ':' in line and not line.startswith('20'):
                if current_exception and current_trace:
                    stats.exceptions.stack_traces[current_exception] = '\n'.join(current_trace).strip()
                exception_parts = line.split(':', 1)
                current_exception = exception_parts[0].strip()
                current_trace = [line]
            elif current_exception:
                current_trace.append(line)
        
        # 保存最后一个异常
        if current_exception and current_trace:
            stats.exceptions.stack_traces[current_exception] = '\n'.join(current_trace).strip()
    
    # 返回统计对象的字符串表示
    return str(stats)


if __name__ == '__main__':
    print(get_log_level_count())