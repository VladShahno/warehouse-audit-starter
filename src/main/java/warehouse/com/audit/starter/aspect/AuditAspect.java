package warehouse.com.audit.starter.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import warehouse.com.audit.starter.annotation.AuditEvent;
import warehouse.com.audit.starter.service.AuditService;

@Aspect
@RequiredArgsConstructor
public class AuditAspect {

  private final AuditService auditService;

  private ThreadLocal<Boolean> disableDefaultEvents = InheritableThreadLocal.withInitial(() -> Boolean.FALSE);

  @Around(value = "@annotation(auditEvent)", argNames = "pjp,auditEvent")
  public Object logAuditEvent(ProceedingJoinPoint pjp, AuditEvent auditEvent) throws Throwable {
    disableDefaultEvents.set(auditEvent.disableDefaultEvents());
    try {
      if (StringUtils.isEmpty(auditEvent.action())) {
        return pjp.proceed();
      }

      var annotatedArgument = auditService.getAuditables(Arrays.asList(pjp.getArgs()));
      if (annotatedArgument.isPresent()) {
        return proceedWithArgumentValue(pjp, auditEvent, annotatedArgument.get());
      }
      return proceedWithReturnObject(pjp, auditEvent);
    } finally {
      disableDefaultEvents.remove();
    }
  }

  @Around(value = "(execution(* org.springframework.data.repository.CrudRepository.save(..)) " +
      "|| execution(* org.springframework.data.repository.CrudRepository.saveAll(..))" +
      "|| execution(* org.springframework.data.*.repository.JpaRepository.saveAndFlush(..)))" +
      " && args(auditEntity)", argNames = "pjp,auditEntity")
  public Object logSaveEvent(ProceedingJoinPoint pjp, Object auditEntity) throws Throwable {

    if (disableDefaultEvents.get()) {
      return pjp.proceed();
    }

    var auditables = auditService.getAuditables(auditEntity);
    if (auditables.isPresent()) {
      String action = isNew(auditables.get())
          ? auditService.getAnnotation(auditables.get().get(0)).createEvent()
          : auditService.getAnnotation(auditables.get().get(0)).updateEvent();
      var proceed = pjp.proceed();
      auditService.sendAuditEvent(auditables.get(), action);
      return proceed;
    }

    return pjp.proceed();
  }

  @After(value = "(execution(* org.springframework.data.repository.CrudRepository.delete(..)) " +
      "|| execution(* org.springframework.data.repository.CrudRepository.deleteAll(..)))" +
      " && args(auditEntity)")
  public void logDeleteEvent(Object auditEntity) {

    if (!disableDefaultEvents.get()) {
      Optional<List<Object>> auditables = auditService.getAuditables(auditEntity);
      auditables.ifPresent(entities -> {
        var action = auditService.getAnnotation(entities.get(0)).deleteEvent();
        auditService.sendAuditEvent(entities, action);
      });
    }
  }

  private boolean isNew(List<Object> auditEntities) {

    return auditEntities
        .stream()
        .findFirst()
        .map(auditable -> hasAnyNullFieldsAnnotatedBy(auditable, List.of(LastModifiedDate.class, Id.class)))
        .orElse(false);
  }

  private boolean hasAnyNullFieldsAnnotatedBy(Object object, List<Class<? extends Annotation>> annotations) {
    return annotations.stream()
        .map(annotation -> getField(object, annotation))
        .filter(Objects::nonNull)
        .map(field -> getFieldValue(object, field))
        .anyMatch(Objects::isNull);
  }

  private Field getField(Object object, Class<? extends Annotation> annotation) {
    var fieldsListWithAnnotation = FieldUtils.getFieldsListWithAnnotation(object.getClass(), annotation);
    return fieldsListWithAnnotation.isEmpty() ? null : fieldsListWithAnnotation.get(0);
  }

  @SneakyThrows
  private Object getFieldValue(Object object, Field field) {
    return PropertyUtils.getProperty(object, field.getName());
  }

  private Object proceedWithArgumentValue(ProceedingJoinPoint pjp, AuditEvent auditEvent, List<Object> auditables)
      throws Throwable {
    var retVal = pjp.proceed();
    auditService.sendAuditEvent(auditables, auditEvent.action());
    return retVal;
  }

  private Object proceedWithReturnObject(ProceedingJoinPoint pjp, AuditEvent auditEvent) throws Throwable {
    var proceed = pjp.proceed();
    auditService.getAuditables(proceed).
        ifPresent(auditables -> auditService.sendAuditEvent(auditables, auditEvent.action()));
    return proceed;
  }
}
