package warehouse.com.audit.starter.entity.valid;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import warehouse.com.audit.starter.annotation.AuditableEntity;
import warehouse.com.audit.starter.annotation.AuditableId;
import warehouse.com.audit.starter.annotation.AuditableName;

@Data
@Builder
@AuditableEntity(entityType = MethodTestEntity.METHOD_TYPE)
public class MethodTestEntity {

  public static final String METHOD_TYPE = "methodType";
  @AuditableId
  private String id;
  private String name;
  @LastModifiedDate
  private Date lastModifiedDate;
  private String subscriptionId;

  @AuditableName
  public String getEntityName() {
    return name;
  }
}
