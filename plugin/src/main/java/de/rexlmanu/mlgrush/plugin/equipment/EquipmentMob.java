package de.rexlmanu.mlgrush.plugin.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.entity.EntityType;

import java.util.List;

@AllArgsConstructor
@Data
@Getter
@Accessors(fluent = true)
@Builder
public class EquipmentMob {

  private EntityType entityType;
  private List<String> lines;
  private String locationName;
  private String inventoryName;
  private Buyable[] elements;

}
