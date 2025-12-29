package gnu.ictcontestbackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class WeatherInfo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String weatherType;
    private String message;
    private double temp;
    private double humidity;
    private double laundryScore;
    private LocalDateTime createdAt;
}
