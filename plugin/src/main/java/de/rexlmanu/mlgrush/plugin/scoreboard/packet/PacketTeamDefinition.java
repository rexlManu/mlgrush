package de.rexlmanu.mlgrush.plugin.scoreboard.packet;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import java.util.Collection;
import net.kyori.adventure.text.Component;

public record PacketTeamDefinition(
    String name,
    Component prefix,
    Component suffix,
    WrapperPlayServerTeams.NameTagVisibility visibility,
    WrapperPlayServerTeams.CollisionRule collisionRule,
    Collection<String> entries) {}
