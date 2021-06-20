package de.rexlmanu.mlgrush.plugin.command;

import de.rexlmanu.mlgrush.plugin.command.exception.CommandParameterMissingException;

@SuppressWarnings("unchecked")
public interface CommandParameter {

  static void require(String name, int position, String[] arguments) throws CommandParameterMissingException {
    if (arguments.length < (position + 1)) throw new CommandParameterMissingException(
      String.format("Der Parameter &8'&d%s&8' &7fehlt.", name)
    );
  }

  static <T> T cast(String name, int position, String[] arguments, Class<T> type) throws CommandParameterMissingException {
    require(name, position, arguments);
    try {
      return (T) type.getMethod("valueOf", String.class).invoke(null, arguments[position]);
    } catch (Exception e) {
      throw new CommandParameterMissingException(
        String.format("Der Parameter &8'&d%s&8' &7muss Type &8'&d%s&8' &7sein.", name, type.getName())
      );
    }
  }

}
