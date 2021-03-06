package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import eu.miopowered.nickapi.identity.Skin;
import eu.miopowered.nickapi.user.NickUser;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Accessors(fluent = true)
public class NickCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "nick";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) throws Exception {
    NickUser nickUser = NickUser.builder(gamePlayer.player())
      .username("MioNaruse")
      .uniqueId(UUID.fromString("34e57efa-5783-46c7-a9fc-890296aaba1f"))
      .skin(new Skin("ewogICJ0aW1lc3RhbXAiIDogMTYyNDQwOTYxNDczMSwKICAicHJvZmlsZUlkIiA6ICI0M2ZiZjA1MWZkZTk0YjMwYjkyZWZlNDgwNGI4YWIwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ3ZWViIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFkODZjMWY3NGI0YjFiYjgwZTI3YzUxOTdlMWE4ZDlhYjRlNDY4M2FiMDMzZmU0MGFkYmU0ZmZiMWY0NWQxNDgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        "ZgB4RDHiU8cDzs3NTZAvMh1/06Py9BEEmlNy/pNjp02QiBy8QsYziQcPRAELqkQWpBM5ArAjToUFZfaMQy1ej5/P8N9S89xM0lD9Mho2cF23nfMVQY53+S4tAFuKKQ1IU7+EyWYxuCLq2vS7w1ENiOxcKXW3hW97snKlQVPP3DyZKyt07HjvlHOJlQb7KYg+cRK7f++K2qFTjcbUtubrVQRRpWdFisOwKjLqGLiQA1JTG18QXjQH5kIwAVIg5hCkrq5jgRzd3DvA6O1UvU3+pePh+QLqUwVBZV27Jpe6NVzwT9iHdcgpCKkku9Aehs/ff8UI3BqBdFI+4sQtMcAtBxqiLITPH+zeeb5EQnnS7sZE8caRT2U6GHXra2ZX81oNULspQBIe/5CrawMRpnzGZ17NS0ux7vR2pRBzfuqGdPhKbrajEFqq9WRXAzEki8DJNGt/DvFHrIn2ltdffm2ECMqQ6Bq9Kk0XE3bTK4sopKNST9R49KdBBDOLoY7YqXy4u4I9Wm5JjDTnVz8hZwUUseuXKquqHBqoG8bczxck3fzehUnEC647sbegcmAcZl35ND7OxfBispOCXFNuFrQcRDhyANIZp+RxnuBsWo6gMKqijbFfVoQLq14p/fRzaUQauTAe6wFveHuYRBatMr6Ej75cA9n9566XqCoAhvVbor8="
      ))
      .filter(player -> player.getName().equals(gamePlayer.player().getName()))
      .build();
    GameManager.instance().nickAPI().register(nickUser);
    gamePlayer.sendMessage("Du wurdest genickt.");
    GameManager.instance().nickAPI().updater().update(nickUser, nickUser.fakeIdentity());
    GameManager.instance().scoreboardHandler().updateAll(gamePlayer.environment());
  }
}
