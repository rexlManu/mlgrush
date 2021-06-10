package de.rexlmanu.mlgrush.plugin.utility;

import java.util.List;
import java.util.Random;

public class RandomElement {

  private static final Random RANDOM = new Random();

  public static <E> E of(List<E> list) {
    return list.get(RANDOM.nextInt(list.size()));
  }

}
