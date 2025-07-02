package com.example.ootd.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegionPartitioner implements Partitioner {

  @Value("${weather.regions.file.path:classpath:regions.csv}")
  private String regionsFilePath;

  @Value("${weather.batch.regions-per-partition:5}")
  private int regionsPerPartition;

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    Map<String, ExecutionContext> partitions = new HashMap<>();
    List<RegionInfo> regions = readRegionsFromCsv();

    // 지역을 청크로 나누기
    List<List<RegionInfo>> regionChunks = partitionList(regions, regionsPerPartition);

    for (int i = 0; i < regionChunks.size(); i++) {
      ExecutionContext context = new ExecutionContext();
      context.put("regions", regionChunks.get(i));
      context.put("partitionId", i);
      partitions.put("partition" + i, context);
    }

    log.info("Created {} partitions for {} regions", partitions.size(), regions.size());
    return partitions;
  }

  private List<List<RegionInfo>> partitionList(List<RegionInfo> list, int partitionSize) {
    List<List<RegionInfo>> partitions = new ArrayList<>();
    for (int i = 0; i < list.size(); i += partitionSize) {
      partitions.add(list.subList(i, Math.min(i + partitionSize, list.size())));
    }
    return partitions;
  }

  private List<RegionInfo> readRegionsFromCsv() {
    FlatFileItemReader<RegionInfo> reader = new FlatFileItemReaderBuilder<RegionInfo>()
        .name("regionInfoReader")
        .resource(getResource())
        .delimited()
        .names("regionName", "nx", "ny")
        .fieldSetMapper(new BeanWrapperFieldSetMapper<RegionInfo>() {{
          setTargetType(RegionInfo.class);
        }})
        .linesToSkip(1)
        .build();

    List<RegionInfo> regions = new ArrayList<>();
    try {
      reader.open(null);
      RegionInfo region;
      while ((region = reader.read()) != null) {
        regions.add(region);
      }
      reader.close();
    } catch (Exception e) {
      log.error("Failed to read CSV file", e);
      throw new RuntimeException("CSV 파일 읽기 실패", e);
    }

    return regions;
  }

  private Resource getResource() {
    if (regionsFilePath.startsWith("classpath:")) {
      return new ClassPathResource(regionsFilePath.substring("classpath:".length()));
    } else {
      return new FileSystemResource(regionsFilePath);
    }
  }
}