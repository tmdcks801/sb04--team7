package com.example.ootd.domain.weather.service;


import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import java.time.LocalDateTime;
import java.util.List;

public interface WeatherService {

  WeatherSummaryDto getSummaryWeather(double latitude, double longitude);

  List<WeatherDto> getThreeDayWeather(double latitude, double longitude, LocalDateTime targetTime);

}
