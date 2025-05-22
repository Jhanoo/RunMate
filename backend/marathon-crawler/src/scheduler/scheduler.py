"""
스케줄러 모듈: 마라톤 크롤링 작업을 정해진 시간에 실행
"""
import os
import logging
from datetime import datetime
from zoneinfo import ZoneInfo
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
from dotenv import load_dotenv

from src.crawler.marathon_crawler import MarathonCrawler

# 로거 설정
logger = logging.getLogger(__name__)

# .env 파일 로드
load_dotenv()

# 환경 변수에서 스케줄링 시간 로드
CRAWL_HOUR = int(os.getenv('CRAWL_HOUR', '1'))     # 기본값 01:00
CRAWL_MINUTE = int(os.getenv('CRAWL_MINUTE', '0'))  # 기본값 01:00

# 타임존 (Asia/Seoul)
TIMEZONE = os.getenv('TIMEZONE', 'Asia/Seoul')

def create_scheduler():
    """스케줄러 생성 및 설정"""
    tz = ZoneInfo(TIMEZONE)
    scheduler = BackgroundScheduler(timezone=tz)
    
    # 마라톤 크롤링 작업 등록 (매일 지정된 시간에 실행)
    scheduler.add_job(
        func=crawl_marathons,
        trigger=CronTrigger(hour=CRAWL_HOUR, minute=CRAWL_MINUTE, timezone=tz),
        id='marathon_crawler',
        name='마라톤 정보 크롤링',
        replace_existing=True,
        coalesce=True,
        max_instances=1,
        misfire_grace_time=300
    )
    
    return scheduler


def crawl_marathons():
    """마라톤 크롤링 실행 함수"""
    tz = ZoneInfo(TIMEZONE)
    now = datetime.now(tz)
    logger.info(f"마라톤 크롤링 작업 시작: {now.strftime('%Y-%m-%d %H:%M:%S')} ({TIMEZONE})")
    
    try:
        with MarathonCrawler() as crawler:
            crawler.crawl()
        logger.info("마라톤 크롤링 작업 성공적으로 완료")
    except Exception as e:
        logger.error(f"마라톤 크롤링 작업 실패: {e}", exc_info=True)


def start_scheduler():
    """스케줄러 시작"""
    scheduler = create_scheduler()
    
    try:
        scheduler.start()
        logger.info(
            f"스케줄러 시작 - 매일 {CRAWL_HOUR:02d}:{CRAWL_MINUTE:02d} "
            f"({TIMEZONE})에 크롤링 실행"
        )
        return scheduler
    except Exception as e:
        logger.error(f"스케줄러 시작 실패: {e}", exc_info=True)
        return None