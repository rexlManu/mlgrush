package eu.miopowered.repository.impl;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.miopowered.repository.Key;
import eu.miopowered.repository.Repository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A {@link Repository} implementation for json file system
 * <p>
 * The objects are serialized by gson and the key name is the file name with json ending.
 *
 * @param <T>
 */
public class GsonRepository<T extends Key> implements Repository<T> {

  private static final Gson GSON = new GsonBuilder().serializeNulls().create();

  /**
   * Creates a instance for {@link GsonRepository} declared {@link Repository}
   *
   * @param directory the directory where the objects are saved
   * @param type      the object class is needed to deserialize the object back to the class
   * @param <T>       the object type
   * @return Repository instance
   */
  public static <T extends Key> Repository<T> of(Path directory, Class<T> type) {
    return new GsonRepository<>(directory, type);
  }

  private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

  private final Path directory;
  private final Class<T> type;

  private GsonRepository(Path directory, Class<T> type) {
    this.directory = directory;
    this.type = type;
  }

  @Override
  public ImmutableList<T> all() {
    try {
      return ImmutableList.copyOf(Files.list(this.directory)
        .map(this::read)
        .map(this::fromJson)
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to find all elements.", e);
      return null;
    }
  }

  @Override
  public Optional<T> find(@NotNull Key key) {
    try {
      return Optional.ofNullable(
        this.fromJson(new String(Files.readAllBytes(this.directory.resolve(this.keyAsFileName(key)))))
      );
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Could not found any element with that key.", e);
      return Optional.empty();
    }
  }

  @Override
  public boolean insert(@NotNull T object) {
    try {
      Files.write(
        this.directory.resolve(this.keyAsFileName(object)),
        this.toJson(object).getBytes(),
        StandardOpenOption.CREATE_NEW
      );
      return true;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error occurred while inserting data.", e);
      return false;
    }
  }

  @Override
  public boolean update(@NotNull T object) {
    try {
      Files.write(
        this.directory.resolve(this.keyAsFileName(object)),
        this.toJson(object).getBytes(),
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
      );
      return true;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error occurred while updating data.", e);
      return false;
    }
  }

  @Override
  public boolean delete(@NotNull Key key) {
    try {
      return Files.deleteIfExists(this.directory.resolve(this.keyAsFileName(key)));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error occurred while deleting data.", e);
      return false;
    }
  }

  private String read(Path path) {
    try {
      return new String(Files.readAllBytes(path));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error occurred while reading data.", e);
      return null;
    }
  }

  private String keyAsFileName(Key key) {
    return key.getKey() + ".json";
  }

  private String toJson(T object) {
    return GSON.toJson(object, object.getClass());
  }

  private T fromJson(String content) {
    if (content == null) return null;
    return GSON.fromJson(content, this.type);
  }
}
