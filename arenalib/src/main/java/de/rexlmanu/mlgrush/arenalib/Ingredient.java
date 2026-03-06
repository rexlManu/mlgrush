package de.rexlmanu.mlgrush.arenalib;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Data
@Accessors(fluent = true)
public class Ingredient implements Serializable {
  private int code;
  private String material;
  private int data;
}
