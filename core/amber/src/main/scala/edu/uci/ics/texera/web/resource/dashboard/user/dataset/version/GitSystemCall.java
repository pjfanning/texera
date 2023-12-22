package edu.uci.ics.texera.web.resource.dashboard.user.dataset.version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitSystemCall {

  public static void initRepo(String path) throws IOException, InterruptedException {
    executeGitCommand(path, "init", path);
  }

  public static void cloneShared(String srcPath, String destPath) throws IOException, InterruptedException {
    executeGitCommand(srcPath, "clone", "--shared", srcPath, destPath);
  }

  public static String addAndCommit(String repoPath, String commitMessage) throws IOException, InterruptedException {
    // Adding all files and committing
    executeGitCommand(repoPath, "add", ".");
    executeGitCommand(repoPath, "commit", "-m", commitMessage);

    // Retrieving the full commit hash of the latest commit
    return executeGitCommand(repoPath, "rev-parse", "HEAD");
  }

  public static void showFileContentOfCommit(String repoPath, String commitHash, String filePath, OutputStream outputStream) throws IOException, InterruptedException {
    // Use the commit hash instead of the branch name in the git command
    String fileContent = executeGitCommand(repoPath, "show", commitHash + ":" + filePath);
    try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream))) {
      writer.print(fileContent);
    }
  }


  public static Map<String, Object> getFileTreeHierarchy(String repoPath, String commitHash) throws IOException, InterruptedException {
    String treeOutput = executeGitCommand(repoPath, "ls-tree", "-r", commitHash);
    return parseFileTree(treeOutput);
  }

  private static Map<String, Object> parseFileTree(String treeOutput) {
    Map<String, Object> fileTree = new HashMap<>();
    StringTokenizer st = new StringTokenizer(treeOutput, "\n");
    while (st.hasMoreTokens()) {
      String line = st.nextToken();
      String[] parts = line.split("\\s+");

      if (parts.length > 3) {
        String type = parts[1]; // "blob" for files, "tree" for directories
        String path = parts[3];

        if (type.equals("blob")) {
          String[] pathParts = path.split("/");
          addToFileTree(fileTree, pathParts, 0);
        }
      }
    }
    return fileTree;
  }

  private static void addToFileTree(Map<String, Object> tree, String[] pathParts, int index) {
    if (index == pathParts.length - 1) {
      // It's a file, add it to the map
      tree.put(pathParts[index], "file");
    } else {
      // It's a directory, recurse
      tree.computeIfAbsent(pathParts[index], k -> new HashMap<String, Object>());
      @SuppressWarnings("unchecked")
      Map<String, Object> subTree = (Map<String, Object>) tree.get(pathParts[index]);
      addToFileTree(subTree, pathParts, index + 1);
    }
  }

  private static String parseCommitHash(String commitOutput) {
    Pattern pattern = Pattern.compile("\\[.* ([0-9a-f]{5,40})\\]");
    Matcher matcher = pattern.matcher(commitOutput);

    if (matcher.find()) {
      return matcher.group(1); // Group 1 is the commit hash
    } else {
      return null; // or throw an exception if the commit hash is not found
    }
  }


  private static String executeGitCommand(String workingDirectory, String... args) throws IOException, InterruptedException {
    List<String> commands = new ArrayList<>();
    commands.add("git"); // Add the "git" prefix
    Collections.addAll(commands, args); // Add the rest of the arguments

    ProcessBuilder builder = new ProcessBuilder(commands);
    builder.directory(new File(workingDirectory));  // Set the working directory
    builder.redirectErrorStream(true); // Redirect error stream to standard output
    Process process = builder.start();

    // Reading the output of the command
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new IOException("Failed to execute Git command: " + String.join(" ", commands));
    }

    return output.toString().trim(); // Return the output as a String
  }

  public static void rollbackToLastCommit(String repoPath) throws IOException, InterruptedException {
    executeGitCommand(repoPath, "reset", "--hard", "HEAD");
  }

  public static boolean hasUncommittedChanges(String repoPath) throws IOException, InterruptedException {
    String statusOutput = executeGitCommand(repoPath, "status", "--porcelain");
    return !statusOutput.isEmpty();
  }
}

