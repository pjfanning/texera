package edu.uci.ics.texera.web.resource.dashboard.user.dataset.version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GitSystemCall {

  public static void initRepo(String path) throws IOException, InterruptedException {
    executeGitCommand(path, "init", path);
  }

  public static void cloneShared(String srcPath, String destPath) throws IOException, InterruptedException {
    executeGitCommand(srcPath, "clone", "--shared", srcPath, destPath);
  }

  public static void addAndCommit(String repoPath, String commitMessage) throws IOException, InterruptedException {
    // Adding all files
    executeGitCommand(repoPath, "add", ".");
    // Committing the changes
    executeGitCommand(repoPath, "commit", "-m", commitMessage);
  }

  private static void executeGitCommand(String workingDirectory, String... args) throws IOException, InterruptedException {
    List<String> commands = new ArrayList<>();
    commands.add("git"); // Add the "git" prefix
    Collections.addAll(commands, args); // Add the rest of the arguments

    ProcessBuilder builder = new ProcessBuilder(commands);
    builder.directory(new File(workingDirectory));  // Set the working directory
    Process process = builder.start();
    int exitCode = process.waitFor();

    if (exitCode != 0) {
      throw new IOException("Failed to execute Git command: " + String.join(" ", commands));
    }
  }
}

