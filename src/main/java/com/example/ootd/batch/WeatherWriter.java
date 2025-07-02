package com.example.ootd.batch;

import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeatherWriter implements ItemWriter<Weather> {

  private final WeatherRepository weatherRepository;

  @Override
  public void write(Chunk<? extends Weather> items) {
    if (items == null || items.isEmpty()) {
      log.info("No weather data to write.");
      return;
    }

    weatherRepository.saveAll(items.getItems());
    log.info("Saved {} weather entries to database.", items.size());
  }
}