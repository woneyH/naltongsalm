package gnu.ictcontestbackend.repository;

import gnu.ictcontestbackend.domain.WeatherInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherInfoRepository extends JpaRepository<WeatherInfo, Long> {

}
