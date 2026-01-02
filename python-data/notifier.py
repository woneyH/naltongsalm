import os
import requests
import json
from datetime import datetime

class WebNotifier:
    def __init__(self, spring_url=None):
        """
        생성자에서 spring_url이 전달되지 않으면 환경 변수에서 가져옵니다.
        """
        self.spring_url = spring_url or os.environ.get('SPRING_SERVER_URL')

    def send_report(self, payload):
        """종합 기상 데이터를 스프링 서버로 전송합니다."""
        if not self.spring_url:
            print("오류: SPRING_SERVER_URL 설정이 없습니다. Azure Portal 환경 변수를 확인하세요.")
            return

        try:
            headers = {'Content-Type': 'application/json'}
            # JSON 데이터를 문자열로 직렬화하여 POST 요청을 보냅니다.
            # [수정됨] timeout을 5초 -> 30초로 변경하여 연결 안정성 확보
            response = requests.post(
                self.spring_url, 
                data=json.dumps(payload), 
                headers=headers, 
                timeout=30
            )
            print(f"서버 전송 결과: {response.status_code}")
        except Exception as e:
            print(f"Spring 서버 연결 실패 (터널 확인 필요): {e}")

def check_and_notify(notifier, current_row):
    # (이 아래 부분은 기존 코드와 동일하므로 그대로 두시면 됩니다)
    """
    임계치 체크뿐만 아니라 모든 기상 수치를 통합하여 전송합니다.
    """
    # 1. 데이터 추출
    temp = current_row.get('T1H', 0)      # 기온
    reh = current_row.get('REH', 0)       # 습도
    wsd = current_row.get('WSD', 0)       # 풍속
    rn1 = current_row.get('RN1', 0)       # 1시간 강수량
    lscore = current_row.get('laundry_score', 0)
    
    # 건조 지수 계산
    dscore = 100 - reh 

    # 2. 요약 메시지 생성
    status_msg = f"현재 기온 {temp}°C, 습도 {reh}%입니다."

    # 3. 통합 페이로드 구성
    payload = {
        "type": "ALL_DATA_UPDATE",
        "message": status_msg,
        "laundryScore": float(lscore),
        "drynessScore": float(dscore),
        "temp": float(temp),
        "humidity": float(reh),
        "windSpeed": float(wsd),
        "precipitation": float(rn1),
        "timestamp": datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    }

    # 4. 서버로 데이터 전송
    notifier.send_report(payload)