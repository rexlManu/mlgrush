package eu.miopowered.repository;

/**
 * For the object to implement to identify the key for naming
 */
public interface Key {

  /**
   * For the usage when for cases where only the key is required and not the whole object
   * <p>
   * Use cases are for like find or deleting in {@link Repository}
   *
   * @param object the value
   * @return a {@link Key} instance
   */
  static Key wrap(Object object) {
    return object::toString;
  }

  /**
   * Gets the object as string mostly via {@link Object}::toString method.
   *
   * @return the object as string
   */
  String getKey();

}
