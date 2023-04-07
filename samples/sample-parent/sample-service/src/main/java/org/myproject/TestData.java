package org.myproject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myproject.entities.Sample;
import org.myproject.entities.SampleParent;
import org.myproject.repositories.SampleParentRepository;
import org.myproject.repositories.SampleRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@Profile("test-data")
@RequiredArgsConstructor
public class TestData {

  private final SampleRepository sampleRepository;
  private final SampleParentRepository sampleParentRepository;

  @PostConstruct
  public void setup() {
    for (int i = 0; i < 5; i++) {
      SampleParent sampleParent = saveSampleParent(i);
      Sample sample = saveSample(sampleParent, i);
    }
  }

    private SampleParent saveSampleParent(int i) {

      SampleParent parent = SampleParent.builder()
              .firstname("firstname " + i)
              .lastname("lastname " + i)
              .active(Boolean.TRUE)
              .createdAt(LocalDateTime.now())
              .uuid(UUID.randomUUID())
              .status("STATUS")
              .updatedAt(LocalDateTime.now())
              .build();
      SampleParent savedSampleParent = sampleParentRepository.save(parent);
      log.info("Saved sampleParent {}", savedSampleParent);
      return savedSampleParent;
    }

    private Sample saveSample(SampleParent sampleParent, int i) {

      Sample sample = Sample.builder()
              .firstname("firstname " + i)
              .lastname("lastname " + i)
              .active(Boolean.TRUE)
              .createdAt(LocalDateTime.now())
              .uuid(UUID.randomUUID())
              .status("STATUS")
              .updatedAt(LocalDateTime.now())
              .sampleParentId(sampleParent.getId())
              .build();

      Sample savedSample = sampleRepository.save(sample);

      log.info("Saved sample {}", savedSample);
      return savedSample;
    }
}
