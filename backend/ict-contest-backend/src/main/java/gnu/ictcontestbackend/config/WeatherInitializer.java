package gnu.ictcontestbackend.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherInitializer {

    // Azure Function 수동 실행 URL
    private final String AZURE_FUNCTION_URL = "https://naltongsalm-c8cxh6fvgbgzdgha.koreacentral-01.azurewebsites.net/api/weather_now";

    @EventListener(ApplicationReadyEvent.class)
    public void wakeUpAzureFunction() {
        System.out.println("=========================================");
        System.out.println("🚀 Spring Boot 시작됨: Azure Function 깨우기 시도...");

        // 1. 타임아웃 설정 (60초까지 대기)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60 * 1000); // 연결 대기: 60초
        factory.setReadTimeout(60 * 1000);    // 데이터 수신 대기: 60초
        RestTemplate restTemplate = new RestTemplate(factory);

        int maxRetries = 5; // 최대 5번 시도
        boolean success = false;

        // 2. 재시도 로직 (반복문)
        for (int i = 1; i <= maxRetries; i++) {
            try {
                System.out.println("⏳ [시도 " + i + "/" + maxRetries + "] 기상 데이터 요청 중... (Azure Cold Start 대기)");

                // Azure Function 호출
                String result = restTemplate.getForObject(AZURE_FUNCTION_URL, String.class);

                System.out.println("✅ Azure 응답 성공: " + result);
                success = true;
                break; // 성공하면 반복문 탈출

            } catch (Exception e) {
                System.out.println("⚠️ 시도 " + i + " 실패: Azure가 아직 준비되지 않았거나 응답이 없습니다.");
                System.out.println("   (에러 내용: " + e.getMessage() + ")");

                if (i < maxRetries) {
                    try {
                        System.out.println("💤 3초 후 다시 시도합니다...");
                        Thread.sleep(3000); // 3초 대기
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        if (!success) {
            System.out.println("❌ 최종 실패: Azure Function이 응답하지 않습니다. (Azure Portal에서 상태를 확인하세요)");
        }
        System.out.println("=========================================");
    }
}