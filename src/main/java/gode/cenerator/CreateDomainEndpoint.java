package gode.cenerator;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller("/create")
public class CreateDomainEndpoint {

  @Post
  public String hello(String createCommand) {
    System.out.println(createCommand);
    return "ok";
  }
}