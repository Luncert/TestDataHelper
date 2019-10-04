package org.luncert.testdatahelper.component;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.luncert.testdatahelper.Util;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RunWith(MockitoJUnitRunner.class)
public class ProjectManagerTest {

  @InjectMocks
  private ProjectManager projectManager;

  @Test
  public void testLoadProject() throws IOException {
    InputStream inputStream = projectManager.loadProject("https://github.com/Luncert/LeetCodePractice");
    byte[] buf = new byte[1024];
    while (true) {
      int len = inputStream.read(buf);
      if (len < 0) {
        break;
      }
      System.out.write(buf, 0, len);
    }
  }

  @Test
  public void test() throws IOException, InterruptedException {
//    ProcessBuilder pb = new ProcessBuilder("git", "clone", "https://github.com/Luncert/LeetCodePractice");
    ProcessBuilder pb = new ProcessBuilder("python", "test.py");
    pb.directory(new File("../tmp"));
    pb.redirectErrorStream(true);
    Process process = pb.start();
    copyStream(process.getInputStream(), System.out);
    if (process.isAlive()) {
      process.waitFor();
    }
  }

  private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
    byte[] buf = new byte[1024];
    while (true) {
      int len = inputStream.read(buf, 0, 1024);
      if (len == -1) {
        break;
      }
      outputStream.write(buf, 0, len);
    }
  }
}
