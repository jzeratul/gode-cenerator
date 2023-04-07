package org.myproject.endpoints;

import lombok.RequiredArgsConstructor;
import org.myproject.mappers.SampleMapper;
import org.myproject.mappers.SampleParentMapper;
import org.myproject.repositories.SampleParentRepository;
import org.myproject.repositories.SampleRepository;
import org.myproject.rest.api.SampleServiceApi;
import org.myproject.rest.model.SampleParentsDTO;
import org.myproject.rest.model.SamplesDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Endpoints implements SampleServiceApi {

  private final SampleParentRepository repository;
  private final SampleParentMapper mapper;

  private final SampleRepository sampleRepository;
  private final SampleMapper sampleMapper;

  @Override
  public ResponseEntity<SampleParentsDTO> getParentSamples() throws Exception {
    return ResponseEntity.ok(mapper.map(repository.findAll()));
  }

  @Override
  public ResponseEntity<SamplesDTO> getSamples() throws Exception {
    return ResponseEntity.ok(sampleMapper.map(sampleRepository.findAll()));
  }
}