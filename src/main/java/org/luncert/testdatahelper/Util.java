package org.luncert.testdatahelper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Util {

  private Util() {}

  public static String printException(Throwable e) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    e.printStackTrace(new PrintWriter(outputStream));
    return new String(outputStream.toByteArray());
  }

  /**
   * read data from InputStream
   * @return data
   */
  public static byte[] read(InputStream inputStream) throws IOException {
    try (BufferedInputStream buffer = new BufferedInputStream(inputStream);
         DataInputStream dataIn = new DataInputStream(buffer);
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(bos)) {
      byte[] buf = new byte[1024];
      while (true) {
        int len = dataIn.read(buf);
        if (len < 0) {
          break;
        }
        dos.write(buf, 0, len);
      }
      return bos.toByteArray();
    }
  }
}
