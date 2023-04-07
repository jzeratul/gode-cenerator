package gode.cenerator;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.time.LocalDateTime;

@Controller("/healthcheck")
public class HealthCheckEndpoint {

  @Get
  public String healthcheck() {
    System.out.println("Status check " + LocalDateTime.now());
    return "ok";
  }
}
