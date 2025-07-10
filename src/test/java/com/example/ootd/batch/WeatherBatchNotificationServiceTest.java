package com.example.ootd.batch;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherBatchNotificationService 테스트")
class WeatherBatchNotificationServiceTest {

    @InjectMocks
    private WeatherBatchNotificationService notificationService;
    
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        // 로그 캡처를 위한 설정
        logger = (Logger) LoggerFactory.getLogger(WeatherBatchNotificationService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    @DisplayName("실패 알림 전송 - 정상적인 실패 지역 정보")
    void sendFailureNotification_Success() {
        // Given
        String jobName = "weatherCollectJob";
        Long jobExecutionId = 12345L;
        
        Map<String, List<String>> failedRegions = new HashMap<>();
        failedRegions.put("READ", Arrays.asList("서울특별시 강남구", "부산광역시 해운대구"));
        failedRegions.put("PROCESS", Arrays.asList("제주특별자치도 제주시"));
        failedRegions.put("WRITE", Arrays.asList("대구광역시 수성구", "광주광역시 북구"));

        // When
        notificationService.sendFailureNotification(jobName, failedRegions, jobExecutionId);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(logEvent.getMessage()).contains("Batch failure notification: {}");
        
        String logMessage = (String) logEvent.getArgumentArray()[0];
        assertThat(logMessage).contains("날씨 배치 작업 일부 실패");
        assertThat(logMessage).contains("Job Name: weatherCollectJob");
        assertThat(logMessage).contains("Job Execution ID: 12345");
        assertThat(logMessage).contains("실패한 지역:");
        assertThat(logMessage).contains("READ 단계: 서울특별시 강남구, 부산광역시 해운대구");
        assertThat(logMessage).contains("PROCESS 단계: 제주특별자치도 제주시");
        assertThat(logMessage).contains("WRITE 단계: 대구광역시 수성구, 광주광역시 북구");
    }

    @Test
    @DisplayName("실패 알림 전송 - 빈 실패 지역 맵")
    void sendFailureNotification_EmptyFailedRegions() {
        // Given
        String jobName = "weatherCollectJob";
        Long jobExecutionId = 67890L;
        Map<String, List<String>> emptyFailedRegions = new HashMap<>();

        // When
        notificationService.sendFailureNotification(jobName, emptyFailedRegions, jobExecutionId);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        String logMessage = (String) logEvent.getArgumentArray()[0];
        assertThat(logMessage).contains("날씨 배치 작업 일부 실패");
        assertThat(logMessage).contains("Job Name: weatherCollectJob");
        assertThat(logMessage).contains("Job Execution ID: 67890");
        assertThat(logMessage).contains("실패한 지역:");
    }

    @Test
    @DisplayName("실패 알림 전송 - 일부 단계만 실패")
    void sendFailureNotification_PartialStageFailure() {
        // Given
        String jobName = "weatherCollectJob";
        Long jobExecutionId = 11111L;
        
        Map<String, List<String>> failedRegions = new HashMap<>();
        failedRegions.put("READ", Arrays.asList("인천광역시 연수구"));

        // When
        notificationService.sendFailureNotification(jobName, failedRegions, jobExecutionId);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        String logMessage = (String) logsList.get(0).getArgumentArray()[0];
        assertThat(logMessage).contains("READ 단계: 인천광역시 연수구");
        assertThat(logMessage).doesNotContain("PROCESS 단계:");
        assertThat(logMessage).doesNotContain("WRITE 단계:");
    }

    @Test
    @DisplayName("실패 알림 전송 - 단일 지역 실패")
    void sendFailureNotification_SingleRegionFailure() {
        // Given
        String jobName = "weatherCollectJob";
        Long jobExecutionId = 22222L;
        
        Map<String, List<String>> failedRegions = new HashMap<>();
        failedRegions.put("WRITE", Arrays.asList("울산광역시 남구"));

        // When
        notificationService.sendFailureNotification(jobName, failedRegions, jobExecutionId);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        String logMessage = (String) logsList.get(0).getArgumentArray()[0];
        assertThat(logMessage).contains("WRITE 단계: 울산광역시 남구");
    }

    @Test
    @DisplayName("실패 알림 전송 - 여러 지역이 한 단계에서 실패")
    void sendFailureNotification_MultipleRegionsInOneStage() {
        // Given
        String jobName = "weatherCollectJob";
        Long jobExecutionId = 33333L;
        
        Map<String, List<String>> failedRegions = new HashMap<>();
        failedRegions.put("PROCESS", Arrays.asList(
            "경기도 성남시", "경기도 안양시", "경기도 부천시", "경기도 고양시"
        ));

        // When
        notificationService.sendFailureNotification(jobName, failedRegions, jobExecutionId);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        String logMessage = (String) logsList.get(0).getArgumentArray()[0];
        assertThat(logMessage).contains("PROCESS 단계: 경기도 성남시, 경기도 안양시, 경기도 부천시, 경기도 고양시");
    }

    @Test
    @DisplayName("로그 레벨이 ERROR인지 확인")
    void sendFailureNotification_LogLevel() {
        // Given
        String jobName = "testJob";
        Long jobExecutionId = 44444L;
        Map<String, List<String>> failedRegions = new HashMap<>();
        failedRegions.put("TEST", Arrays.asList("테스트 지역"));

        // When
        notificationService.sendFailureNotification(jobName, failedRegions, jobExecutionId);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.ERROR);
    }
}
