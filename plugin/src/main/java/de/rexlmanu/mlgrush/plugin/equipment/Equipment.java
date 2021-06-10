package de.rexlmanu.mlgrush.plugin.equipment;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;

public interface Equipment {

  void onEquip(GamePlayer gamePlayer, int slot);

}
