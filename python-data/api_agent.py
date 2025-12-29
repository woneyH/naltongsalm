import requests
import pandas as pd
import os
from datetime import datetime, timedelta

class WeatherAPI:
    def __init__(self):
        self.base_url = 'http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/'
        self.key = os.environ.get('WEATHER_API_KEY')

    def fetch_api(self, endpoint, params):
        try:
            res = requests.get(self.base_url + endpoint, params=params, timeout=10)
            return res.json()['response']['body']['items']['item']
        except:
            return []

    def get_integrated_data(self):
        now = datetime.now()
        
        # 1. 실시간 대시보드용 (Ncst) - 진주 실제 기온 1도 반영 목적
        # 매시간 40분에 데이터가 확정되므로 안전하게 45분 전 기준
        base_date = now.strftime('%Y%m%d')
        base_time_ncst = (now - timedelta(minutes=45)).strftime('%H00')
        
        ncst_params = {
            'serviceKey': self.key, 'dataType': 'JSON', 'nx': 81, 'ny': 84,
            'base_date': base_date, 'base_time': base_time_ncst
        }
        ncst_data = self.fetch_api('getUltraSrtNcst', ncst_params)

        # 2. 24시간 미래 그래프용 (VilageFcst)
        # 단기예보 기준 시간: 02, 05, 08, 11, 14, 17, 20, 23시
        available_hours = [2, 5, 8, 11, 14, 17, 20, 23]
        last_hour = max([h for h in available_hours if h <= now.hour])
        base_time_fcst = f"{last_hour:02d}00"
        
        fcst_params = {
            'serviceKey': self.key, 'dataType': 'JSON', 'nx': 81, 'ny': 84,
            'base_date': base_date, 'base_time': base_time_fcst, 'numOfRows': 300
        }
        fcst_data = self.fetch_api('getVilageFcst', fcst_params)
        
        return ncst_data, fcst_data