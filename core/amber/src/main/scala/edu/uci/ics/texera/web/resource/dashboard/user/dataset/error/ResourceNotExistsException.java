package edu.uci.ics.texera.web.resource.dashboard.user.dataset.error;

/**
 * An exception that is thrown when a requested resource does not exist.
 */
public class ResourceNotExistsException extends Exception {

  private static final long serialVersionUID = 9876L; // Unique serialVersionUID for serialization

  /**
   * Constructs the exception with the resource identifier that does not exist.
   *
   * @param resourceId The identifier of the resource that does not exist.
   */
  public ResourceNotExistsException(String resourceId) {
    super("Resource with ID " + resourceId + " does not exist");
  }

  /**
   * Constructs the exception with the resource identifier that does not exist and a cause.
   *
   * @param cause The cause of the exception (used for exception chaining).
   */
  public ResourceNotExistsException(String desc, Throwable cause) {
    super("Resource " + desc + " does not exist", cause);
  }
}
