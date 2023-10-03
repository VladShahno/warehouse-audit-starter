package warehouse.com.audit.starter.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import warehouse.com.audit.starter.aspect.AuditAspect;
import warehouse.com.audit.starter.service.AuditService;
import warehouse.com.audit.starter.service.ValidationProcessor;
import warehouse.com.eventstarter.service.EventService;

@AutoConfigureAfter(MongoAutoConfiguration.class)
public class AuditConfiguration {

  @Bean
  public ValidationProcessor validationProcessor() {
    return new ValidationProcessor();
  }

  @Bean
  public AuditAspect auditAspect(AuditService auditService) {
    return new AuditAspect(auditService);
  }

  @Bean
  public AuditService auditService(EventService eventService) {
    return new AuditService(eventService);
  }
}
