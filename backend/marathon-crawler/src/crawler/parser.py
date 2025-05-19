"""
마라톤 웹사이트 HTML 파싱 함수 모듈
"""
import random
import logging
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
                            
                            
                            if url:
                                try:
                                    # 상세 HTML 가져오기
                                    detail_html = get_marathon_detail(url)
                                    
                                    # ✅ parse_marathon_detail()로 통합 파싱
                                    details = parse_marathon_detail(detail_html)
                                    if 'distances' in details and details['distances']:
                                        marathon['distances'] = details['distances']  # 3개 모두 잡아옴
                                    
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
        
        # 첫 번째 유효한 '종목'만 꺼내오기
        raw_distance = None

        for tr in soup.find_all('tr'):
            tds = tr.find_all('td')
            if len(tds) < 2:
                continue
            header_td = tds[0]
            if header_td.get('id', '').startswith('Buo'):
                continue
            if header_td.get_text(strip=True) == '종목':
                raw_distance = tds[1].get_text(strip=True)
                break

        if raw_distance:
            raw_distance = re.sub(r'(\d+)\s*km', r'\1km', raw_distance, flags=re.IGNORECASE)
            parts = re.split(r'[,\s]+', raw_distance)
            
            filtered = []
            for part in parts:
                if not part:
                    continue
                part = part.replace('(', '').replace(')', '')
                if '거리' in part:
                    continue
                if part.startswith('0'):
                    continue
                low = part.lower()
                if '풀' in part or '하프' in part or 'km' in low:
                    filtered.append(part)

            mapped = []
            for part in filtered:
                if '풀' in part:
                    mapped.append('풀(42.195km)')
                elif '하프' in part:
                    mapped.append('하프(21.0975km)')
                else:
                    mapped.append(part)

            if mapped:
                details['distances'] = mapped
        
        return details
    
    except Exception as e:
        logger.error(f"상세 페이지 파싱 실패: {e}")
        return details