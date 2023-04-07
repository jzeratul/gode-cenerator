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

import java.util.function.Predicate;

@ServerWebSocket("/chat/{topic}/{username}") // (1)
public class ChatServerWebSocket {
  private static final Logger LOG = LoggerFactory.getLogger(ChatServerWebSocket.class);

  private final WebSocketBroadcaster broadcaster;

  public ChatServerWebSocket(WebSocketBroadcaster broadcaster) {
    this.broadcaster = broadcaster;
  }

  @OnOpen
  public void onOpen(String topic, String username, WebSocketSession session) {
    String msg = "[" + username + "] Joined!";
    LOG.info(msg);
    LOG.info(session.getId());
    broadcaster.broadcastSync(msg, MediaType.APPLICATION_JSON_TYPE, isValid(topic, session));
  }

  @OnMessage // (3)
  public void onMessage(String topic, String username,
                        String message, WebSocketSession session) {
    String msg = "[" + username + "] " + message;
    LOG.info(msg + "<<<<<< onMessage");
    Predicate<WebSocketSession> valid = isValid(topic, session);
    broadcaster.broadcastSync(msg, MediaType.APPLICATION_JSON_TYPE, valid);
  }

  @OnClose // (5)
  public void onClose(String topic, String username, WebSocketSession session) {
    String msg = "[" + username + "] Disconnected!";
    broadcaster.broadcastSync(msg, isValid(topic, session));
  }

  private Predicate<WebSocketSession> isValid(String topic, WebSocketSession session) {
    Predicate<WebSocketSession> predicate = s -> {
      boolean b = s == session;
      boolean topic1 = topic.equalsIgnoreCase(s.getUriVariables().get("topic", String.class, null));
      boolean topic2 = b && topic1;
      LOG.info("Session {} s.getUri {} Topic {} return {}", b, s.getUriVariables().asMap(), topic, topic2);
      return topic2;
    };
    return predicate;
  }
}