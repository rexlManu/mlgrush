package de.rexlmanu.mlgrush.plugin.integration;

import de.rexlmanu.mlgrush.plugin.GamePlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IntegrationHandler {

  private static final List<GameIntegration> INTEGRATIONS = new ArrayList<>();

  static {
    availableIntegrations().forEach(integration -> {
      try {
        INTEGRATIONS.add(integration.gameIntegration().getConstructor().newInstance());
        GamePlugin.getProvidingPlugin(GamePlugin.class).getLogger().info(String.format("GameIntegration %s enabled", integration.name().toLowerCase()));
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        e.printStackTrace();
      }
    });
  }

  private static List<Integration> availableIntegrations() {
    return Arrays.stream(Integration.values()).filter(Integration::available).collect(Collectors.toList());
  }

  public static void enableIntegration() {
    INTEGRATIONS.forEach(GameIntegration::onEnable);
  }

  public static void gameInitIntegration() {
    INTEGRATIONS.forEach(GameIntegration::onGameInit);
  }
}
