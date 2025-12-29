package gnu.ictcontestbackend.service;

import gnu.ictcontestbackend.domain.WeatherInfo;
import gnu.ictcontestbackend.repository.WeatherInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherInfoService {
    private final WeatherInfoRepository weatherInfoRepository;

    @Autowired
    public WeatherInfoService(WeatherInfoRepository weatherInfoRepository) {
        this.weatherInfoRepository = weatherInfoRepository;
    }

    public void save(WeatherInfo weatherInfo) {
        weatherInfoRepository.save(weatherInfo);
    }

    public List<WeatherInfo> findAll() {
        return weatherInfoRepository.findAll();
    }
}
