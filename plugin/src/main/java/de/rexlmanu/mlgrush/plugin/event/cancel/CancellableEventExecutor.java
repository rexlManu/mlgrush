package de.rexlmanu.mlgrush.plugin.event.cancel;

import lombok.SneakyThrows;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Method;

/**
 * Used for {@link EventCancel} to cancel any event
 */
public class CancellableEventExecutor implements EventExecutor {
  @SneakyThrows
  @Override
  public void execute(Listener listener, Event event) throws EventException {
    Class<? extends Event> eventClass = event.getClass();
    Method setCancelled = eventClass.getMethod("setCancelled", Boolean.TYPE);
    setCancelled.invoke(event, true);
  }
}
