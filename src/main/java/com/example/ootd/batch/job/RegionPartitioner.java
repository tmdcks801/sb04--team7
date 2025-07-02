package com.example.ootd.batch.job;

import com.example.ootd.batch.RegionCsvReader;
import com.example.ootd.batch.dto.RegionInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegionPartitioner implements Partitioner {

  private final RegionCsvReader regionCsvReader;

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    Map<String, ExecutionContext> partitions = new HashMap<>();

    // CSV에서 252개 지역 정보 읽기
    List<RegionInfo> allRegions = regionCsvReader.readAllRegions();
    log.info("Total regions loaded: {}", allRegions.size());

    // 파티션당 지역 수 계산
    int regionsPerPartition = (allRegions.size() + gridSize - 1) / gridSize;

    for (int i = 0; i < gridSize; i++) {
      ExecutionContext context = new ExecutionContext();

      int startIdx = i * regionsPerPartition;
      int endIdx = Math.min(startIdx + regionsPerPartition, allRegions.size());

      if (startIdx < allRegions.size()) {
        List<RegionInfo> partitionRegions = allRegions.subList(startIdx, endIdx);

        // ExecutionContext에 지역 목록 저장
        context.put("regions", new ArrayList<>(partitionRegions));
        context.put("partitionId", i);
        context.put("startIndex", startIdx);
        context.put("endIndex", endIdx);

        partitions.put("partition" + i, context);

        log.info("Partition {} created with {} regions (index {} to {})",
            i, partitionRegions.size(), startIdx, endIdx - 1);

        // 디버깅용 - 각 파티션의 첫 번째와 마지막 지역 출력
        if (!partitionRegions.isEmpty()) {
          log.debug("Partition {}: {} ~ {}",
              i,
              partitionRegions.get(0).getRegionName(),
              partitionRegions.get(partitionRegions.size() - 1).getRegionName()
          );
        }
      }
    }

    return partitions;
  }
}