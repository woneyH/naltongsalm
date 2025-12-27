package gnu.ictcontestbackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class WeatherInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String weatherType;

    @Column
    private String message;

    @Column
    private String weatherValue;

    @Column
    private LocalDateTime createdAt;


}
