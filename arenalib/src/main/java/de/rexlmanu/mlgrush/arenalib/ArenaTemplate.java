package de.rexlmanu.mlgrush.arenalib;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@Data
@Builder
public class ArenaTemplate implements Serializable {
  private String displayMaterial;
  private String name;
  private String description;
  private Map<String, Position> positionMap;
  private List<Ingredient> ingredients;
  // x, y, z, code
  private int[][][] layout;
}
