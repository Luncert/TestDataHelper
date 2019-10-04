package org.luncert.testdatahelper.component;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.luncert.testdatahelper.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RunWith(JUnit4.class)
public class DualChannelStreamTest {

  @Test
  public void successCase() throws IOException {
    byte[] testData = "This is a test".getBytes();
    DualChannelStream dualChannelStream = new DualChannelStream(2, true);
    OutputStream outputStream = dualChannelStream.getWriteStream();
    InputStream inputStream = dualChannelStream.getReadStream();
    Thread thread1 = new Thread(() -> {
      try {
        for (byte b : testData) {
          outputStream.write(b);
          Thread.sleep(150);
        }
        outputStream.close();
      } catch (IOException | InterruptedException e) {
        Assert.fail(Util.printException(e));
      }
    });
    thread1.start();
    for (byte b : testData) {
      int i = inputStream.read();
      Assert.assertEquals(b, i);
    }
    Assert.assertTrue(dualChannelStream.allDataConsumed());
  }
}
