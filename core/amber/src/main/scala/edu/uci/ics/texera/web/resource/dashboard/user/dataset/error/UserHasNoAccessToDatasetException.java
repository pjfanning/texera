package edu.uci.ics.texera.web.resource.dashboard.user.dataset.error;

public class UserHasNoAccessToDatasetException extends Exception {

  private static final long serialVersionUID = 6789L; // Changed the serialVersionUID for uniqueness

  /**
   * Constructs the exception with the dataset ID to which the user has no access.
   *
   * @param did The dataset ID to which the user has no access.
   */
  public UserHasNoAccessToDatasetException(int did) {
    super("User has no access to " + did);
  }

  /**
   * Constructs the exception with the dataset ID to which the user has no access and a cause.
   *
   * @param did   The dataset ID to which the user has no access.
   * @param cause The cause of the exception (used for exception chaining).
   */
  public UserHasNoAccessToDatasetException(int did, Throwable cause) {
    super("User has no access to " + did, cause);
  }
}
