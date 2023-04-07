package org.myproject.repositories;

import org.myproject.entities.Sample;
import org.springframework.data.repository.CrudRepository;

public interface SampleRepository extends CrudRepository<Sample, Long> {

}
