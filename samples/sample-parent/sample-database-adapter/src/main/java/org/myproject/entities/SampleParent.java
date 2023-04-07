package org.myproject.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SAMPLE_PARENT")
public class SampleParent extends BaseEntity {

  @Column(name = "FIRSTNAME")
  private String firstname;

  @Column(name = "LASTNAME")
  private String lastname;
}
