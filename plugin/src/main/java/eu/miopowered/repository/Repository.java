package eu.miopowered.repository;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Repository with all crud operations (create, read, update and delete)
 *
 * @param <T> the object that is associated with the repository
 */
public interface Repository<T extends Key> {

  /**
   * Finds all associated objects and returns them in a {@link ImmutableList} list
   *
   * @return all objects
   */
  ImmutableList<T> all();

  /**
   * Search for a object with a specific {@link Key}
   *
   * @param key the key that is used to search
   * @return a {@link Optional} with the object, could be empty
   */
  Optional<T> find(@NotNull Key key);

  /**
   * Executes the create operation for a object
   *
   * @param object the object that should be inserted
   * @return should return false if the object is already inserted
   */
  boolean insert(@NotNull T object);

  /**
   * Update a object to save it
   *
   * @param object the object that should be updated
   * @return a if the action was successful
   */
  boolean update(@NotNull T object);

  /**
   * Deletes a object by {@link Key}
   *
   * @param key the key from the object
   * @return should only return false if the object dont exist
   */
  boolean delete(@NotNull Key key);
}
