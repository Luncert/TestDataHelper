package org.luncert.testdatahelper.component;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.luncert.testdatahelper.Util;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class ProjectManager {

  private static final String STORE_PATH = ".appInfo";
  private static final String METADATA_NAME = "metadata";

  private ThreadPoolExecutor executor = new ThreadPoolExecutor(
      1, 8,
      30, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(512),
      new ThreadPoolExecutor.DiscardPolicy());

  private File workspace = new File(STORE_PATH);
  private Map<String, Project> projectMap = new HashMap<>();

  @PostConstruct
  public void afterConstruct() {
    File file = Paths.get(STORE_PATH).toFile();
    if (file.exists()) {
      if (file.isDirectory()) {
        loadMetadata();
      } else {
        if (!file.delete()) {
          log.error("Failed to delete file {}.", file.getAbsolutePath());
        }
        if (!file.mkdir()) {
          log.error("Failed to create application data directory");
        }
      }
    } else {
      if (!file.mkdir()) {
        log.error("Failed to create application data directory");
      }
    }
  }

  private void loadMetadata() {
//    File file = Paths.get(STORE_PATH, METADATA_NAME).toFile();
//    if (!file.exists()) {
//      return;
//    }
//    if (!file.isFile()) {
//      if (file.delete()) {
//        log.error("Failed to delete file {}.", file.getAbsolutePath());
//      }
//    }
//    try {
//      Scanner scanner = new Scanner(new FileInputStream(file));
//      while (scanner.hasNext()) {
//        String line = scanner.nextLine().trim();
//        if (line.length() > 0) {
//          int i = line.lastIndexOf('=');
//          if (i > 0) {
//            metadata.put(line.substring(0, i), line.substring(i + 1));
//          } else {
//            log.error("Invalid line in metadata.");
//          }
//        }
//      }
//      scanner.close();
//    } catch (IOException e) {
//      // impossible exception
//    }
  }

  @PreDestroy
  public void beforeDestroy() throws IOException {
    // save metadata
//    File file = Paths.get(STORE_PATH, METADATA_NAME).toFile();
//    FileOutputStream outputStream = new FileOutputStream(file);
//    PrintStream p = new PrintStream(outputStream);
//    for (Map.Entry<String, String> entry : metadata.entrySet()) {
//      p.write(entry.getKey().getBytes());
//      p.print('=');
//      p.write(entry.getValue().getBytes());
//      p.print('\n');
//    }
//    p.close();
  }

  public synchronized InputStream loadProject(String scmUrl) {
    // if not project found for target SCM url, create one
    // otherwise, try to sync to git remote repository
    DualChannelStream stream = new DualChannelStream(32, true);
    executor.submit(() -> {
      Project project = projectMap.get(scmUrl);
      if (project == null) {
        project = new Project(scmUrl);
        projectMap.put(scmUrl, project);
        try {
          Git git = Git.cloneRepository()
              .setURI(scmUrl)
              .setDirectory(Paths.get(STORE_PATH, project.getId()).toFile())
              .call();
        } catch (GitAPIException e) {
          log.error("clone failed", e);
        }
        // clone project
        if (execute(stream.getWriteStream(), workspace,
            "git", "clone", project.getScmUrl()) != 0) {
          project.setStatus(ProjectStatus.CloneFailed);
        }
      } else {
        // try to sync remote
        if (execute(stream.getWriteStream(), new File(project.getStorePath()),
            "git", "pull") != 0) {
          project.setStatus(ProjectStatus.SyncRemoteFailed);
        }
      }
      stream.close();
    });
    return stream.getReadStream();
  }

  public synchronized InputStream changeProjectBranch(String scmUrl, String branch) {
    Project project = projectMap.get(scmUrl);
    if (project == null) {
      log.error("Failed to checkout branch of nonexistent project {}.", scmUrl);
      return NullInputStream.INSTANCE;
    }
    DualChannelStream stream = new DualChannelStream(32, true);
    executor.submit(() -> {
      if (!project.getCurrentBranch().equals(branch)) {
        if (execute(stream.getWriteStream(), new File(project.getStorePath()),
            "git", "checkout", project.getCurrentBranch()) != 0) {
          project.setStatus(ProjectStatus.CheckoutFailed);
        } else if (!project.getStatus().equals(ProjectStatus.CheckoutFailed)) {
          // resolve project information, after checkout succeed
          resolveProject(project, stream.getWriteStream());
        }
      }
      stream.close();
    });
    return stream.getReadStream();
  }

  private void resolveProject(Project project, OutputStream outputStream) {
    project.setStatus(ProjectStatus.ResolveFailed);
    project.setStatus(ProjectStatus.Ok);
  }

  private int execute(OutputStream outputStream, File workspace, String...commands) {
    ProcessBuilder pb = new ProcessBuilder(commands);
    pb.directory(workspace);
    // combine stdout and stderr
    pb.redirectErrorStream(true);
    try {
      Process process = pb.start();
      copyStream(process.getInputStream(), outputStream);
      if (process.isAlive()) {
        process.waitFor();
      }
      return process.exitValue();
    } catch (IOException | InterruptedException e) {
      log.warn("Failed to execute command.", e);
    }
    return 1;
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

  public synchronized void clearStorage() {

  }
}
