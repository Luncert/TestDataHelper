package org.luncert.testdatahelper.controller;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.luncert.testdatahelper.Util;
import org.luncert.testdatahelper.component.ProjectManager;
import org.luncert.testdatahelper.component.RealtimeDataTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;

@RestController("/project/{projectScmUrl}")
public class ProcessController {

  private static final String KEY_PROJECT = "key_project";

  private ProjectManager projectManager;

  private RealtimeDataTransport rdt;

  @Autowired
  public ProcessController(ProjectManager projectManager, RealtimeDataTransport rdt) {
    this.projectManager = projectManager;
    this.rdt = rdt;
  }

  /**
   * Load project
   * @param projectScmUrl String
   * @return channel id
   */
  @PostMapping
  public ResponseEntity<String> prepareProject(String projectScmUrl) {
    // validate project url
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet request = new HttpGet(projectScmUrl);
    try {
      CloseableHttpResponse response = httpClient.execute(request);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != org.apache.http.HttpStatus.SC_OK) {
        return ResponseEntity.badRequest().body("Project SCM server returns " + statusCode + ".");
      }
    } catch (IOException e) {
      return new ResponseEntity<>(Util.printException(e), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // load project
    InputStream inputStream = projectManager.loadProject(projectScmUrl);
    return ResponseEntity.ok(rdt.registerChannel(inputStream));
  }

  @GetMapping("/status")
  public ResponseEntity<String> getStatus(String projectScmUrl) {
    return null;
  }

  @PostMapping("/branch/{branchName}")
  public ResponseEntity<String> changeBranch(String projectScmUrl, String branchName) {
    return null;
  }

  @GetMapping("/source/{className}")
  public ResponseEntity<String> getClassSource(String projectScmUrl, String className) {
    return null;
  }
}
