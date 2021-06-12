package de.rexlmanu.mlgrush.plugin.integration.pluginstube;

import lombok.AllArgsConstructor;
import net.pluginstube.library.perk.IPerk;
import net.pluginstube.library.perk.PerkCategory;
import org.bukkit.Material;

@AllArgsConstructor
public class PluginStubePerk implements IPerk {

  private PerkCategory perkCategory;
  private int id;
  private String name;
  private Material material;
  private int price;

  @Override
  public PerkCategory perkCategory() {
    return this.perkCategory;
  }

  @Override
  public Integer id() {
    return this.id;
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public Material material() {
    return this.material;
  }

  @Override
  public Integer price() {
    return this.price;
  }
}
