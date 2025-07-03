package com.example.ootd.batch.dto;

import com.example.ootd.domain.weather.api.WeatherApiResponse;
import com.example.ootd.domain.weather.api.WeatherApiResponse.Item;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class WeatherBatchData implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  private String regionName;
  private int nx;
  private int ny;
  private List<Item> items = new ArrayList<>();
  private List<Item> previousItems = new ArrayList<>();  // 전날 비교용 + TMN/TMX 데이터

  public void addItem(WeatherApiResponse.Item item) {
    this.items.add(item);
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public void setItems(List<Item> timeItems) {
    this.items = timeItems;
  }

  public void setNx(int nx) {
    this.nx = nx;
  }

  public void setNy(int ny) {
    this.ny = ny;
  }

  public void setPreviousItems(List<Item> previousItems) {
    this.previousItems = previousItems;
  }
}
