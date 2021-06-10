package de.rexlmanu.mlgrush.plugin.event;

import de.rexlmanu.mlgrush.plugin.event.type.GamePlayerEvent;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.function.Consumer;

@AllArgsConstructor
@Data
@Accessors(fluent = true)
public class EventContainer<E> implements Listener {

  private Environment environment;
  private Class<E> eventClass;
  private Consumer<GamePlayerEvent<E>> eventConsumer;

  public EventContainer<E> unregister() {
    HandlerList.unregisterAll(this);
    return this;
  }

}
