package warehouse.com.audit.starter.exception;

import warehouse.com.reststarter.exception.CustomRuntimeException;

public class AuditInitializationException extends CustomRuntimeException {

  public AuditInitializationException(String message) {
    super(message);
  }
}
