package org.luncert.testdatahelper.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Scope(value = "singleton")
@Slf4j
public class RealtimeDataTransport {

  private Map<String, InputStream> channels = new ConcurrentHashMap<>();
  private ThreadPoolExecutor executor = new ThreadPoolExecutor(
      1, 8,
      30, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(512),
      new ThreadPoolExecutor.DiscardPolicy());

  /**
   * Register InputStream as new channel.
   * @param inputStream InputStream
   * @return channel id
   */
  public String registerChannel(InputStream inputStream) {
    String id = UUID.randomUUID().toString();
    channels.put(id, inputStream);
    return id;
  }

  /**
   * Consume channel.
   * @param channelId channel id
   * @param outputStream OutputStream
   * @return Future
   */
  public Future<Boolean> consumeChannel(String channelId, OutputStream outputStream) {
    Objects.requireNonNull(outputStream, "parameter outputStream must be non-null");
    if (!channels.containsKey(channelId)) {
      throw new IllegalArgumentException("invalid channel id " + channelId);
    }
    InputStream inputStream = channels.remove(channelId);
    return executor.submit(() -> {
      byte[] buf = new byte[1024];
      try {
        int n;
        while (true) {
          n = inputStream.read(buf);
          if (n < 0) {
            break;
          } else if (n == 0) {
            // Waiting for data
            Thread.sleep(100);
          } else {
            outputStream.write(buf, 0, n);
          }
        }
        outputStream.close();
        return true;
      } catch (IOException e) {
        log.warn("Exception on reading or writing data", e);
      } catch (InterruptedException e) {
        log.warn("Exception on waiting for data", e);
      }
      return false;
    });
  }
}
