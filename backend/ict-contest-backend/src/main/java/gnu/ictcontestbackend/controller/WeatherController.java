package gnu.ictcontestbackend.controller;

import gnu.ictcontestbackend.domain.WeatherInfo;
import gnu.ictcontestbackend.dto.WeatherPayload;
import gnu.ictcontestbackend.service.WeatherInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherInfoService weatherInfoService;
    public static WeatherPayload latestData;

    @PostMapping("/alerts")
    public ResponseEntity<String> receiveAlert(@RequestBody WeatherPayload data) {
        // [추가된 로그] 데이터가 들어오면 콘솔에 출력합니다.
        System.out.println("=================================================");
        System.out.println("📨 [WeatherController] Azure에서 데이터 도착함!");
        System.out.println("   - 현재 온도: " + data.getCurrentTemp());
        System.out.println("   - 현재 습도: " + data.getCurrentReh());
        System.out.println("=================================================");

        latestData = data;

        try {
            if (data.getForecastList() != null && !data.getForecastList().isEmpty()) {
                entitySave(data);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 데이터 저장 중 오류 (무시 가능): " + e.getMessage());
        }

        return ResponseEntity.ok("데이터 수신 완료");
    }

    @GetMapping("/history")
    public List<WeatherInfo> getAllHistory() {
        return weatherInfoService.findAll();
    }

    private void entitySave(WeatherPayload data) {
        // 데이터가 "--"일 경우 에러 방지 처리
        double temp = 0.0;
        double hum = 0.0;
        try {
            temp = Double.parseDouble(data.getCurrentTemp());
            hum = Double.parseDouble(data.getCurrentReh());
        } catch (NumberFormatException e) {
            // 온도가 "--" 등으로 올 경우 0.0으로 처리
        }

        WeatherPayload.ForecastItem first = data.getForecastList().get(0);
        WeatherInfo entity = WeatherInfo.builder()
                .weatherType("INTEGRATED")
                .message(data.getCurrentTemp() + "도 / " + data.getCurrentReh() + "%")
                .temp(temp)
                .humidity(hum)
                .laundryScore(first.getLaundryScore())
                .createdAt(LocalDateTime.now())
                .build();
        weatherInfoService.save(entity);
    }
}