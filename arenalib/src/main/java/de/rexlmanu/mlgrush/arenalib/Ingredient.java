package de.rexlmanu.mlgrush.arenalib;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@AllArgsConstructor
@Data
@Accessors(fluent = true)
public class Ingredient implements Serializable {
  private int code;
  private String material;
  private int data;
}
