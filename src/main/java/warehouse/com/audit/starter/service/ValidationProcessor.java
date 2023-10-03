package warehouse.com.audit.starter.service;

import jakarta.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.LastModifiedDate;
import warehouse.com.audit.starter.annotation.AuditableEntity;
import warehouse.com.audit.starter.annotation.AuditableId;
import warehouse.com.audit.starter.annotation.AuditableName;
import warehouse.com.audit.starter.annotation.AuditableType;
import warehouse.com.audit.starter.exception.AuditInitializationException;

/**
 * Validates that {@link AuditableEntity} has fields and methods annotated with {@link AuditableId},
 * {@link AuditableName}, {@link LastModifiedDate}
 */
public class ValidationProcessor {

  @Value("${application.audit.basePackage:warehouse.com}")
  private String basePackage;

  @SneakyThrows
  @PostConstruct
  public void validateRequiredAnnotations() {
    var provider = new ClassPathScanningCandidateComponentProvider(false);
    provider.addIncludeFilter(new AnnotationTypeFilter(AuditableEntity.class));
    var candidateComponents = provider.findCandidateComponents(basePackage);
    for (BeanDefinition candidateComponent : candidateComponents) {
      var aClass = Class.forName(candidateComponent.getBeanClassName());

      validateAnnotatedField(aClass, AuditableId.class);
      validateAnnotatedField(aClass, LastModifiedDate.class);
      validateAnnotatedFieldOrMethod(aClass, AuditableName.class);

      if (isEntityTypeEmpty(aClass)) {
        validateAnnotatedMethod(aClass, AuditableType.class);
      }
    }
  }

  private boolean isEntityTypeEmpty(Class<?> aClass) {
    return StringUtils.EMPTY.equals(aClass.getAnnotation(AuditableEntity.class).entityType());
  }

  private void validateAnnotatedField(Class<?> aClass, Class<? extends Annotation> annotation) {
    if (isAnnotationAbsentOnField(aClass, annotation)) {
      throw new AuditInitializationException(getErrorText(annotation, aClass));
    }
  }

  private void validateAnnotatedMethod(Class<?> aClass, Class<? extends Annotation> annotation) {
    if (isAnnotationAbsentOnMethod(aClass, annotation)) {
      throw new AuditInitializationException(getErrorText(annotation, aClass));
    }
  }

  private void validateAnnotatedFieldOrMethod(Class<?> aClass,
      Class<? extends Annotation> annotation) {
    if (isAnnotationAbsentOnField(aClass, annotation) && isAnnotationAbsentOnMethod(aClass,
        annotation)) {
      throw new AuditInitializationException(getErrorText(annotation, aClass));
    }
  }

  private String getErrorText(Class<? extends Annotation> annotation, Class<?> aClass) {
    return String
        .format("Annotation %s is required for %s entity, see %s java doc", annotation.getName(),
            aClass.getName(),
            AuditableEntity.class.getName());
  }

  private boolean isAnnotationAbsentOnMethod(Class<?> aClass,
      Class<? extends Annotation> annotation) {
    return MethodUtils.getMethodsListWithAnnotation(aClass, annotation).isEmpty();
  }

  private boolean isAnnotationAbsentOnField(Class<?> aClass,
      Class<? extends Annotation> annotation) {
    return FieldUtils.getFieldsListWithAnnotation(aClass, annotation).isEmpty();
  }
}

