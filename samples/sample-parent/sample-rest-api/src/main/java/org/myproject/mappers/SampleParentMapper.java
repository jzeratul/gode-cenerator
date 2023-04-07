package org.myproject.mappers;

import org.myproject.entities.SampleParent;
import org.myproject.rest.model.SampleParentDTO;
import org.myproject.rest.model.SampleParentsDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SampleParentMapper {

  public SampleParentsDTO map(Iterable<SampleParent> all) {
    return SampleParentsDTO.builder()
            .items(mapSampleParents(all))
            .build();
  }

  private List<SampleParentDTO> mapSampleParents(Iterable<SampleParent> all) {

    List<SampleParentDTO> results = new ArrayList<>();
    for (SampleParent sp : all) {
      results.add(copy(sp));
    }
    return results;
  }

  private SampleParentDTO copy(SampleParent sp) {
    return SampleParentDTO.builder()
            .firstname(sp.getFirstname())
            .lastname(sp.getLastname())
            .id(sp.getId())
            .build();
  }
}
