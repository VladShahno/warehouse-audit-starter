package warehouse.com.audit.starter.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static warehouse.com.audit.starter.common.Constants.CREATED;
import static warehouse.com.audit.starter.common.Constants.DELETED;
import static warehouse.com.audit.starter.common.Constants.UPDATED;
import static warehouse.com.audit.starter.entity.valid.MethodTestEntity.METHOD_TYPE;
import static warehouse.com.audit.starter.entity.valid.TestEntity.TEST_ENTITY_TYPE;

import java.lang.annotation.Annotation;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import warehouse.com.audit.starter.TestSpringContext;
import warehouse.com.audit.starter.annotation.AuditEvent;
import warehouse.com.audit.starter.entity.valid.TestEntity;
import warehouse.com.audit.starter.service.AuditService;

@SpringBootTest(classes = {AuditService.class, AuditAspect.class})
class AuditAspectTest extends TestSpringContext {

  @Autowired
  private AuditAspect auditAspect;

  @AfterEach
  public void cleanUp() {
    ReflectionTestUtils
        .setField(auditAspect, "disableDefaultEvents",
            InheritableThreadLocal.withInitial(() -> Boolean.FALSE));
  }

  @Test
  void shouldLogEventWithArgumentEntity() throws Throwable {
    var joinPoint = mock(ProceedingJoinPoint.class);
    var args = new TestEntity[]{getTestEntity()};
    when(joinPoint.getArgs()).thenReturn(args);

    auditAspect.logAuditEvent(joinPoint, getAuditEvent("test", true));

    //then
    verify(eventService).publish(auditEventCaptor.capture());
    assertThat(auditEventCaptor.getValue())
        .isEqualToIgnoringGivenFields(getExpected("test", TEST_ENTITY_TYPE), "timestamp");
  }

  @Test
  void shouldLogEventWithReturnedEntity() throws Throwable {
    var joinPoint = mock(ProceedingJoinPoint.class);
    when(joinPoint.getArgs()).thenReturn(new Object[]{});
    when(joinPoint.proceed()).thenReturn(getTestEntity());

    auditAspect.logAuditEvent(joinPoint, getAuditEvent("test", true));

    //then
    verify(eventService).publish(auditEventCaptor.capture());
    assertThat(auditEventCaptor.getValue())
        .isEqualToIgnoringGivenFields(getExpected("test", TEST_ENTITY_TYPE), "timestamp");
  }

  @Test
  void shouldNotLogIfNoAnnotatedEntity() throws Throwable {
    var joinPoint = mock(ProceedingJoinPoint.class);
    when(joinPoint.getArgs()).thenReturn(new Object[]{});
    when(joinPoint.proceed()).thenReturn(new Object());

    auditAspect.logAuditEvent(joinPoint, getAuditEvent("test", true));

    //then
    verify(eventService, never()).publish(auditEventCaptor.capture());
  }

  @Test
  void shouldLogCreatedEvent() throws Throwable {
    //given
    var auditable = getTestEntity();
    var joinPoint = mock(ProceedingJoinPoint.class);
    when(joinPoint.getArgs()).thenReturn(new Object[]{});

    //when
    auditAspect.logSaveEvent(joinPoint, auditable);

    //then
    verify(eventService).publish(auditEventCaptor.capture());
    assertThat(auditEventCaptor.getValue())
        .isEqualToIgnoringGivenFields(getExpected(CREATED, TEST_ENTITY_TYPE), "timestamp");
  }

  @Test
  void shouldLogUpdatedEvent() throws Throwable {
    //given
    var auditable = getMethodTestEntity();
    var joinPoint = mock(ProceedingJoinPoint.class);
    when(joinPoint.getArgs()).thenReturn(new Object[]{});

    //when
    auditAspect.logSaveEvent(joinPoint, auditable);

    //then
    verify(eventService).publish(auditEventCaptor.capture());
    assertThat(auditEventCaptor.getValue())
        .isEqualToIgnoringGivenFields(getExpected(UPDATED, METHOD_TYPE), "timestamp");
  }

  @Test
  void shouldLogDeletedEvent() {
    //given
    var auditable = List.of(getTestEntity());

    //when
    auditAspect.logDeleteEvent(auditable);

    //then
    verify(eventService).publish(auditEventCaptor.capture());
    assertThat(auditEventCaptor.getValue())
        .isEqualToIgnoringGivenFields(getExpected(DELETED, TEST_ENTITY_TYPE), "timestamp");
  }

  @Test
  void shouldNotLogNotAnnotatedEntity() {
    //when
    auditAspect.logDeleteEvent(new Object());

    //then
    verify(eventService, never()).publish(auditEventCaptor.capture());
  }

  @Test
  void shouldNotLogDeleteEventForDisabledAnnotatedEntity() {
    //given
    ReflectionTestUtils
        .setField(auditAspect, "disableDefaultEvents",
            InheritableThreadLocal.withInitial(() -> Boolean.TRUE));
    //when
    auditAspect.logDeleteEvent(new Object());

    //then
    verify(eventService, never()).publish(auditEventCaptor.capture());
  }

  @Test
  void shouldNotLogSaveEventForDisabledAnnotatedEntity() throws Throwable {
    //given
    var joinPoint = mock(ProceedingJoinPoint.class);
    ReflectionTestUtils
        .setField(auditAspect, "disableDefaultEvents",
            InheritableThreadLocal.withInitial(() -> Boolean.TRUE));

    //when
    auditAspect.logSaveEvent(joinPoint, new Object());

    //then
    verify(eventService, never()).publish(auditEventCaptor.capture());
  }

  private AuditEvent getAuditEvent(String action, boolean disableDefaultEvents) {
    return new AuditEvent() {

      @Override
      public String action() {
        return action;
      }

      @Override
      public boolean disableDefaultEvents() {
        return disableDefaultEvents;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return AuditEvent.class;
      }

    };
  }
}
