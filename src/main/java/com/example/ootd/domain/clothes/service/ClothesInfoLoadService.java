package com.example.ootd.domain.clothes.service;

import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClothesInfoLoadService {

  private String url;

  public ClothesDto load(String url) {

    this.url = url;
    String tag = supports();

    switch (tag) {
      case "zigzag":
        return zigzagLoad();
      case "ably":
        return ablyLoad();
      case "musinsa":
        return musinsaLoad();
    }

    return null;
  }

  private String supports() {

    if (url.contains("zigzag.kr")) {
      return "zigzag";
    } else if (url.contains("a-bly.com")) {
      return "ably";
    } else if (url.contains("musinsa.com")) {
      return "musinsa";
    }

    return null;
  }

  // 지그재그
  private ClothesDto zigzagLoad() {
    try {
      Connection connection = Jsoup.connect(url)
          .timeout(10 * 1000);  // 최대 10초 대기
      Document document = connection.get();

      Elements elements = document.getElementsByAttributeValue("class",
          "BODY_15 REGULAR css-1n8byw e1jjvoab1");
      String name = elements.get(0).text();

      elements = document.getElementsByAttributeValue("alt", "상품 이미지");
      String imageUrl = elements.get(0).attr("src");

      ClothesDto clothesDto = ClothesDto.builder()
          .name(name)
          .imageUrl(imageUrl)
          .build();

      return clothesDto;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // 에이블리
  private ClothesDto ablyLoad() {
    try {
      Connection connection = Jsoup.connect(url)
          .userAgent("Mozilla/5.0")
          .referrer("https://www.google.com")
          .timeout(10 * 1000);  // 최대 10초 대기
      Document document = connection.get();

      Elements elements = document.getElementsByAttributeValue("class",
          "typography typography__body1 color__gray70");
      String name = elements.get(0).text();

      elements = document.getElementsByAttributeValue("alt", "상품 썸네일");
      String imageUrl = elements.get(0).attr("src");

      ClothesDto clothesDto = ClothesDto.builder()
          .name(name)
          .imageUrl(imageUrl)
          .build();

      return clothesDto;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // 무신사
  private ClothesDto musinsaLoad() {
    try {
      Connection connection = Jsoup.connect(url)
          .timeout(10 * 1000);  // 최대 10초 대기
      Document document = connection.get();

      Elements elements = document.select("meta[property=og:title]");
      String name = elements.attr("content");

      elements = document.select("meta[property=og:image]");
      String imageUrl = elements.attr("content");

      ClothesDto clothesDto = ClothesDto.builder()
          .name(name)
          .imageUrl(imageUrl)
          .build();

      return clothesDto;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
