package warehouse.com.audit.starter;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import warehouse.com.audit.starter.service.ValidationProcessor;
import warehouse.com.eventstarter.service.EventService;

@TestConfiguration
public class TestConfig {

  @Bean
  public ValidationProcessor validationProcessor() {
    return new ValidationProcessor();
  }

  @Bean
  public EventService eventService() {
    return mock(EventService.class);
  }
}
