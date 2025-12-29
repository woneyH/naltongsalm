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
        latestData = data;
        if (data.getForecastList() != null && !data.getForecastList().isEmpty()) {
            entitySave(data);
        }
        return ResponseEntity.ok("데이터 수신 완료");
    }

    @GetMapping("/history")
    public List<WeatherInfo> getAllHistory() {
        return weatherInfoService.findAll();
    }

    private void entitySave(WeatherPayload data) {
        // 가장 첫 번째(현재 시간대) 예측 데이터를 대표로 저장
        WeatherPayload.ForecastItem first = data.getForecastList().get(0);
        WeatherInfo entity = WeatherInfo.builder()
                .weatherType("INTEGRATED")
                .message(data.getCurrentTemp() + "도 / " + data.getCurrentReh() + "%")
                .temp(Double.parseDouble(data.getCurrentTemp()))
                .humidity(Double.parseDouble(data.getCurrentReh()))
                .laundryScore(first.getLaundryScore())
                .createdAt(LocalDateTime.now())
                .build();
        weatherInfoService.save(entity);
    }
}
