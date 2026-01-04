package gnu.ictcontestbackend.controller;

import gnu.ictcontestbackend.dto.WeatherPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.*;

@Controller
@RequestMapping("/gnu-weather")
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        WeatherPayload data = WeatherController.latestData;

        if (data != null && data.getForecastList() != null) {
            // 1. 대시보드 (진주 실제 온도 반영)
            model.addAttribute("currentWeather", Map.of(
                    "temp", data.getCurrentTemp(),
                    "humidity", data.getCurrentReh(),
                    "skyStatus", "경상국립대학교 실시간 기상 정보"
            ));

            // 2. 현재 점수
            model.addAttribute("laundryScore", (int)data.getForecastList().get(0).getLaundryScore());
            model.addAttribute("drynessScore", (int)data.getForecastList().get(0).getDrynessScore());

            // 3. 24시간 Chart.js 데이터
            Map<String, Object> chartData = new HashMap<>();
            chartData.put("labels", data.getForecastList().stream().map(f -> f.getTime()).toList());
            chartData.put("scores", data.getForecastList().stream().map(f -> f.getLaundryScore()).toList());
            chartData.put("temps", data.getForecastList().stream().map(f -> f.getTemp()).toList());
            model.addAttribute("chartDataObj", chartData);
        } else {
            // 데이터 수신 전 기본값
            model.addAttribute("currentWeather", Map.of("temp", "--", "humidity", "0", "skyStatus", "수신 대기 중"));
            model.addAttribute("laundryScore", 0);
            model.addAttribute("drynessScore", 0);
            model.addAttribute("chartDataObj", Map.of("labels", List.of(), "scores", List.of(), "temps", List.of()));
        }
        model.addAttribute("alertList", new ArrayList<>());
        model.addAttribute("graphUrl", "https://naltongsalm1.blob.core.windows.net/graphs/weather_detail.png");
        return "home";
    }
}