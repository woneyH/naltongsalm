import requests
import os
from datetime import datetime, timedelta

class WeatherAPI:
    def __init__(self):
        self.base_url = 'http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/'
        # Azure Portal의 [환경 변수]에 'WEATHER_API_KEY'가 설정되어 있어야 합니다.
        self.key = os.environ.get('WEATHER_API_KEY')

    def fetch_api(self, endpoint, params):
        try:
            # 타임아웃을 10초로 설정
            res = requests.get(self.base_url + endpoint, params=params, timeout=10)
            
            # 응답 상태 코드가 200이 아니면 실패 처리
            if res.status_code != 200:
                print(f"API Error Status: {res.status_code}")
                return []
            
            # JSON 파싱 시도
            json_data = res.json()
            
            # 기상청 에러 메시지(SERVICE ERROR 등)가 오는지 확인
            if 'response' not in json_data or 'body' not in json_data['response']:
                print(f"API Structure Error: {json_data}")
                return []
                
            return json_data['response']['body']['items']['item']
        except Exception as e:
            print(f"Fetch Error ({endpoint}): {e}")
            return []

    def get_integrated_data(self):
        # [핵심 수정] Azure(UTC) 시간을 한국 시간(KST)으로 변환
        now = datetime.utcnow() + timedelta(hours=9)
        
        # 1. 실시간 대시보드용 (Ncst)
        base_date = now.strftime('%Y%m%d')
        base_time_ncst = (now - timedelta(minutes=45)).strftime('%H00')
        
        print(f"DEBUG: Requesting NCST for {base_date} {base_time_ncst}")

        ncst_params = {
            'serviceKey': self.key, 'dataType': 'JSON', 'nx': 81, 'ny': 84,
            'base_date': base_date, 'base_time': base_time_ncst
        }
        ncst_data = self.fetch_api('getUltraSrtNcst', ncst_params)

        # 2. 24시간 미래 그래프용 (VilageFcst)
        # 단기예보 기준 시간: 02, 05, 08, 11, 14, 17, 20, 23시
        available_hours = [2, 5, 8, 11, 14, 17, 20, 23]
        
        current_hour = now.hour
        
        # [버그 수정] 0시, 1시에는 max() 계산 전에 먼저 어제 23시로 설정해야 함
        if current_hour < 2:
            base_date_fcst = (now - timedelta(days=1)).strftime('%Y%m%d')
            base_time_fcst = "2300"
        else:
            base_date_fcst = base_date
            # 이제 2시 이상임이 보장되므로 max() 계산이 안전함
            last_hour = max([h for h in available_hours if h <= current_hour])
            base_time_fcst = f"{last_hour:02d}00"

        print(f"DEBUG: Requesting FCST for {base_date_fcst} {base_time_fcst}")

        fcst_params = {
            'serviceKey': self.key, 'dataType': 'JSON', 'nx': 81, 'ny': 84,
            'base_date': base_date_fcst, 'base_time': base_time_fcst, 'numOfRows': 300
        }
        fcst_data = self.fetch_api('getVilageFcst', fcst_params)
        
        return ncst_data, fcst_data