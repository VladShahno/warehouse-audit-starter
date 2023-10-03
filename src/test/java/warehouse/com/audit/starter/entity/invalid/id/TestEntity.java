package warehouse.com.audit.starter.entity.invalid.id;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import warehouse.com.audit.starter.annotation.AuditableEntity;
import warehouse.com.audit.starter.annotation.AuditableName;

@Data
@Builder
@AuditableEntity(entityType = TestEntity.TEST_ENTITY_TYPE)
public class TestEntity {

  public static final String TEST_ENTITY_TYPE = "test";
  private String id;
  @AuditableName
  private String name;
  @LastModifiedDate
  private Date lastModifiedDate;
  private String subscriptionId;
}
