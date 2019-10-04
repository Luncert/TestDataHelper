package org.luncert.testdatahelper.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.OutputStream;

@ServerEndpoint("/realtimeDataChannel/{channelId}")
@Component
@Slf4j
public class WebSocketServer {

  @Autowired
  private RealtimeDataTransport transport;

  private Session session;

  @OnOpen
  public void onOpen(Session session, @PathParam("channelId") String channelId) throws IOException {
    this.session = session;
    OutputStream outputStream = session.getBasicRemote().getSendStream();
    transport.consumeChannel(channelId, outputStream);
  }

  @OnClose
  public void onClose() {
    this.session = null;
  }

  @OnError
  public void onError(Session session, Throwable error) {
    log.warn("WebSocket error", error);
  }
}
