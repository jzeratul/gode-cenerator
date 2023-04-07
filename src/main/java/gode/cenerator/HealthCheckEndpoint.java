package gode.cenerator;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/healthcheck")
public class HealthCheckEndpoint {

  @Get
  public String healthcheck() {
    return "ok";
  }
}
