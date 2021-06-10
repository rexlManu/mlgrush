package de.rexlmanu.mlgrush.plugin.event;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.event.type.GamePlayerEvent;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventCoordinator implements EventExecutor {

  private List<EventContainer<?>> containers;

  public EventCoordinator() {
    this.containers = new ArrayList<>();
  }

  public <E> EventContainer<E> add(Environment environment, Class<E> eventClass, Consumer<GamePlayerEvent<E>> eventConsumer) {
    EventContainer<E> handler = new EventContainer<>(environment, eventClass, eventConsumer);
    this.containers.add(handler);
    Bukkit.getPluginManager().registerEvent(
      (Class<? extends Event>) handler.eventClass(),
      handler,
      EventPriority.HIGHEST,
      this,
      GamePlugin.getProvidingPlugin(GamePlugin.class)
    );
    return handler;
  }

  @Override
  public void execute(Listener listener, Event event) throws EventException {
    if (event instanceof PlayerEvent) {
      this.callEvents(((PlayerEvent) event).getPlayer(), event, listener);
      return;
    }
    if (event instanceof BlockPlaceEvent) {
      this.callEvents(((BlockPlaceEvent) event).getPlayer(), event, listener);
      return;
    }
    if (event instanceof BlockBreakEvent) {
      this.callEvents(((BlockBreakEvent) event).getPlayer(), event, listener);
      return;
    }
  }

  private void callEvents(Player target, Event event, Listener listener) {
    PlayerProvider.find(target.getUniqueId()).ifPresent(gamePlayer -> this.containers.stream()
      .filter(eventContainer -> eventContainer.equals(listener))
      .filter(eventContainer -> eventContainer.eventClass().equals(event.getClass()))
      .filter(eventContainer -> gamePlayer.environment().equals(eventContainer.environment()))
      .findAny()
      .ifPresent(eventContainer -> {
        eventContainer.eventConsumer().accept(new GamePlayerEvent(event, gamePlayer));
      }));
  }

  public void remove(EventContainer<?> eventContainer) {
    this.containers.remove(eventContainer);
  }
}
