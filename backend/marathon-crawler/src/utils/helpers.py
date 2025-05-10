"""
유틸리티 함수 모듈
"""
import os
import random
import time
import logging
import re
from functools import wraps

def get_env_int(key, default=0):
    """환경 변수에서 정수값 가져오기"""
    value = os.getenv(key)
    if value is None:
        return default
    try:
        return int(value)
    except ValueError:
        return default

def get_env_float(key, default=0.0):
    """환경 변수에서 실수값 가져오기"""
    value = os.getenv(key)
    if value is None:
        return default
    try:
        return float(value)
    except ValueError:
        return default

def clean_text(text):
    """특수 문자 및 공백 정리"""
    if text is None:
        return ""
    # 비분리 공백(\xa0)을 일반 공백으로 변환
    cleaned = text.replace('\xa0', ' ')
    # 연속된 공백을 하나로 줄임
    cleaned = re.sub(r'\s+', ' ', cleaned)
    # 앞뒤 공백 제거
    return cleaned.strip()

def random_delay(min_seconds=1.0, max_seconds=3.0):
    """랜덤 지연 시간을 갖는 데코레이터
    
    Args:
        min_seconds: 최소 지연 시간 (초)
        max_seconds: 최대 지연 시간 (초)
    """
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            # 함수 실행 전 랜덤 지연
            delay = random.uniform(min_seconds, max_seconds)
            logging.debug(f"대기 중... {delay:.2f}초")
            time.sleep(delay)
            
            # 함수 실행
            return func(*args, **kwargs)
        return wrapper
    return decorator

def retry(max_attempts=3, delay=1.0, backoff=2.0, exceptions=(Exception,)):
    """예외 발생 시 재시도 데코레이터
    
    Args:
        max_attempts: 최대 시도 횟수
        delay: 초기 지연 시간 (초)
        backoff: 지연 시간 증가 계수
        exceptions: 재시도할 예외 타입 (튜플)
    """
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            attempt = 0
            current_delay = delay
            
            while attempt < max_attempts:
                try:
                    return func(*args, **kwargs)
                except exceptions as e:
                    attempt += 1
                    if attempt >= max_attempts:
                        logging.error(f"최대 시도 횟수 초과: {max_attempts}회, 마지막 오류: {str(e)}")
                        raise
                    
                    logging.warning(f"재시도 중... {attempt}/{max_attempts}, 오류: {str(e)}")
                    time.sleep(current_delay)
                    current_delay *= backoff  # 지연 시간 증가
            
            return None  # 여기에 도달하지 않지만, 코드 완성성을 위해 포함
        return wrapper
    return decorator

def chunks(lst, n):
    """리스트를 n 크기의 청크로 분할"""
    for i in range(0, len(lst), n):
        yield lst[i:i + n]