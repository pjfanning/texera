package edu.uci.ics.texera.web.resource.dashboard.user.dataset.version;

public class VersionDescriptor {
  private String versionID;
  private String versionRepoPath;

  public VersionDescriptor(String versionID, String versionRepoPath) {
    this.versionID = versionID;
    this.versionRepoPath = versionRepoPath;
  }

  public String getVersionID() {
    return versionID;
  }

  public void setVersionID(String versionID) {
    this.versionID = versionID;
  }

  public String getVersionRepoPath() {
    return versionRepoPath;
  }

  public void setVersionRepoPath(String versionRepoPath) {
    this.versionRepoPath = versionRepoPath;
  }
}
