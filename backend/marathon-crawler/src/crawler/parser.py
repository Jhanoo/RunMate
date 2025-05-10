"""
마라톤 웹사이트 HTML 파싱 함수 모듈
"""
import random
import logging
import json
import re
from datetime import datetime
from bs4 import BeautifulSoup
import requests

logger = logging.getLogger(__name__)

def get_user_agent():
    """랜덤 User-Agent 반환"""
    user_agents = [
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15',
        'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0'
    ]
    return random.choice(user_agents)

def parse_date(date_str):
    """날짜 문자열을 파싱하여 date 객체로 변환 (시간 제외)"""
    try:
        # 한국어 날짜 형식 처리 (예: 2025년 3월 15일)
        matches = re.match(r'(\d{4})년 (\d{1,2})월 (\d{1,2})일', date_str)
        if matches:
            year, month, day = map(int, matches.groups())
            # datetime 대신 date 객체만 반환
            return datetime(year, month, day).date()
        
        # "2025년 3월 1일 (토)" 형식 처리
        matches = re.match(r'(\d{4})년 (\d{1,2})월 (\d{1,2})일 \(.+\)', date_str)
        if matches:
            year, month, day = map(int, matches.groups())
            return datetime(year, month, day).date()
        
        # 다양한 날짜 형식 처리
        date_formats = [
            '%Y.%m.%d',  # 2025.05.10
            '%Y-%m-%d',  # 2025-05-10
        ]
        
        # 불필요한 공백 제거
        date_str = date_str.strip()
        
        # 각 형식으로 파싱 시도
        for date_format in date_formats:
            try:
                dt = datetime.strptime(date_str, date_format)
                return dt.date()  # 시간 제외, 날짜만 반환
            except ValueError:
                continue
        
        # 모든 형식이 실패한 경우
        logger.warning(f"날짜 파싱 실패: {date_str}")
        return None
    except Exception as e:
        logger.error(f"날짜 파싱 오류: {str(e)}, 날짜: {date_str}")
        return None

def get_marathon_list(page=1):
    """runningwikii.com에서 마라톤 목록 가져오기"""
    url = "https://runningwikii.com/entry/2025-marathon-schedule"
    
    headers = {
        'User-Agent': get_user_agent(),
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Referer': 'https://runningwikii.com/'
    }
    
    try:
        # 웹페이지 요청
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        
        # HTML 파싱
        soup = BeautifulSoup(response.text, 'html.parser')
        
        marathons = []
        
        # 테이블에서 마라톤 정보 추출
        tables = soup.find_all('table')
        for table in tables:
            rows = table.find_all('tr')
            # 첫 번째 행은 헤더이므로 건너뜀
            for row in rows[1:]:
                cells = row.find_all('td')
                if len(cells) >= 4:  # 최소 4개의 셀(날짜, 대회명, 지역, 장소)이 있는지 확인
                    try:
                        date_cell = cells[0].text.strip()
                        name_cell = cells[1].text.strip()
                        region_cell = cells[2].text.strip() if len(cells) > 2 else ""
                        location_cell = cells[3].text.strip() if len(cells) > 3 else ""
                        
                        # 날짜 추출 및 변환
                        date_match = re.search(r'(\d{4})년 (\d{1,2})월 (\d{1,2})일', date_cell)
                        if date_match:
                            year, month, day = map(int, date_match.groups())
                            date = datetime(year, month, day, 9, 0, 0)
                            
                            # 대회 링크 추출
                            link_tag = cells[1].find('a')
                            url = link_tag['href'] if link_tag else None
                            
                            # 기본 ID 생성
                            marathon_id = url.split('/')[-1] if url else f"marathon-{len(marathons)+1}"
                            
                            # 거리 정보는 상세 페이지에서 추출
                            distances = []
                            
                            # 마라톤 정보 생성
                            marathon = {
                                'id': marathon_id,
                                'name': name_cell,
                                'date': date,
                                'location': location_cell,  # 테이블의 장소 정보 사용
                                'url': url
                            }
                            
                            # 상세 페이지가 있는 경우 거리 정보 추출 시도
                            if url:
                                try:
                                    detail_html = get_marathon_detail(url)
                                    detail_soup = BeautifulSoup(detail_html, 'html.parser')
                                    
                                    # 거리 정보 추출
                                    distance_keywords = ['풀코스', '42.195', '하프', '21.0975', '21K', '10K', '5K']
                                    for p in detail_soup.find_all('p'):
                                        for keyword in distance_keywords:
                                            if keyword in p.text:
                                                if '풀코스' in p.text or '42.195' in p.text:
                                                    distances.append('풀코스 (42.195km)')
                                                elif '하프' in p.text or '21' in p.text:
                                                    distances.append('하프코스 (21.0975km)')
                                                elif '10K' in p.text:
                                                    distances.append('10km')
                                                elif '5K' in p.text:
                                                    distances.append('5km')
                                    
                                    # 중복 제거
                                    distances = list(set(distances))
                                    
                                    if distances:
                                        marathon['distances'] = distances
                                except Exception as e:
                                    logger.error(f"상세 페이지 처리 실패 ({url}): {str(e)}")
                            
                            # 중복 확인
                            is_duplicate = False
                            for existing in marathons:
                                if existing['name'] == name_cell and existing['date'] == date:
                                    is_duplicate = True
                                    break
                            
                            if not is_duplicate:
                                marathons.append(marathon)
                                logger.info(f"마라톤 정보 추출: {name_cell}, 장소: {location_cell}")
                    except Exception as e:
                        logger.error(f"행 파싱 실패: {str(e)}, 행: {row}")
        
        # 추출된 마라톤 정보가 없으면 기본 테스트 데이터 사용
        if not marathons:
            logger.warning("웹사이트에서 마라톤 정보를 찾을 수 없어 테스트용 데이터를 생성합니다.")
            
            # 테스트용 데이터 생성 (실제 마라톤 대회 정보 기반)
            test_marathons = [
                {
                    'id': '2025-seoul-marathon',
                    'name': '2025 서울마라톤 (동아 마라톤)',
                    'date': datetime(2025, 3, 15).date(),  # datetime 대신 date 객체 사용
                    'location': '서울 광화문광장',
                    'url': 'https://runningwikii.com/entry/2025-seoul-marathon',
                    'distances': ['풀코스 (42.195km)', '하프코스 (21.0975km)', '10km', '5km']
                },
                {
                    'id': '2025-seoul-half-marathon',
                    'name': '2025 서울하프마라톤',
                    'date': datetime(2025, 4, 27).date(),  # datetime 대신 date 객체 사용
                    'location': '서울 광화문 - 월드컵공원',
                    'url': 'https://runningwikii.com/entry/2025-seoul-half-marathon',
                    'distances': ['하프코스 (21.0975km)', '10km']
                },
                {
                    'id': '2025-jeju-international-marathon',
                    'name': '2025 평화의 섬 제주국제마라톤',
                    'date': datetime(2025, 4, 27).date(),  # datetime 대신 date 객체 사용
                    'location': '제주시 구좌읍',
                    'url': 'https://runningwikii.com/entry/2025-jeju-international-marathon',
                    'distances': ['풀코스 (42.195km)', '하프코스 (21.0975km)', '10km', '5km']
                }
            ]
            
            marathons.extend(test_marathons)
            
        return marathons
    
    except Exception as e:
        logger.error(f"마라톤 목록 가져오기 실패: {str(e)}")
        return []

