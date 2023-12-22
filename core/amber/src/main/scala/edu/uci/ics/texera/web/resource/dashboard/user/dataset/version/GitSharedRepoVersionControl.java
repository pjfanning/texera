package edu.uci.ics.texera.web.resource.dashboard.user.dataset.version;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GitSharedRepoVersionControl {
  private final String baseRepoPath;

  public GitSharedRepoVersionControl(String baseRepoPath) throws IOException{
    this.baseRepoPath = baseRepoPath;

    if (!Files.exists(Paths.get(baseRepoPath))) {
      throw new IOException("Base repository path does not exist: " + baseRepoPath);
    }
  }

  public void createVersion(String versionName, Optional<String> baseVersionName) throws IOException, GitAPIException, InterruptedException {
    String newVersionRepoPath = getDatasetVersionPath(versionName);
    if (Files.exists(Paths.get(newVersionRepoPath))) {
      throw new IOException("Target version repository already exist: " + newVersionRepoPath);
    }
    if (!baseVersionName.isPresent()) {
      Files.createDirectories(Paths.get(newVersionRepoPath));
      GitSystemCall.initRepo(newVersionRepoPath);
    } else {
      // the new version is created based on a base version
      String baseVersionRepoPath = getDatasetVersionPath(baseVersionName.get());
      GitSystemCall.cloneShared(baseVersionRepoPath, newVersionRepoPath);
    }
  }

  public VersionDescriptor checkoutToVersion(String versionName) throws IOException, GitAPIException {
    return new VersionDescriptor(versionName, getDatasetVersionPath(versionName));
  }

  public List<String> listVersions() throws IOException, GitAPIException {
    Path path = Paths.get(baseRepoPath);

    // Create a comparator to sort paths based on creation time
    Comparator<Path> creationTimeComparator = (p1, p2) -> {
      try {
        BasicFileAttributes attrs1 = Files.readAttributes(p1, BasicFileAttributes.class);
        BasicFileAttributes attrs2 = Files.readAttributes(p2, BasicFileAttributes.class);
        // Compare in reverse order to get the list from latest to earliest
        return attrs2.creationTime().compareTo(attrs1.creationTime());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    };

    return Files.list(path)
        .filter(Files::isDirectory)
        .sorted(creationTimeComparator) // Sort directories based on creation time
        .map(p -> p.getFileName().toString())  // Get only the last segment of the path
        .collect(Collectors.toList());
  }


  public String getDatasetVersionPath(String versionName) {
    return baseRepoPath + "/" + versionName;
  }

  public void commitVersion(String versionName) throws IOException, InterruptedException {
    String repoPath = getDatasetVersionPath(versionName);
    GitSystemCall.addAndCommit(repoPath, "defaultMessage");
  }
}
