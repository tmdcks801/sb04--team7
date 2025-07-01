package com.example.ootd.batch;

import com.example.ootd.domain.weather.api.WeatherApiResponse;
import com.example.ootd.domain.weather.api.WeatherApiResponse.Item;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class WeatherBatchData {

  private String regionName;
  private List<Item> items = new ArrayList<>();

  public void addItem(WeatherApiResponse.Item item) {
    this.items.add(item);
  }
}
