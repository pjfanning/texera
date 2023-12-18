package edu.uci.ics.texera.web.resource.dashboard.user.dataset.version;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class GitVersionControl {
  private final String baseRepoPath;

  public GitVersionControl(String baseRepoPath) throws IOException{
    this.baseRepoPath = baseRepoPath;

    if (!Files.exists(Paths.get(baseRepoPath))) {
      throw new IOException("Base repository path does not exist: " + baseRepoPath);
    }
  }

  // return the hashcode of this version
  public String createVersion(String versionName) throws IOException, InterruptedException {
    // Assuming versionName is used as the commit message
    return GitSystemCall.addAndCommit(baseRepoPath, versionName);
  }

  public Map<String, Object> retrieveFileTreeOfVersion(String versionCommitHashVal) throws IOException, InterruptedException {
    return GitSystemCall.getFileTreeHierarchy(baseRepoPath, versionCommitHashVal);
  }

  public void retrieveFileContentOfVersion(String commitHash, String filePath, OutputStream outputStream) throws IOException, InterruptedException {
    GitSystemCall.showFileContentOfCommit(baseRepoPath, commitHash, filePath, outputStream);
  }
}
