package org.luncert.testdatahelper.component;

import java.io.InputStream;

public class NullInputStream extends InputStream {

  public static final InputStream INSTANCE = new NullInputStream();

  private NullInputStream() {}

  @Override
  public int read() {
    return -1;
  }
}
