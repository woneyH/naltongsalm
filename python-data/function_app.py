import azure.functions as func
import logging
from api_agent import WeatherAPI
from processor import process_weather_data
from notifier import WebNotifier
from visualizer import generate_weather_graph

app = func.FunctionApp()

# =========================================================
# 핵심 로직 함수
# =========================================================
def run_weather_logic(trigger_source: str):
    logging.info(f"[{trigger_source}] 기상 데이터 처리 로직 시작")
    try:
        # 1. 객체 생성 및 설정
        api = WeatherAPI()
        #
        # [중요] 내일 아침에 VS Code 켜고, DevTunnel 주소가 바뀌었는지 꼭 확인하세요!
        # 바뀌었다면 여기 주소만 수정해서 다시 배포하면 됩니다.
        target_url = "https://naltongsalm-gnu.azurewebsites.net/api/alerts"

        notifier = WebNotifier(target_url) 
        
        # 2. 데이터 가져오기
        ncst_data, fcst_data = api.get_integrated_data()
        
        if ncst_data and fcst_data:
            # 3. 데이터 가공
            result_payload = process_weather_data(ncst_data, fcst_data)
            
            # 4. 그래프 생성
            try:
                generate_weather_graph(result_payload['forecastList'])
            except Exception as e:
                logging.error(f"그래프 에러: {e}")
            
            # 5. 전송
            notifier.send_report(result_payload)
            logging.info(f"✅ [{trigger_source}] 전송 완료")
            return "성공"
        else:
            logging.warning("데이터 없음")
            return "실패 (API 응답 없음)"
            
    except Exception as e:
        logging.error(f"에러 발생: {e}")
        return f"에러: {e}"

# =========================================================
# ❌ [사용 안 함] 자동 API 호출 방지 (주석 처리됨)
# 밤새 기상청 API 트래픽을 아끼기 위해 끕니다.
# =========================================================
# @app.schedule(schedule="0 */10 * * * *", arg_name="myTimer", run_on_startup=True, use_monitor=False) 
# def weather_timer_trigger(myTimer: func.TimerRequest) -> None:
#     run_weather_logic("TIMER_AUTO")

# =========================================================
# ✅ [사용] 수동 실행 (Spring Boot가 깨울 때 사용)
# =========================================================
@app.route(route="weather_now", auth_level=func.AuthLevel.ANONYMOUS)
def weather_http_trigger(req: func.HttpRequest) -> func.HttpResponse:
    result = run_weather_logic("HTTP_MANUAL")
    return func.HttpResponse(f"수동 실행 결과: {result}", status_code=200)