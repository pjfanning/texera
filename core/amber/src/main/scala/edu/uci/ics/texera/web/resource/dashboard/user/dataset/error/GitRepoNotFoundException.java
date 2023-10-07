package edu.uci.ics.texera.web.resource.dashboard.user.dataset.error;

public class GitRepoNotFoundException extends Exception {

  private static final long serialVersionUID = 1234L;

  /**
   * Constructs the exception with the path where the Git repo was not found.
   *
   * @param path The path where the Git repo was expected but not found.
   */
  public GitRepoNotFoundException(String path) {
    super("Git repo is not found at " + path);
  }

  /**
   * Constructs the exception with the path where the Git repo was not found and a cause.
   *
   * @param path   The path where the Git repo was expected but not found.
   * @param cause  The cause of the exception (used for exception chaining).
   */
  public GitRepoNotFoundException(String path, Throwable cause) {
    super("Git repo is not found at " + path, cause);
  }
}




