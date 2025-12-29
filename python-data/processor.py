import pandas as pd

def process_weather_data(ncst_list, fcst_list):
    # 실시간 데이터 추출
    ncst_df = pd.DataFrame(ncst_list).set_index('category')
    curr_temp = ncst_df.loc['T1H', 'obsrValue'] if 'T1H' in ncst_df.index else "--"
    curr_reh = ncst_df.loc['REH', 'obsrValue'] if 'REH' in ncst_df.index else "0"

    # 예보 데이터 가공 (24시간)
    fcst_df = pd.DataFrame(fcst_list)
    pivot_df = fcst_df.pivot(index=['fcstDate', 'fcstTime'], columns='category', values='fcstValue').reset_index()
    
    forecast_items = []
    # 상위 24개(1시간 단위 24시간분) 데이터 추출
    for _, row in pivot_df.head(24).iterrows():
        reh = float(row['REH'])
        wsd = float(row['WSD'])
        # 빨래지수 공식: (100-습도)*0.6 + (풍속*10)*0.4
        l_score = (100 - reh) * 0.6 + (wsd * 10) * 0.4
        
        forecast_items.append({
            "time": row['fcstTime'][:2] + "시",
            "temp": float(row['TMP']),
            "laundryScore": round(l_score, 1),
            "drynessScore": 100 - reh
        })
        
    return {
        "currentTemp": curr_temp,
        "currentReh": curr_reh,
        "forecastList": forecast_items
    }