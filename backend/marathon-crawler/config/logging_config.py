"""
로깅 설정 모듈
"""
import os
import logging
import codecs
import sys
from logging.handlers import RotatingFileHandler

def setup_logging(log_dir='logs', log_level=logging.INFO):
    """로깅 설정
    
    Args:
        log_dir: 로그 파일이 저장될 디렉토리
        log_level: 로깅 레벨
    """
    # 로그 디렉토리가 없으면 생성
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
    
    # Windows 환경에서 출력 스트림 인코딩 설정
    if sys.stdout.encoding != 'utf-8':
        sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')
        sys.stderr = codecs.getwriter('utf-8')(sys.stderr.buffer, 'strict')
    
    # 로거 설정
    logger = logging.getLogger()
    logger.setLevel(log_level)
    
    # 기존 핸들러 제거
    for handler in logger.handlers[:]:
        logger.removeHandler(handler)
    
    # 포맷터 설정
    formatter = logging.Formatter(
        '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # 파일 핸들러 (로테이팅 로그 파일)
    file_handler = RotatingFileHandler(
        os.path.join(log_dir, 'marathon_crawler.log'),
        maxBytes=10 * 1024 * 1024,  # 10MB
        backupCount=5,
        encoding='utf-8'  # UTF-8 인코딩 사용
    )
    file_handler.setFormatter(formatter)
    
    # 콘솔 핸들러
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)
    
    # 핸들러 추가
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)
    
    return logger