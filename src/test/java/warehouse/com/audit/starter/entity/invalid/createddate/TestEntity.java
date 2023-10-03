package warehouse.com.audit.starter.entity.invalid.createddate;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import warehouse.com.audit.starter.annotation.AuditableEntity;
import warehouse.com.audit.starter.annotation.AuditableId;
import warehouse.com.audit.starter.annotation.AuditableName;

@Data
@Builder
@AuditableEntity(entityType = TestEntity.TEST_ENTITY_TYPE)
public class TestEntity {

  public static final String TEST_ENTITY_TYPE = "test";
  @AuditableId
  private String id;
  @AuditableName
  private String name;
  private Date createdDate;
  private String subscriptionId;
}
