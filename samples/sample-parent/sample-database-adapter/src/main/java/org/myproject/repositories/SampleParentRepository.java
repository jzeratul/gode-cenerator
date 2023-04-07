package org.myproject.repositories;

import org.myproject.entities.SampleParent;
import org.springframework.data.repository.CrudRepository;

public interface SampleParentRepository extends CrudRepository<SampleParent, Long> {

}
