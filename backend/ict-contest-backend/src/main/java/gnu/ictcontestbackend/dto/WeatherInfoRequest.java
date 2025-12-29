package gnu.ictcontestbackend.dto;

public record WeatherInfoRequest (
        String weatherType,
        String message,
        String weatherValue
){ }
