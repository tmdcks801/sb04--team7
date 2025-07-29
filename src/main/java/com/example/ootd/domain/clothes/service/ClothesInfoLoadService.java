package com.example.ootd.domain.clothes.service;

import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.exception.clothes.UnsupportedCrawlingUrlException;
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

    if (!supports()) {
      throw UnsupportedCrawlingUrlException.withUrl(url);
    }

    return loader();
  }

  private boolean supports() {

    if (url.contains("zigzag.kr")) {
      return true;
    } else if (url.contains("a-bly.com")) {
      return true;
    } else if (url.contains("musinsa.com")) {
      return true;
    }

    return false;
  }

  // 무신사
  private ClothesDto loader() {
    try {
      Connection connection = Jsoup.connect(url)
          .userAgent("Mozilla/5.0")
          .referrer("https://www.google.com")
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
