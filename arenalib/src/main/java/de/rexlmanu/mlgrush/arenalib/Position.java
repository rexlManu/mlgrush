package de.rexlmanu.mlgrush.arenalib;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@AllArgsConstructor
@Data
@Accessors(fluent = true)
@Builder
public class Position implements Serializable {
  private double x, y, z;
  private float yaw, pitch;
}
