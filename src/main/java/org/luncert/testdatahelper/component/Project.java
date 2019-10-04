package org.luncert.testdatahelper.component;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class Project {

  private String id;
  private String scmUrl;
  private String storePath;
  private String currentBranch;
  private Set<String> branches = new HashSet<>();
  private ProjectStatus status;

  Project(String scmUrl) {
    this.id = UUID.randomUUID().toString();
    this.scmUrl = scmUrl;
  }
}
