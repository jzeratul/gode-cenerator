package org.myproject.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SAMPLE")
public class Sample extends BaseEntity {

  @Column(name = "FIRSTNAME")
  private String firstname;

  @Column(name = "LASTNAME")
  private String lastname;

  @Column(name = "SAMPLE_PARENT_ID")
  private Long sampleParentId;
}
