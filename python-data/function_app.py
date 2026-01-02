import azure.functions as func
import logging
from datetime import datetime
from api_agent import WeatherAPI
from processor import process_weather_data
from notifier import WebNotifier, check_and_notify

# visualizer 파일에서 함수 가져오기
from visualizer import generate_weather_graph

app = func.FunctionApp(http_auth_level=func.AuthLevel.ANONYMOUS)

@app.route(route="weather_now")
def weather_http_trigger(req: func.HttpRequest) -> func.HttpResponse:
    logging.info('통합 기상 데이터 처리 시작')
    
    try:
        # 1. 객체 생성
        api = WeatherAPI()
        
        # [중요] 사용자의 Spring Boot Dev Tunnel 주소로 설정
        # 이 주소로 데이터를 쏘게 됩니다.
        target_url = "https://m8jzbfpm-8080.asse.devtunnels.ms/api/alerts"
        notifier = WebNotifier(target_url) 
        
        # 2. 통합 데이터 가져오기 (실황 + 24시간 예보)
        ncst_data, fcst_data = api.get_integrated_data()
        
        if ncst_data and fcst_data:
            # 3. 데이터 가공 (현재 온도 및 24시간 리스트 추출)
            result_payload = process_weather_data(ncst_data, fcst_data)
            
            # 4. [그래프] Matplotlib 그래프 생성 및 업로드
            # result_payload 안에 있는 'forecastList'를 사용해 그래프를 그립니다.
            try:
                generate_weather_graph(result_payload['forecastList'])
                logging.info("그래프 생성 및 업로드 성공")
            except Exception as e:
                logging.error(f"그래프 생성 중 에러 발생: {e}")
            
            # 5. [전송] 스프링 서버로 전체 데이터 전송
            # 이 코드가 실행되어야 웹사이트 숫자가 바뀝니다.
            notifier.send_report(result_payload)
            logging.info(f"스프링 서버({target_url})로 데이터 전송 완료")
            
            return func.HttpResponse("진주 실시간 데이터 및 그래프 처리 완료", status_code=200)
        else:
            return func.HttpResponse("기상청 API 응답이 비어있습니다.", status_code=500)
            
    except Exception as e:
        logging.error(f"에러 발생: {e}")
        return func.HttpResponse(f"서버 에러: {e}", status_code=500)