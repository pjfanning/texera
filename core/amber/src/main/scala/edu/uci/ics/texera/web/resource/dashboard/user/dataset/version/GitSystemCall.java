package edu.uci.ics.texera.web.resource.dashboard.user.dataset.version;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitSystemCall {

  public static Charset BYTE_2_STRING_CHARSET = StandardCharsets.UTF_8;

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
    byte[] commitHashOutput = executeGitCommand(repoPath, "rev-parse", "HEAD");
    return new String(commitHashOutput, StandardCharsets.UTF_8).trim();
  }

  public static void showFileContentOfCommit(String repoPath, String commitHash, String filePath, OutputStream outputStream) throws IOException, InterruptedException {
    byte[] fileContent = executeGitCommand(repoPath, "show", commitHash + ":" + filePath);
    outputStream.write(fileContent);
  }


  public static Map<String, Object> getFileTreeHierarchy(String repoPath, String commitHash) throws IOException, InterruptedException {
    String treeOutput = new String(executeGitCommand(repoPath, "ls-tree", "-r", commitHash), BYTE_2_STRING_CHARSET);
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

  private static byte[] executeGitCommand(String workingDirectory, String... args) throws IOException, InterruptedException {
    List<String> commands = new ArrayList<>();
    commands.add("git");
    Collections.addAll(commands, args);

    ProcessBuilder builder = new ProcessBuilder(commands);
    builder.directory(new File(workingDirectory));
    builder.redirectErrorStream(true);
    Process process = builder.start();

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = process.getInputStream().read(buffer)) != -1) {
        outputStream.write(buffer, 0, length);
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new IOException("Failed to execute Git command: " + String.join(" ", commands));
      }

      return outputStream.toByteArray();
    }
  }

  public static void rollbackToLastCommit(String repoPath) throws IOException, InterruptedException {
    executeGitCommand(repoPath, "reset", "--hard", "HEAD");
  }

  public static boolean hasUncommittedChanges(String repoPath) throws IOException, InterruptedException {
    String statusOutput = new String(executeGitCommand(repoPath, "status", "--porcelain"), BYTE_2_STRING_CHARSET);
    return !statusOutput.isEmpty();
  }
}

