package de.rexlmanu.mlgrush.plugin.event;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

public class EventCancel {

    private static final EventExecutor EVENT_EXECUTOR = new CancellableEventExecutor();

    /**
     * Cancel any {@link Event} that is cancellable
     *
     * @param eventClass event
     */
    public static void on(@NotNull Class<? extends Event> eventClass) {
        Bukkit.getPluginManager().registerEvent(
                eventClass,
                new Listener() {},
                EventPriority.HIGHEST,
                EVENT_EXECUTOR,
                GamePlugin.getProvidingPlugin(GamePlugin.class)
        );
    }
}
