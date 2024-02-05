package edu.uci.ics.texera.web.resource.dashboard.user.dataset.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

  public void initRepo() throws IOException, InterruptedException {
    GitSystemCall.initRepo(baseRepoPath);
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

  public void recoverToLatestVersion() throws IOException, InterruptedException {
    if (GitSystemCall.hasUncommittedChanges(baseRepoPath)) {
      GitSystemCall.rollbackToLastCommit(baseRepoPath);
    }
  }

  public FileInputStream retrieveFileContentAsStream(String commitHash, String filePath) throws IOException, InterruptedException {
    // Create a temporary file to hold the content
    File tempFile = Files.createTempFile("gitContent", ".tmp").toFile();
    tempFile.deleteOnExit(); // Ensure the file is deleted on exit

    // Use GitSystemCall to write the file content at commitHash into the tempFile
    try (OutputStream tempOutputStream = Files.newOutputStream(tempFile.toPath())) {
      GitSystemCall.showFileContentOfCommit(baseRepoPath, commitHash, filePath, tempOutputStream);
    }

    // Return a FileInputStream for the temporary file
    return new FileInputStream(tempFile);
  }


  public String writeFileContentToTempFile(String commitHash, String filePath) throws IOException, InterruptedException {
    // Remove leading slash from filePath if present
    if (filePath.startsWith("/")) {
      filePath = filePath.substring(1);
    }

    // Extract the file extension from the filePath
    String fileExtension = "";
    int dotIndex = filePath.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < filePath.length() - 1) { // Ensure there is an extension
      fileExtension = filePath.substring(dotIndex); // Include the dot in the extension
    }

    // Create a temporary file with the extracted file extension
    Path tempFilePath = Files.createTempFile("gitContent", fileExtension); // Use the extracted extension
    File tempFile = tempFilePath.toFile();
    tempFile.deleteOnExit(); // Ensure the file is deleted on exit

    // Use GitSystemCall to write the file content at commitHash into the tempFile
    try (OutputStream tempOutputStream = Files.newOutputStream(tempFilePath)) {
      GitSystemCall.showFileContentOfCommit(baseRepoPath, commitHash, filePath, tempOutputStream);
    }

    // Return the absolute path of the temporary file as a String
    return tempFilePath.toAbsolutePath().toString();
  }
}

