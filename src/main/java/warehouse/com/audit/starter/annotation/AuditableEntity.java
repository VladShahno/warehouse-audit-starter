package warehouse.com.audit.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If the annotation {@code @AuditableEntity} is present on the declaration
 * of a class then entity is audited.
 * {@link AuditableId}, {@link AuditableName} and {@link org.springframework.data.annotation.LastModifiedDate}
 * are required to build proper audit event
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AuditableEntity {

  /**
   * Entity type. For example asset, user, device etc
   */
  String entityType() default "";

  String createEvent() default "created";

  String deleteEvent() default "deleted";

  String updateEvent() default "updated";
}
