package de.rexlmanu.mlgrush.arenacreator.utility;

import org.bukkit.ChatColor;

public class MessageFormat {

  public static String replaceColors(String input) {
    return ChatColor.translateAlternateColorCodes('&', input);
  }

  public static String of(String format, Object... parameters) {
    return replaceColors(String.format(format, parameters));
  }

}
