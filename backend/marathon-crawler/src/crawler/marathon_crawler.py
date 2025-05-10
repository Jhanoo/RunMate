"""
마라톤 웹사이트 크롤러 메인 모듈
"""
import os
import logging
import time
import random
from dotenv import load_dotenv

from src.crawler.parser import (
    get_marathon_list,
    get_marathon_detail, 
    parse_marathon_detail
)
from src.database.database import get_db_session, close_db_session
from src.database.models import Marathon, MarathonDistance
from src.utils.helpers import get_env_int, get_env_float, random_delay, retry

# .env 파일 로드
load_dotenv()

# 환경 변수에서 설정 로드
PAGES_TO_CRAWL = get_env_int('PAGES_TO_CRAWL', 5)
CRAWL_DELAY_MIN = get_env_float('CRAWL_DELAY_MIN', 1.0)
CRAWL_DELAY_MAX = get_env_float('CRAWL_DELAY_MAX', 3.0)

class MarathonCrawler:
    """마라톤 웹사이트 크롤러 클래스"""
    
    def __init__(self):
        """크롤러 초기화"""
        self.session = get_db_session()
        self.total_marathons = 0
        self.logger = logging.getLogger(__name__)
    
    def __enter__(self):
        """컨텍스트 매니저 진입"""
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        """컨텍스트 매니저 종료 시 리소스 정리"""
        close_db_session(self.session)
    
    @retry(max_attempts=3, exceptions=(Exception,))
    def crawl_marathon_list(self, page=1):
        """마라톤 목록 페이지 크롤링"""
        self.logger.info(f"페이지 {page} 크롤링 시작")
        
        # 마라톤 목록 가져오기 (get_marathon_list 함수는 이미 파싱된 목록을 반환)
        marathons = get_marathon_list(page)
        
        if not marathons:
            self.logger.warning(f"페이지 {page}에서 마라톤 정보를 찾을 수 없습니다.")
            return 0
            
        self.logger.info(f"페이지 {page}에서 {len(marathons)}개의 마라톤 정보 추출")
        
        # 각 마라톤 상세 정보 크롤링
        for marathon in marathons:
            try:
                self._process_marathon_detail(marathon)
                time.sleep(random.uniform(CRAWL_DELAY_MIN, CRAWL_DELAY_MAX))
            except Exception as e:
                self.logger.error(f"마라톤 상세 처리 실패: {str(e)}")
        
        return len(marathons)
    
    @random_delay(min_seconds=CRAWL_DELAY_MIN, max_seconds=CRAWL_DELAY_MAX)
    def _process_marathon_detail(self, marathon):
        """마라톤 상세 정보 처리"""
        if 'url' not in marathon:
            self.logger.warning(f"마라톤 URL 정보가 없습니다: {marathon.get('name', '이름 없음')}")
            return
        
        # 이미 거리 정보가 있는 경우 상세 페이지 요청 건너뛰기
        if 'distances' in marathon and marathon['distances']:
            self.logger.info(f"마라톤 '{marathon.get('name', '이름 없음')}'의 거리 정보가 이미 있으므로 상세 페이지 요청을 건너뜁니다.")
        else:
            # 상세 페이지 크롤링
            detail_html = get_marathon_detail(marathon['url'])
            
            # 상세 정보 파싱
            details = parse_marathon_detail(detail_html)
            
            # 마라톤 정보와 상세 정보 병합
            marathon.update(details)
        
        # 거리 정보가 없는 경우 기본값 추가
        if 'distances' not in marathon or not marathon['distances']:
            # 대회명에서 거리 정보 추론
            if '풀' in marathon.get('name', '') or '42' in marathon.get('name', ''):
                self.logger.info(f"대회명에서 풀코스 정보 추출: {marathon.get('name', '이름 없음')}")
                marathon['distances'] = ['풀코스']
            elif '하프' in marathon.get('name', '') or 'half' in marathon.get('name', '').lower():
                self.logger.info(f"대회명에서 하프코스 정보 추출: {marathon.get('name', '이름 없음')}")
                marathon['distances'] = ['하프']
            else:
                self.logger.warning(f"마라톤 '{marathon.get('name', '이름 없음')}'의 거리 정보가 없어 기본값을 사용합니다.")
                marathon['distances'] = ['풀코스', '하프', '10km', '5km']
        
        # 데이터베이스에 저장
        self._save_marathon(marathon)
    
    def _save_marathon(self, marathon_data):
        """마라톤 정보를 데이터베이스에 저장"""
        try:
            # 필수 필드 확인
            if not all(key in marathon_data and marathon_data[key] for key in ['name', 'date', 'location']):
                self.logger.warning(f"필수 필드 누락: {marathon_data.get('name', '이름 없음')}")
                return False
                
            # 이미 존재하는 마라톤인지 확인 (이름과 날짜 기준)
            existing_marathon = self.session.query(Marathon).filter_by(
                name=marathon_data['name'], 
                date=marathon_data['date']
            ).first()
            
            if not existing_marathon:
                # 새 마라톤 추가
                new_marathon = Marathon(
                    name=marathon_data['name'],
                    date=marathon_data['date'],
                    location=marathon_data['location']
                )
                self.session.add(new_marathon)
                self.session.flush()  # ID 생성을 위해 flush
                
                # 거리 정보 추가
                if 'distances' in marathon_data and marathon_data['distances']:
                    for distance_value in marathon_data['distances']:
                        distance = MarathonDistance(
                            marathon_id=new_marathon.marathon_id,
                            distance=distance_value
                        )
                        self.session.add(distance)
                
                self.logger.info(f"DB에 새 마라톤 추가: {marathon_data['name']}")
                self.total_marathons += 1
            else:
                self.logger.info(f"이미 존재하는 마라톤: {marathon_data['name']}")
                
                # 기존 마라톤의 거리 정보 업데이트 (필요시)
                if 'distances' in marathon_data and marathon_data['distances']:
                    # 기존 거리 정보 조회
                    existing_distances = [d.distance for d in existing_marathon.distances]
                    
                    # 새로운 거리 정보 추가
                    for distance_value in marathon_data['distances']:
                        if distance_value not in existing_distances:
                            distance = MarathonDistance(
                                marathon_id=existing_marathon.marathon_id,
                                distance=distance_value
                            )
                            self.session.add(distance)
                            self.logger.info(f"기존 마라톤 '{marathon_data['name']}'에 새 거리 '{distance_value}' 추가")
            
            self.session.commit()
            return True
        
        except Exception as e:
            self.session.rollback()
            self.logger.error(f"DB 저장 실패: {str(e)}")
            return False
    
    def crawl(self):
        """전체 크롤링 수행"""
        try:
            self.logger.info("마라톤 크롤링 시작")
            
            # 단일 페이지만 처리 (서브페이스 앱의 경우 페이지네이션이 복잡할 수 있음)
            num_marathons = self.crawl_marathon_list()
            self.logger.info(f"크롤링 완료: {num_marathons}개 마라톤")
            
            self.logger.info(f"크롤링 완료. 총 {self.total_marathons}개의 새 마라톤이 DB에 추가되었습니다.")
            
        except Exception as e:
            self.logger.error(f"크롤링 중 오류 발생: {str(e)}")
            raise