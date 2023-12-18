package edu.uci.ics.texera.web.resource.dashboard.user.dataset.version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class GitVersionControl {
  private final String baseRepoPath;

  public GitVersionControl(String baseRepoPath) throws IOException{
    this.baseRepoPath = baseRepoPath;

    if (!Files.exists(Paths.get(baseRepoPath))) {
      throw new IOException("Base repository path does not exist: " + baseRepoPath);
    }
  }

  // return the hashcode of this version
  public void createVersion(String versionName, Optional<String> baseVersion) throws IOException, InterruptedException {
//    if (GitSystemCall.branchExists(baseRepoPath, versionName)) {
//      throw new IOException("Target version repository already exist: " + versionName);
//    }

//    if (baseVersion.isPresent())
//      GitSystemCall.checkoutBranch(baseRepoPath, baseVersion.get());
//
//    GitSystemCall.createBranch(baseRepoPath, versionName);
  }
}
