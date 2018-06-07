package main.java.com.meridian.utilities.main.java.com.meridian.utilities.ctc;

/**
 * Exception raised when an environment variable is not defined.
 */
public class UndefinedEnvironmentVariableException extends Exception {
  public UndefinedEnvironmentVariableException(String message) {
    super(message);
  }
}
