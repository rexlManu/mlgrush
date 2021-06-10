package de.rexlmanu.mlgrush.arenalib;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;

public class ArenaFormat {

  public static void toFile(ArenaTemplate template, Path path) {
    try (FileOutputStream fileOutputStream = new FileOutputStream(path.toFile())) {
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
      objectOutputStream.writeObject(template);
      objectOutputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Optional<ArenaTemplate> fromFile(Path path) {
    try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
      ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
      return Optional.of((ArenaTemplate) objectInputStream.readObject());
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

}
