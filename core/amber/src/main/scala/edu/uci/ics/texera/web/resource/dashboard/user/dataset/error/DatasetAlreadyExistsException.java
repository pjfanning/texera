package edu.uci.ics.texera.web.resource.dashboard.user.dataset.error;

public class DatasetAlreadyExistsException extends Exception {

  private static final long serialVersionUID = 5678L; // Changed the serialVersionUID for uniqueness

  /**
   * Constructs the exception with the name of the dataset that already exists.
   *
   * @param datasetName The name of the dataset that already exists.
   */
  public DatasetAlreadyExistsException(String datasetName) {
    super("Dataset named: " + datasetName + " already exists");
  }

  /**
   * Constructs the exception with the name of the dataset that already exists and a cause.
   *
   * @param datasetName  The name of the dataset that already exists.
   * @param cause        The cause of the exception (used for exception chaining).
   */
  public DatasetAlreadyExistsException(String datasetName, Throwable cause) {
    super("Dataset named: " + datasetName + " already exists", cause);
  }
}
