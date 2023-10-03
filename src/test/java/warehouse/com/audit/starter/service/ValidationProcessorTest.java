package warehouse.com.audit.starter.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import warehouse.com.audit.starter.exception.AuditInitializationException;

@ExtendWith(MockitoExtension.class)
class ValidationProcessorTest {

  private final ValidationProcessor validationProcessor = new ValidationProcessor();

  @ParameterizedTest
  @ValueSource(strings = {
      "warehouse.com.audit.starter.entity.invalid.id",
      "warehouse.com.audit.starter.entity.invalid.name",
      "warehouse.com.audit.starter.entity.invalid.createddate",
      "warehouse.com.audit.starter.entity.invalid.type"})
  public void shouldThrowException(String basePackage) {
    ReflectionTestUtils.setField(validationProcessor, "basePackage", basePackage);
    assertThrows(AuditInitializationException.class,
        () -> validationProcessor.validateRequiredAnnotations());
  }
}
