package org.luncert.testdatahelper.component;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RunWith(MockitoJUnitRunner.class)
public class RealtimeDataTransportTest {

  @InjectMocks
  private RealtimeDataTransport rdt;

  @Test
  public void successCase() throws ExecutionException, InterruptedException {
    byte[] testData = String.join("\n", Arrays.asList("line1", "line2", "line3", "line4")).getBytes();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(testData);
    String channelId = rdt.registerChannel(inputStream);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Future<Boolean> future = rdt.consumeChannel(channelId, outputStream);
    Assert.assertTrue(future.get());
    Assert.assertArrayEquals(outputStream.toByteArray(), testData);
  }
}
