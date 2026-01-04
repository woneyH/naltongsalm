package gnu.ictcontestbackend.controller;

import gnu.ictcontestbackend.dto.WeatherPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/gnu-weather")
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        WeatherPayload data = WeatherController.latestData;
        List<Map<String, String>> alertList = new ArrayList<>(); // 알람을 담을 리스트

        if (data != null && data.getForecastList() != null && !data.getForecastList().isEmpty()) {
            // 1. 현재 데이터 바인딩
            model.addAttribute("currentWeather", Map.of(
                    "temp", data.getCurrentTemp(),
                    "humidity", data.getCurrentReh(),
                    "skyStatus", "경상국립대학교 실시간 기상 정보"
            ));

            // 2. 점수 추출 (첫 번째 시간대 기준)
            double lScore = data.getForecastList().get(0).getLaundryScore();
            double dScore = data.getForecastList().get(0).getDrynessScore();

            model.addAttribute("laundryScore", (int) lScore);
            model.addAttribute("drynessScore", (int) dScore);

            // ==========================================
            // [핵심 로직] 점수에 따른 알람 생성
            // ==========================================

            // 1) 빨래 추천 (70점 이상)
            if (lScore >= 70) {
                Map<String, String> alert = new HashMap<>();
                alert.put("type", "success"); // 초록색 (css: .alert-success)
                alert.put("title", "빨래 추천");
                alert.put("message", "☀️ 오늘은 빨래하기 좋아요!");
                alertList.add(alert);
            }
            // 2) 빨래 비추천 (40점 이하)
            else if (lScore <= 40) {
                Map<String, String> alert = new HashMap<>();
                alert.put("type", "warning"); // 노란색 (css: .alert-warning)
                alert.put("title", "빨래 주의");
                alert.put("message", "🌧️ 오늘은 빨래 피하세요.");
                alertList.add(alert);
            }

            // 3) 건조/화재 경고 (65점 이상 -> 습도 35% 이하)
            if (dScore >= 65) {
                Map<String, String> alert = new HashMap<>();
                alert.put("type", "danger"); // 빨간색 (css: .alert-danger)
                alert.put("title", "건조 주의보");
                alert.put("message", "🚨 대기가 매우 건조합니다! 흡연 및 화재 주의 🚨");
                alertList.add(alert);
            }

            // 4. 차트 데이터
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

            // 수신 대기 알림
            Map<String, String> defaultAlert = new HashMap<>();
            defaultAlert.put("type", "secondary");
            defaultAlert.put("title", "데이터 수신 중");
            defaultAlert.put("message", "잠시만 기다려주세요...");
            alertList.add(defaultAlert);
        }

        model.addAttribute("alertList", alertList);
        model.addAttribute("graphUrl", "https://naltongsalm1.blob.core.windows.net/graphs/weather_detail.png");
        return "home";
    }
}