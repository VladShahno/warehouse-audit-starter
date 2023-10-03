package warehouse.com.audit.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used on the method level. The method has to have an entity marked with {@code AuditableEntity}
 * as an argument or return it.
 * <p>  It is used to log activity events with a custom action. </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditEvent {
  String action() default "";

  /**
   * Disables default events such as 'created', 'updated', 'deleted'
   */
  boolean disableDefaultEvents() default true;
}
