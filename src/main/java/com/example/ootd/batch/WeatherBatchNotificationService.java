package com.example.ootd.batch;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherBatchNotificationService {

  // 나중에 제대로 만들 예정
  public void sendFailureNotification(String jobName, Map<String, List<String>> failedRegions,
      Long jobExecutionId) {
    StringBuilder message = new StringBuilder();
    message.append("날씨 배치 작업 일부 실패\n");
    message.append("Job Name: ").append(jobName).append("\n");
    message.append("Job Execution ID: ").append(jobExecutionId).append("\n");
    message.append("실패한 지역:\n");

    failedRegions.forEach((stage, regions) -> {
      message.append("- ").append(stage).append(" 단계: ")
          .append(String.join(", ", regions)).append("\n");
    });

    // 실제 알림 전송
    log.error("Batch failure notification: {}", message.toString());

  }
}
