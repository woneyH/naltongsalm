package gnu.ictcontestbackend.config;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
@EnableScheduling // 스케줄링 기능 활성화
public class WeatherInitializer {

    // 배포된 Python Azure Function 주소 (꼭 환경변수나 실제 주소로 변경!)
    private final String AZURE_FUNCTION_URL = "https://naltongsalm-c8cxh6fvgbgzdgha.koreacentral-01.azurewebsites.net/api/weather_now";

    // 1. 서버 켜지자마자 1회 실행 (기존 기능 유지)
    // 2. 이후 30분마다(1800000ms) 자동으로 실행
    @Scheduled(fixedRate = 1800000)
    public void wakeUpAndRefreshWeather() {
        System.out.println("=========================================");
        System.out.println("⏰ [스케줄러] 기상 데이터 갱신 시작 (1시간 주기)");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60 * 1000);
        factory.setReadTimeout(60 * 1000);
        RestTemplate restTemplate = new RestTemplate(factory);

        try {
            // Azure Function 호출
            String result = restTemplate.getForObject(AZURE_FUNCTION_URL, String.class);
            System.out.println("✅ 데이터 갱신 성공: " + result);
        } catch (Exception e) {
            System.out.println("⚠️ 데이터 갱신 실패: " + e.getMessage());
        }
        System.out.println("=========================================");
    }
}