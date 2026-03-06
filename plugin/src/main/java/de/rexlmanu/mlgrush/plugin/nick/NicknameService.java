package de.rexlmanu.mlgrush.plugin.nick;

import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NicknameService {

  private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

  private final Map<UUID, NickProfile> profiles = new ConcurrentHashMap<>();

  public Optional<NickProfile> get(UUID uniqueId) {
    return Optional.ofNullable(this.profiles.get(uniqueId));
  }

  public boolean isNicked(UUID uniqueId) {
    return this.profiles.containsKey(uniqueId);
  }

  public String displayName(UUID uniqueId, String fallback) {
    return this.get(uniqueId).map(NickProfile::nickname).orElse(fallback);
  }

  public void register(Player player, String nickname) {
    String safeNickname = nickname == null || nickname.isBlank() ? player.getName() : nickname.trim();
    if (safeNickname.length() > 16) {
      safeNickname = safeNickname.substring(0, 16);
    }
    NickProfile profile = new NickProfile(player.getName(), safeNickname);
    this.profiles.put(player.getUniqueId(), profile);
    this.apply(player, profile.nickname());
  }

  public void unregister(Player player) {
    this.profiles.remove(player.getUniqueId());
    this.apply(player, player.getName());
  }

  private void apply(Player player, String text) {
    String formatted = MessageFormat.replaceColors(text);
    player.displayName(LEGACY_SERIALIZER.deserialize(formatted));
    player.playerListName(LEGACY_SERIALIZER.deserialize(formatted));
    player.setCustomName(formatted);
  }

  public record NickProfile(String realName, String nickname) {
  }
}
