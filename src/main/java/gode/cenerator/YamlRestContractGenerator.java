package gode.cenerator;

import io.micronaut.websocket.WebSocketBroadcaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static io.micronaut.http.MediaType.TEXT_PLAIN_TYPE;

public class YamlRestContractGenerator {

  public static void main(String[] args) throws Exception {

    List<String> modelInput = new ArrayList<>();
    modelInput.add("User id firstname lastname birthdate address");
    modelInput.add("Company id firstname lastname birthdate address");
    modelInput.add("Action id name");
    modelInput.add("Address id street city country postalcode");

    YamlRestContractGenerator y = new YamlRestContractGenerator();
    try (Stream<Path> paths = Files.walk(Paths.get("/Users/KC36IK/ws-me/gode-cenerator/samples"))) {
      paths.filter(Files::isRegularFile)
        .filter(y::skip)
        .filter(y::pickThisExtension)
        .forEach(yml -> {
          try {
            y.parse(yml, null, modelInput);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    }
  }

  private boolean skip(Path x) {
    String string = x.toString();
    return
      !string.contains("node_modules") &&
        !string.contains("target");
  }

  private boolean pickThisExtension(Path x) {
    int dotIndex = x.toString().lastIndexOf(".");
    if (dotIndex < 1) {
      return false;
    }
    String extension = x.toString().substring(dotIndex + 1);
    return extension.equals("yml") || extension.equals("yaml");
  }

  boolean isopenapi = false;
  boolean isdocker = false;
  boolean isconfig = false;
  boolean paths = false;
  boolean components = false;
  boolean schemas = false;

  private void parse(Path fileOrFolder, WebSocketBroadcaster broadcaster, List<String> modelInput) throws Exception {

    isconfig = fileOrFolder.getFileName().toString().startsWith("application");
    isdocker = fileOrFolder.getFileName().toString().startsWith("docker-compose");
    isopenapi = !isconfig && !isdocker && fileOrFolder.getFileName().toString().contains("contract");

    String x = isconfig ? "config" : isopenapi ? "contracts" : isdocker ? "docker" : "unknown";

    System.out.println("Parsing " + fileOrFolder + " " + x);

    Path root = Path.of("MyProject");

    File file = root.toFile();
    if (file.exists()) {
      boolean deleted = file.delete();
      if (!deleted) {
        System.out.println("cannot delete " + root);
      } else {
        Files.createDirectory(root);
      }
    } else {
      Files.createDirectory(root);
    }

    Path path = Path.of(fileOrFolder.toString().substring(fileOrFolder.toString().indexOf("sample-parent")));
    System.out.println(path);
    Files.createDirectories(path);


    if (isopenapi) {
      try (InputStream is = new FileInputStream(fileOrFolder.toFile())) {
        BufferedReader read = new BufferedReader(new InputStreamReader(is));

        String line;
        MyYml myYml = new MyYml();

        boolean foundOpenapiTag = false;
        boolean foundTags = false;
        boolean foundPaths = false;
        boolean foundComponents = false;
        boolean foundSchemas = false;
        boolean finishedTags = false; // to allow this tag elsewhere too
        boolean lastUrlIsSample = false;

        while ((line = read.readLine()) != null) {

          if (!foundOpenapiTag) {
            foundOpenapiTag = line.startsWith("openapi: ");
            if (!foundOpenapiTag) {
              continue;
            }
          }

          if (!foundTags) {
            foundTags = line.startsWith("tags:");
            if (!foundTags) {
              // continue to build the header
              myYml.header.header.append("\n").append(line);
              continue;
            }
          }

          genericMessage(broadcaster, "Generic " + myYml.header.header.toString());

          if (!foundPaths && !finishedTags) {
            foundPaths = line.startsWith("paths:");
            if (!foundPaths) {
              myYml.header.tags.append("\n").append(line);
              if(line.contains("sample")) {
                // duplicate this line for all models
                String finalLine = line;
                modelInput.forEach(
                  m -> {
                    String[] splits = m.split(" ", 2);
                    String modelName = splits[0].toLowerCase();
                    myYml.header.tags.append("\n").append(finalLine.replaceAll("sample", modelName));
                  }
                );
              }
              continue;
            }
          }

          finishedTags = true;
          genericMessage(broadcaster, "Generic " + myYml.header.header.toString());

          if (!foundComponents) {
            foundComponents = line.startsWith("components:");
            if (!foundComponents) {
              // is this a new endpoints?
              if (line.trim().startsWith("/")) {

                fillEndpointsWithModels(modelInput, myYml, lastUrlIsSample);

                lastUrlIsSample = line.contains("sample");
                myYml.endpoints.endpoints.add(new StringBuilder("\n").append(line));
              } else {
                if (myYml.endpoints.endpoints.isEmpty()) {
                  myYml.endpoints.endpoints.add(new StringBuilder());
                }
                myYml.endpoints.endpoints.getLast().append("\n").append(line);
              }
              continue;
            }
            fillEndpointsWithModels(modelInput, myYml, lastUrlIsSample);
            lastUrlIsSample = false;
          }
        }

        genericMessage(broadcaster, "Generic " + myYml.endpoints.endpoints.toString());

        // let's print what we did

        System.out.println("\n =======================================================");
        System.out.println(myYml.header.header);
        System.out.println(myYml.header.tags);
        System.out.println(myYml.endpoints.endpoints);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static void fillEndpointsWithModels(List<String> modelInput, MyYml myYml, boolean lastUrlIsSample) {
    if(!myYml.endpoints.endpoints.isEmpty() && lastUrlIsSample) {
      String sampleModel = myYml.endpoints.endpoints.getLast().toString();

      modelInput.forEach(
        m -> {
          String[] splits = m.split(" ", 2);
          String modelName = splits[0];
          StringBuilder newSb = new StringBuilder(sampleModel.replaceAll("sample", modelName.toLowerCase()).replaceAll("Sample", modelName));
          myYml.endpoints.endpoints.add(newSb);
        }
      );
    }
  }

  private static void genericMessage(WebSocketBroadcaster broadcaster, String message) {
    if (broadcaster != null) {
      broadcaster.broadcastSync(message, TEXT_PLAIN_TYPE);
    }
  }
}

class Header {
  StringBuilder header = new StringBuilder();
  StringBuilder tags = new StringBuilder();
}

class Endpoints {
  LinkedList<StringBuilder> endpoints = new LinkedList<>();

}

class Components {
  StringBuilder schemas = new StringBuilder();
}

class Schemas {
  LinkedList<StringBuilder> objects = new LinkedList<>();
}

class MyYml {
  Header header = new Header();
  Endpoints endpoints = new Endpoints();
  Components components = new Components();
}