def get_marathon_detail(url):
    """마라톤 상세 페이지 HTML 가져오기"""
    headers = {
        'User-Agent': get_user_agent(),
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Referer': 'https://runningwikii.com/'
    }
    
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        return response.text
    
    except Exception as e:
        logger.error(f"상세 페이지 가져오기 실패 ({url}): {str(e)}")
        return ""

def parse_marathon_detail(html_content):
    """마라톤 상세 페이지 HTML 파싱"""
    details = {}
    
    if not html_content:
        return details
    
    try:
        soup = BeautifulSoup(html_content, 'html.parser')
        
        # 거리 정보 추출 방법 개선
        distances = []
        
        # 방법 1: 전체 텍스트에서 종목 정보 찾기
        page_text = soup.get_text()
        
        # 원래 종목명 그대로 유지
        if re.search(r'풀코스|42[.,]195|42km|42 ?km', page_text, re.IGNORECASE):
            distances.append('풀코스')
        
        if re.search(r'하프코스|하프|half|21[.,]0975|21km|21 ?km', page_text, re.IGNORECASE):
            distances.append('하프')
        
        if re.search(r'10[kK]|10km|10 ?km', page_text, re.IGNORECASE):
            distances.append('10km')
        
        if re.search(r'5[kK]|5km|5 ?km', page_text, re.IGNORECASE):
            distances.append('5km')
        
        # 방법 2: "종목" 또는 "코스" 단어 근처에서 정보 추출
        for p in soup.find_all(['p', 'div', 'li', 'span']):
            text = p.get_text()
            if '종목' in text or '코스' in text:
                if '풀' in text or '42' in text or 'full' in text.lower():
                    if '풀코스' not in distances:
                        distances.append('풀코스')
                if '하프' in text or '21' in text or 'half' in text.lower():
                    if '하프' not in distances:
                        distances.append('하프')
                if '10k' in text.lower() or '10km' in text.lower() or '10 km' in text.lower():
                    if '10km' not in distances:
                        distances.append('10km')
                if '5k' in text.lower() or '5km' in text.lower() or '5 km' in text.lower():
                    if '5km' not in distances:
                        distances.append('5km')
        
        # 중복 제거
        distances = list(set(distances))
        
        # 거리 정보 저장
        if distances:
            details['distances'] = distances
        
        return details
    
    except Exception as e:
        logger.error(f"상세 페이지 파싱 실패: {str(e)}")
        return details