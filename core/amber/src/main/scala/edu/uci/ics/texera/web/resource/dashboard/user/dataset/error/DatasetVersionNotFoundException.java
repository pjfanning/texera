package edu.uci.ics.texera.web.resource.dashboard.user.dataset.error;

public class DatasetVersionNotFoundException extends Exception {

  private static final long serialVersionUID = 6790L; // Unique serialVersionUID

  /**
   * Constructs the exception with the dataset ID for which no version was found.
   *
   * @param did The dataset ID for which no version was found.
   */
  public DatasetVersionNotFoundException(int did) {
    super("No version found for dataset " + did);
  }

  /**
   * Constructs the exception with the dataset ID for which no version was found and a cause.
   *
   * @param did   The dataset ID for which no version was found.
   * @param cause The cause of the exception (used for exception chaining).
   */
  public DatasetVersionNotFoundException(int did, Throwable cause) {
    super("No version found for dataset " + did, cause);
  }
}
