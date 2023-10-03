package warehouse.com.audit.starter;

import java.util.Date;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import warehouse.com.audit.starter.entity.valid.MethodTestEntity;
import warehouse.com.audit.starter.entity.valid.TestEntity;
import warehouse.com.audit.starter.service.AuditService;
import warehouse.com.eventstarter.model.AuditEvent;
import warehouse.com.eventstarter.service.EventService;

@ExtendWith(SpringExtension.class)
@Import(TestConfig.class)
public class TestSpringContext {

  public static final String NAME = "name";
  public static final String ID = "id";

  @Autowired
  protected AuditService auditService;

  @MockBean
  protected EventService eventService;


  @Captor
  protected ArgumentCaptor<AuditEvent> auditEventCaptor;

  protected TestEntity getTestEntity() {
    return TestEntity.builder().id(ID).name(NAME).build();
  }

  protected MethodTestEntity getMethodTestEntity() {
    return MethodTestEntity.builder().id(ID).name(NAME).lastModifiedDate(new Date())
        .build();
  }

  protected AuditEvent getExpected(String action, String entityType) {
    return AuditEvent.builder()
        .action(action)
        .entities(Set.of(AuditEvent.Entity.builder()
            .id(ID)
            .name(NAME)
            .build()))
        .entityType(entityType)
        .build();
  }
}
