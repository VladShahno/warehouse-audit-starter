package warehouse.com.audit.starter.entity.invalid.type;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import warehouse.com.audit.starter.annotation.AuditableEntity;
import warehouse.com.audit.starter.annotation.AuditableId;
import warehouse.com.audit.starter.annotation.AuditableName;

@Data
@Builder
@AuditableEntity
public class TestEntity {

  public static final String TEST_ENTITY_TYPE = "test";
  @AuditableId
  private String id;
  @AuditableName
  private String name;
  @LastModifiedDate
  private Date lastModifiedDate;
  private String subscriptionId;
}
