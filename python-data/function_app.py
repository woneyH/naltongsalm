import azure.functions as func
import logging
from datetime import datetime
from api_agent import WeatherAPI
from processor import process_weather_data
from notifier import WebNotifier, check_and_notify

app = func.FunctionApp(http_auth_level=func.AuthLevel.ANONYMOUS)

@app.route(route="weather_now")
def weather_http_trigger(req: func.HttpRequest) -> func.HttpResponse:
    logging.info('통합 기상 데이터 처리 시작')
    
    try:
        # 1. 객체 생성
        api = WeatherAPI()
        notifier = WebNotifier() # 내부에서 SPRING_SERVER_URL 읽음
        
        # 2. 통합 데이터 가져오기 (실황 + 24시간 예보)
        ncst_data, fcst_data = api.get_integrated_data()
        
        if ncst_data and fcst_data:
            # 3. 데이터 가공 (현재 온도 1도 및 24시간 리스트 추출)
            result_payload = process_weather_data(ncst_data, fcst_data)
            
            # 4. 스프링 서버로 전체 페이로드 전송
            notifier.send_report(result_payload)
            
            # 5. [선택] Matplotlib 그래프 생성 (필요시 result_payload['forecastList'] 기반으로 수정 가능)
            # generate_weather_graph(processed_df) 
            
            return func.HttpResponse("진주 실시간 및 24시간 예보 전송 완료", status_code=200)
        else:
            return func.HttpResponse("기상청 API 응답이 비어있습니다.", status_code=500)
            
    except Exception as e:
        logging.error(f"에러 발생: {e}")
        return func.HttpResponse(f"서버 에러: {e}", status_code=500)