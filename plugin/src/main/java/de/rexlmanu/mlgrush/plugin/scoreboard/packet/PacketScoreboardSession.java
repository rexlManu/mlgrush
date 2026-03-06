package de.rexlmanu.mlgrush.plugin.scoreboard.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PacketScoreboardSession {

  private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
  private static final int PLAYER_LIST_SLOT = 0;
  private static final int SIDEBAR_SLOT = 1;
  private static final int BELOW_NAME_SLOT = 2;

  private final UUID uniqueId;
  private final String sidebarObjective;
  private final String belowNameObjective;

  private String sidebarTitle = "";
  private int sidebarLineCount;
  private boolean sidebarInitialized;
  private boolean belowNameInitialized;
  private final Set<String> activeTeams = new HashSet<>();

  public PacketScoreboardSession(UUID uniqueId) {
    this.uniqueId = uniqueId;
    String base = uniqueId.toString().replace("-", "");
    this.sidebarObjective = ("sb" + base).substring(0, 16);
    this.belowNameObjective = ("bn" + base).substring(0, 16);
  }

  public void updateSidebar(String title, List<String> lines) {
    Player player = this.player();
    if (player == null) {
      return;
    }

    Component titleComponent = this.component(title);
    if (!this.sidebarInitialized) {
      this.sendPacket(new WrapperPlayServerScoreboardObjective(
        this.sidebarObjective,
        WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
        titleComponent,
        WrapperPlayServerScoreboardObjective.RenderType.INTEGER
      ));
      this.sendPacket(new WrapperPlayServerDisplayScoreboard(SIDEBAR_SLOT, this.sidebarObjective));
      this.sidebarInitialized = true;
    } else if (!this.sidebarTitle.equals(title)) {
      this.sendPacket(new WrapperPlayServerScoreboardObjective(
        this.sidebarObjective,
        WrapperPlayServerScoreboardObjective.ObjectiveMode.UPDATE,
        titleComponent,
        WrapperPlayServerScoreboardObjective.RenderType.INTEGER
      ));
    }

    for (int index = 0; index < this.sidebarLineCount; index++) {
      if (index >= lines.size()) {
        this.sendPacket(new WrapperPlayServerResetScore(this.sidebarHolder(index), this.sidebarObjective));
      }
    }

    for (int index = 0; index < lines.size(); index++) {
      this.sendPacket(new WrapperPlayServerUpdateScore(
        this.sidebarHolder(index),
        WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
        this.sidebarObjective,
        lines.size() - index,
        this.component(lines.get(index)),
        null
      ));
    }

    this.sidebarTitle = title;
    this.sidebarLineCount = lines.size();
  }

  public void updateBelowName(String title, Map<String, Integer> scores) {
    Player player = this.player();
    if (player == null) {
      return;
    }

    Component titleComponent = this.component(title);
    if (!this.belowNameInitialized) {
      this.sendPacket(new WrapperPlayServerScoreboardObjective(
        this.belowNameObjective,
        WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
        titleComponent,
        WrapperPlayServerScoreboardObjective.RenderType.INTEGER
      ));
      this.sendPacket(new WrapperPlayServerDisplayScoreboard(BELOW_NAME_SLOT, this.belowNameObjective));
      this.belowNameInitialized = true;
    } else {
      this.sendPacket(new WrapperPlayServerScoreboardObjective(
        this.belowNameObjective,
        WrapperPlayServerScoreboardObjective.ObjectiveMode.UPDATE,
        titleComponent,
        WrapperPlayServerScoreboardObjective.RenderType.INTEGER
      ));
    }

    Bukkit.getOnlinePlayers().forEach(target -> {
      if (scores.containsKey(target.getName())) {
        this.sendPacket(new WrapperPlayServerUpdateScore(
          target.getName(),
          WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
          this.belowNameObjective,
          scores.get(target.getName()),
          null,
          null
        ));
      } else {
        this.sendPacket(new WrapperPlayServerResetScore(target.getName(), this.belowNameObjective));
      }
    });
  }

  public void clearBelowName() {
    if (!this.belowNameInitialized) {
      return;
    }
    Bukkit.getOnlinePlayers().forEach(target -> this.sendPacket(new WrapperPlayServerResetScore(target.getName(), this.belowNameObjective)));
    this.sendPacket(new WrapperPlayServerScoreboardObjective(
      this.belowNameObjective,
      WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
      Component.empty(),
      WrapperPlayServerScoreboardObjective.RenderType.INTEGER
    ));
    this.belowNameInitialized = false;
  }

  public void applyTeams(List<PacketTeamDefinition> definitions) {
    this.activeTeams.forEach(teamName -> this.sendPacket(new WrapperPlayServerTeams(teamName, WrapperPlayServerTeams.TeamMode.REMOVE, java.util.Optional.empty())));
    this.activeTeams.clear();

    for (PacketTeamDefinition definition : definitions) {
      WrapperPlayServerTeams.ScoreBoardTeamInfo info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
        Component.text(definition.name()),
        definition.prefix(),
        definition.suffix(),
        definition.visibility(),
        definition.collisionRule(),
        null,
        WrapperPlayServerTeams.OptionData.NONE
      );
      this.sendPacket(new WrapperPlayServerTeams(definition.name(), WrapperPlayServerTeams.TeamMode.CREATE, info, definition.entries()));
      this.activeTeams.add(definition.name());
    }
  }

  public void updateTabEntries(Map<UUID, Component> displayNames) {
    List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> entries = new ArrayList<>();
    displayNames.forEach((uuid, displayName) -> {
      WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(uuid);
      playerInfo.setDisplayName(displayName);
      entries.add(playerInfo);
    });

    if (!entries.isEmpty()) {
      this.sendPacket(new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME, entries));
    }
  }

  public void destroy() {
    if (this.sidebarInitialized) {
      for (int index = 0; index < this.sidebarLineCount; index++) {
        this.sendPacket(new WrapperPlayServerResetScore(this.sidebarHolder(index), this.sidebarObjective));
      }
      this.sendPacket(new WrapperPlayServerScoreboardObjective(
        this.sidebarObjective,
        WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
        Component.empty(),
        WrapperPlayServerScoreboardObjective.RenderType.INTEGER
      ));
      this.sidebarInitialized = false;
      this.sidebarLineCount = 0;
    }
    this.clearBelowName();
    this.activeTeams.forEach(teamName -> this.sendPacket(new WrapperPlayServerTeams(teamName, WrapperPlayServerTeams.TeamMode.REMOVE, java.util.Optional.empty())));
    this.activeTeams.clear();
    this.sendPacket(new WrapperPlayServerDisplayScoreboard(PLAYER_LIST_SLOT, ""));
  }

  private String sidebarHolder(int index) {
    return this.sidebarObjective + ':' + index;
  }

  private Component component(String input) {
    return LEGACY_SERIALIZER.deserialize(MessageFormat.replaceColors(input == null ? "" : input));
  }

  private Player player() {
    return Bukkit.getPlayer(this.uniqueId);
  }

  private void sendPacket(PacketWrapper<?> packet) {
    Player player = this.player();
    if (player != null) {
      PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
  }
}
