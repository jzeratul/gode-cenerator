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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.micronaut.http.MediaType.TEXT_PLAIN_TYPE;

public class YamlRestContractGenerator {

  public static void main(String[] args) {

    List<String> modelInput = new ArrayList<>();
    modelInput.add("User id firstname lastname birthdate address");
    modelInput.add("Company id firstname lastname birthdate address");
    modelInput.add("Action id name");
    modelInput.add("Address id street city country postalcode");

    YamlRestContractGenerator y = new YamlRestContractGenerator();
    y.generateFromSample("/Users/KC36IK/ws-me/gode-cenerator/samples", modelInput, null);
  }

  public void generateFromSample(String samplesParentPath, List<String> modelInput, WebSocketBroadcaster broadcaster) {
    try {
      try (Stream<Path> paths = Files.walk(Paths.get(samplesParentPath))) {
        paths.filter(Files::isRegularFile)
          .filter(this::skip)
          .filter(this::pickThisExtension)
          .forEach(yml -> {
            try {
              this.parse(yml, broadcaster, modelInput);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
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

  public void parse(Path fileOrFolder, WebSocketBroadcaster broadcaster, List<String> modelInput) throws Exception {

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
        MyYml myYml = new MyYml(new StringBuilder(), new StringBuilder(), new LinkedList<StringBuilder>(), new StringBuilder());

        boolean foundOpenapiTag = false;
        boolean foundTags = false;
        boolean foundPaths = false;
        boolean foundComponents = false;
        boolean lastUrlIsSample = false;

        boolean sentHeaders = false;
        boolean sentTags = false;
        boolean sentEndpoints = false;

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
              myYml.header().append("\n").append(line);
              continue;
            }
          }

          if(!sentHeaders) {
            genericMessage(broadcaster, "Generic " + myYml.header().toString());
            sentHeaders = true;
          }

          if (!foundPaths) {
            foundPaths = line.startsWith("paths:");
            if (!foundPaths) {
              myYml.tags().append("\n").append(line);
              if (line.contains("sample")) {
                // duplicate this line for all models
                String finalLine = line;
                modelInput.forEach(
                  m -> {
                    String[] splits = m.split(" ", 2);
                    String modelName = splits[0].toLowerCase();
                    String plural = plural(modelName);
                    myYml.tags().append("\n").append(finalLine
                      .replaceAll("samples", plural)
                      .replaceAll("sample", modelName)
                    );
                  }
                );
              }
              continue;
            }
          }

          if(!sentTags) {
            genericMessage(broadcaster, "Generic " + myYml.tags());
            sentTags = true;
          }

          if (!foundComponents) {
            foundComponents = line.startsWith("components:");
            if (!foundComponents) {
              // is this a new endpoints?
              if (line.trim().startsWith("/")) {

                fillEndpointsWithModels(modelInput, myYml, lastUrlIsSample);

                lastUrlIsSample = line.contains("sample");
                myYml.endpoints().add(new StringBuilder("\n").append(line));
              } else {
                if (myYml.endpoints().isEmpty()) {
                  myYml.endpoints().add(new StringBuilder());
                }
                myYml.endpoints().getLast().append("\n").append(line);
              }
              continue;
            }
            fillEndpointsWithModels(modelInput, myYml, lastUrlIsSample);
            lastUrlIsSample = false;
            myYml.schemas().append(line).append("\n");
            continue;
          }
          myYml.schemas().append(line).append("\n");

          if(!sentEndpoints) {
            genericMessage(broadcaster, "Generic " + String.join("", myYml.endpoints()));
            sentEndpoints = true;
          }
        }

        String space = "  ";
        String newLine = "\n";
        modelInput.forEach(
          m -> {
            String[] splits = m.split(" ");
            System.out.println(splits);
            String modelName = splits[0];
            String plural = plural(modelName);
            StringBuilder sb = new StringBuilder(space).append(plural).append(newLine)
              .append(space).append(space).append("type: object").append(newLine)
              .append(space).append(space).append("properties:").append(newLine)
              .append(space).append(space).append(space).append("items:").append(newLine)
              .append(space).append(space).append(space).append(space).append("type: array").append(newLine)
              .append(space).append(space).append(space).append(space).append("items:").append(newLine)
              .append(space).append(space).append(space).append(space).append(space).append("$ref: '#/components/schemas/" + modelName + "'").append(newLine)
              .append(space).append(modelName).append(newLine)
              .append(space).append(space).append("type: object").append(newLine)
              .append(space).append(space).append("properties:").append(newLine);

            if (splits.length > 1) {
              for (int i = 1; i < splits.length; i++) {
                String attribute = splits[i];
                if (attribute.toLowerCase().contains("datetime")) {
                  sb.append(space).append(space).append(space).append(attribute).append(newLine)
                    .append(space).append(space).append(space).append(space).append("type: string").append(newLine)
                    .append(space).append(space).append(space).append(space).append("format: date-time").append(newLine)
                  ;
                } else if (attribute.toLowerCase().contains("date")) {
                  sb.append(space).append(space).append(space).append(attribute).append(newLine)
                    .append(space).append(space).append(space).append(space).append("type: string").append(newLine)
                    .append(space).append(space).append(space).append(space).append("format: date").append(newLine)
                  ;
                } else if (attribute.toLowerCase().contains("id")) {
                  sb.append(space).append(space).append(space).append(attribute).append(newLine)
                    .append(space).append(space).append(space).append(space).append("type: integer").append(newLine)
                    .append(space).append(space).append(space).append(space).append("format: int64").append(newLine)
                  ;
                } else if (attribute.toLowerCase().contains("number") || attribute.toLowerCase().contains("nr")) {
                  sb.append(space).append(space).append(space).append(attribute).append(newLine)
                    .append(space).append(space).append(space).append(space).append("type: integer").append(newLine)
                    .append(space).append(space).append(space).append(space).append("format: int64").append(newLine)
                  ;
                } else {
                  sb.append(space).append(space).append(space).append(attribute).append(newLine)
                    .append(space).append(space).append(space).append(space).append("type: string").append(newLine)
                    .append(space).append(space).append(space).append(space).append("minLength: 2").append(newLine)
                    .append(space).append(space).append(space).append(space).append("maxLength: 60").append(newLine)
                  ;
                }
              }
            }
            myYml.schemas().append(sb);
          }
        );

        genericMessage(broadcaster, "Generic " + myYml.schemas());

        // let's print what we did

        System.out.println("\n =======================================================");
        System.out.println(myYml.header());
        System.out.println(myYml.tags());
        System.out.println(myYml.endpoints());
        System.out.println(myYml.schemas());

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private String plural(String modelName) {
    if (modelName.charAt(modelName.length() - 1) == 'y') {
      return modelName.substring(0, modelName.length() - 1) + "ies";
    }
    if (modelName.charAt(modelName.length() - 1) == 's') {
      return modelName + "es";
    }
    return modelName + "s";
  }

  private void fillEndpointsWithModels(List<String> modelInput, MyYml myYml, boolean lastUrlIsSample) {
    if (!myYml.endpoints().isEmpty() && lastUrlIsSample) {
      String sampleModel = myYml.endpoints().getLast().toString();

      modelInput.forEach(
        m -> {
          String[] splits = m.split(" ", 2);
          String modelName = splits[0];
          String plural = plural(modelName);
          StringBuilder newSb = new StringBuilder(
            sampleModel
              .replaceAll("samples", plural.toLowerCase())
              .replaceAll("Samples", plural)
              .replaceAll("sample", modelName.toLowerCase())
              .replaceAll("Sample", modelName));
          myYml.endpoints().add(newSb);
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


record MyYml(StringBuilder header, StringBuilder tags, LinkedList<StringBuilder> endpoints, StringBuilder schemas) {
}