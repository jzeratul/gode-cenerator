package org.myproject.mappers;

import org.myproject.entities.Sample;
import org.myproject.rest.model.SampleDTO;
import org.myproject.rest.model.SamplesDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SampleMapper {

  public SamplesDTO map(Iterable<Sample> all) {
    return SamplesDTO.builder()
            .items(mapSamples(all))
            .build();
  }

  private List<SampleDTO> mapSamples(Iterable<Sample> all) {

    List<SampleDTO> results = new ArrayList<>();
    for (Sample sp : all) {
      results.add(copy(sp));
    }
    return results;
  }

  private SampleDTO copy(Sample sp) {
    return SampleDTO.builder()
            .firstname(sp.getFirstname())
            .lastname(sp.getLastname())
            .id(sp.getId())
            .build();
  }
}
