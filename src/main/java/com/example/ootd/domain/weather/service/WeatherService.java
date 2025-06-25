package com.example.ootd.domain.weather.service;


import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;

public interface WeatherService {

  WeatherDto getWeather(double latitude, double longitude);

  WeatherSummaryDto getSummaryWeather(double latitude, double longitude);

}
