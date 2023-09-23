package gode.cenerator;

import io.micronaut.http.MediaType;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerWebSocket("/code-generator/new-generation/{username}")
public class CommandsWebSocket {
  private static final Logger log = LoggerFactory.getLogger(CommandsWebSocket.class);

  private final WebSocketBroadcaster broadcaster;

  public CommandsWebSocket(WebSocketBroadcaster broadcaster) {
    this.broadcaster = broadcaster;
  }

  @OnOpen
  public void onOpen(String username, WebSocketSession session) {
    log.info("ws opened for {}", username);
    broadcaster.broadcastSync("ok", MediaType.APPLICATION_JSON_TYPE);
  }

  @OnMessage
  public void onCommand(String username, String command, WebSocketSession session) {
    String msg = "[Received] " + command;
    log.info(msg + " " + session.toString());
    broadcaster.broadcastSync(command, MediaType.APPLICATION_JSON_TYPE);

    for(int i = 0; i< 3; i++) {
      try {
        Thread.sleep(2000);
        broadcaster.broadcastSync(command, MediaType.APPLICATION_JSON_TYPE);
      } catch (InterruptedException ex) {
        log.error("Error ", ex);
      }
    }
  }

  @OnClose
  public void onClose(String username, WebSocketSession session) {
    String msg = "Disconnected!";
    log.info(msg);
    broadcaster.broadcastSync(msg);
  }
}