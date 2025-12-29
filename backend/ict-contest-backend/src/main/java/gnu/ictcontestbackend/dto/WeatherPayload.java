package gnu.ictcontestbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class WeatherPayload {
    private String currentTemp;
    private String currentReh;
    private List<ForecastItem> forecastList;

    @Data
    public static class ForecastItem {
        private String time;
        private double temp;
        private double laundryScore;
        private double drynessScore;
    }
}