package de.rexlmanu.mlgrush.plugin.scoreboard.packet;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;

import java.util.Collection;

public record PacketTeamDefinition(
  String name,
  Component prefix,
  Component suffix,
  WrapperPlayServerTeams.NameTagVisibility visibility,
  WrapperPlayServerTeams.CollisionRule collisionRule,
  Collection<String> entries
) {
}
