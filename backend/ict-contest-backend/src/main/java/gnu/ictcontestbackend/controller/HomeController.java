package gnu.ictcontestbackend.controller;

import gnu.ictcontestbackend.repository.WeatherInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // 추가
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.*;

@Controller
@RequestMapping("/gnu-weather")
@RequiredArgsConstructor // 레포지토리 주입을 위해 필요
public class HomeController {

    private final WeatherInfoRepository weatherInfoRepository;

    @GetMapping({"/",""})
    public String homeRedirect() {
        return "redirect:/gnu-weather/home"; // 리다이렉트 방식 권장
    }

    /**
     * currentWeather (Map): 현재 날씨 정보 Map 구조
     * laundryScore  (int): 빨래지수(점수)  빨래 추천점수
     * drynessScpre  (int): 건조지수(점수) 건조 지수 점수로 산불 조심과 흡연 경고
     * alertList  (List): 알람 리스트  현재는 빈 리스트만 띄운다.
     * chartDataJson  (String):  0,6,12,18 시 시간대별 데이터 그래프 담기
     * @param model
     * @return
     */
    @GetMapping("/home")
    public String home(Model model) {
        // 1. 현재 날씨 (에러 방지용 기본값)
        Map<String, Object> currentWeather = new HashMap<>();
        currentWeather.put("temp", "22");
        currentWeather.put("skyStatus", "맑음");
        model.addAttribute("currentWeather", currentWeather);

        // 2. 지수 및 코멘트
        model.addAttribute("laundryScore", 0);
        model.addAttribute("drynessScore", 0);

        // 3. 알림 리스트 (null 에러 방지를 위해 빈 리스트 생성)
        model.addAttribute("alertList", new ArrayList<Map<String, String>>());

        // 4. 차트 데이터 초기값
        model.addAttribute("chartDataJson", "{\"labels\":[\"0시\",\"6시\",\"12시\",\"18시\"], \"datasets\":[{\"data\":[0,0,0,0]}]}");

        return "home";
    }
}