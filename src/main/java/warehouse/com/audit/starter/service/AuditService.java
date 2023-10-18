package warehouse.com.audit.starter.service;

import static net.logstash.logback.argument.StructuredArguments.keyValue;
import static warehouse.com.audit.starter.common.Constants.DEFAULT_INITIATOR_ID;

import com.google.common.collect.Lists;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.stereotype.Service;
import warehouse.com.audit.starter.annotation.AuditableEntity;
import warehouse.com.audit.starter.annotation.AuditableId;
import warehouse.com.audit.starter.annotation.AuditableName;
import warehouse.com.audit.starter.annotation.AuditableType;
import warehouse.com.eventstarter.model.AuditEvent;
import warehouse.com.eventstarter.service.EventService;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuditService {

  private final EventService eventService;

  /**
   * Sends audit event to kafka.
   *
   * @param auditable Auditable entity or collection of entities
   * @param action    Action name
   */
  public void sendAuditEvent(Object auditable, String action) {
    sendAuditEvent(auditable, action, null);
  }

  /**
   * This method has been added to track audit events with a manually specified initiator id.
   *
   * @param auditable   Auditable entity or collection of entities
   * @param action      Action name
   * @param initiatorId override of initiator id
   */
  public void sendAuditEvent(Object auditable, String action, String initiatorId) {
    this.sendAuditEvent(auditable, action, initiatorId, null);
  }

  /**
   * This method has been added to track audit events with a manually specified initiator id and
   * description.
   *
   * @param auditable   Auditable entity or collection of entities
   * @param action      Action name
   * @param initiatorId override of initiator id
   * @param description Action description
   */
  public void sendAuditEvent(Object auditable, String action, String initiatorId,
      String description) {

    getAuditables(auditable)
        .map(entities -> getAuditEvent(entities, action, initiatorId, description))
        .ifPresent(event -> {
          eventService.publish(event);
          log.debug("Audit event published",
              keyValue("entityType", event.getEntityType()),
              keyValue("action", event.getAction()),
              keyValue("eventId", event.getEventId()));
        });
  }

  public Optional<List<Object>> getAuditables(Object auditEntity) {

    return Optional.ofNullable(auditEntity)
        .map(this::getList)
        .filter(this::hasAnyEntitiesAnnotatedByAuditableEntity);
  }

  public AuditableEntity getAnnotation(Object object) {
    return object.getClass().getAnnotation(AuditableEntity.class);
  }

  private List<Object> getList(Object entity) {
    return entity instanceof Iterable
        ? Lists.newArrayList((Iterable<Object>) entity)
        : List.of(entity);
  }

  private AuditEvent getAuditEvent(List<Object> auditables, String action, String initiatorId,
      String description) {
    var entityType = StringUtils.EMPTY.equals(getAnnotation(auditables.get(0)).entityType())
        ? invokeMethodByAnnotation(auditables.get(0), AuditableType.class)
        : getAnnotation(auditables.get(0)).entityType();

    return AuditEvent.builder()
        .action(action)
        .entities(auditables.stream().map(this::getEntity).collect(Collectors.toSet()))
        .entityType(entityType)
        .initiatorId(initiatorId)
        .description(description)
        .timestamp(new Date())
        .build();
  }

  private boolean hasAnyEntitiesAnnotatedByAuditableEntity(List<Object> auditables) {
    return auditables.stream()
        .findFirst()
        .map(auditable -> auditable.getClass()
            .isAnnotationPresent(AuditableEntity.class))
        .orElse(false);
  }

  private AuditEvent.Entity getEntity(Object auditable) {
    return AuditEvent.Entity.builder()
        .id(getFieldValueByAnnotation(auditable, AuditableId.class))
        .name(Optional.ofNullable(getFieldValueByAnnotation(auditable, AuditableName.class))
            .orElseGet(() -> invokeMethodByAnnotation(auditable, AuditableName.class)))
        .build();
  }

  private String invokeMethodByAnnotation(Object object,
      Class<? extends Annotation> annotationType) {
    return MethodUtils.getMethodsListWithAnnotation(object.getClass(), annotationType).stream()
        .findFirst()
        .map(method -> invokeMethod(object, method))
        .map(Object::toString)
        .orElse(null);
  }

  @SneakyThrows
  private Object invokeMethod(Object object, Method method) {
    return method == null ? null : method.invoke(object);
  }

  private String getFieldValueByAnnotation(Object object,
      Class<? extends Annotation> annotationType) {
    return FieldUtils.getFieldsListWithAnnotation(object.getClass(), annotationType).stream()
        .findFirst()
        .map(field -> getFieldValue(object, field))
        .map(Object::toString)
        .orElse(null);
  }

  private Object getFieldValue(Object object, Field field) {
    return getFieldValueByName(object, field.getName());

  }

  @SneakyThrows
  private Object getFieldValueByName(Object object, String name) {
    try {
      return PropertyUtils.getProperty(object, name);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }
}
