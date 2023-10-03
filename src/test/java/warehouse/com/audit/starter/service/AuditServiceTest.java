package warehouse.com.audit.starter.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static warehouse.com.audit.starter.common.Constants.UPDATED;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaUtils;
import warehouse.com.audit.starter.annotation.AuditableEntity;
import warehouse.com.audit.starter.entity.valid.TestEntity;
import warehouse.com.eventstarter.model.AuditEvent;
import warehouse.com.eventstarter.service.EventService;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

  private static final String INITIATOR_ID = "initiatorId";
  private static final String TEST_PREFERRED_USER_NAME = "TestPreferredUserName";
  private static final String DESCRIPTION = "Description";
  private final TestEntity auditable = TestEntity.builder().build();
  private final Object nonAuditable = new Object();
  private final warehouse.com.audit.starter.entity.invalid.type.TestEntity invalidTypeAuditable = warehouse.com.audit.starter.entity.invalid.type.TestEntity.builder()
      .build();
  private final warehouse.com.audit.starter.entity.invalid.name.TestEntity invalidIdAuditable = warehouse.com.audit.starter.entity.invalid.name.TestEntity.builder()
      .build();
  @Captor
  ArgumentCaptor<AuditEvent> eventCaptor;
  @InjectMocks
  private AuditService auditService;
  @Mock
  private EventService eventService;

  @AfterAll
  static void cleanUp() {
    KafkaUtils.setConsumerGroupId(null);
  }

  @Test
  void shouldReturnValueWhenAuditablePresent() {
    //when
    var auditables = auditService.getAuditables(auditable);
    //then
    assertTrue(auditables.isPresent());
    var result = auditables.get();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0)).isEqualTo(auditable);
  }

  @Test
  void shouldReturnValueWhenAuditableListPresent() {
    //given
    var auditableList = List.of(auditable);
    //when
    var auditables = auditService.getAuditables(auditableList);
    //then
    assertTrue(auditables.isPresent());
    var result = auditables.get();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0)).isEqualTo(auditable);
  }

  @Test
  void shouldNotReturnValueWhenNotAuditable() {
    //when
    var auditables = auditService.getAuditables(nonAuditable);
    //then
    assertThat(auditables).isEmpty();
  }

  @Test
  void shouldReturnAnnotationValueWhenAuditablePresent() {
    //when
    var auditableEntity = auditService.getAnnotation(auditable);
    //then
    assertThat(auditableEntity.annotationType()).isEqualTo(AuditableEntity.class);
  }

  @Test
  void shouldNotReturnAnnotationValueWhenNotAuditable() {
    //when
    var auditableEntity = auditService.getAnnotation(nonAuditable);
    //then
    assertThat(auditableEntity).isNull();
  }

  @Test
  void shouldSendAuditEvent() {
    //given
    //when
    auditService.sendAuditEvent(auditable, UPDATED);
    //then
    verify(eventService).publish(eventCaptor.capture());
    AuditEvent event = eventCaptor.getValue();
    assertThat(event.getAction()).isEqualTo(UPDATED);
    assertThat(event.getEntities().size()).isEqualTo(1);
    assertThat(event.getEntityType()).isEqualTo("test");
    assertThat(event.getTimestamp().getClass()).isEqualTo(Date.class);
  }

  @Test
  void shouldSetGivenInitiatorIdAndDescriptionWhenSendAuditEvent() {
    //when
    auditService.sendAuditEvent(auditable, UPDATED, INITIATOR_ID, DESCRIPTION);
    //then
    verify(eventService).publish(eventCaptor.capture());
    AuditEvent event = eventCaptor.getValue();
    assertThat(event.getInitiatorId()).isEqualTo(INITIATOR_ID);
    assertThat(event.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  void shouldSetGivenInitiatorIdWhenSendAuditEvent() {
    //when
    auditService.sendAuditEvent(auditable, UPDATED, INITIATOR_ID);
    //then
    verify(eventService).publish(eventCaptor.capture());
    AuditEvent event = eventCaptor.getValue();
    assertThat(event.getInitiatorId()).isEqualTo(INITIATOR_ID);
  }

  @Test
  void shouldReturnEntityTypeIsNullWhenAuditableTypeInvalid() {
    //when
    auditService.sendAuditEvent(invalidTypeAuditable, UPDATED);
    //then
    verify(eventService).publish(eventCaptor.capture());
    AuditEvent event = eventCaptor.getValue();
    assertThat(event.getEntityType()).isNull();
  }

  @Test
  void shouldSetNullOrgIdWhenAuthFailedAndPropertyNotExist() {
    //given
    //when
    auditService.sendAuditEvent(invalidIdAuditable, UPDATED);
    //then
    verify(eventService).publish(eventCaptor.capture());
    AuditEvent event = eventCaptor.getValue();
    assertThat(event.getEventId()).isNull();
  }
}
