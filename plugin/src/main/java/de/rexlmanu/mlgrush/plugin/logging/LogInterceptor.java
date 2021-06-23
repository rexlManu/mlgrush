package de.rexlmanu.mlgrush.plugin.logging;

import de.raik.webhook.WebhookBuilder;
import de.raik.webhook.elements.Embed;
import de.raik.webhook.elements.embedelements.FooterElement;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Accessors(fluent = true)
public class LogInterceptor extends AbstractAppender {
  private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/856648546412658688/hNfmi10sSnOL2jNqpTnR80C_C8M-b5wHThq6-c8v9z762H6Ahez_BB1C07z_M586upeE";
  private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

  private List<String> logs = new CopyOnWriteArrayList<>();

  public LogInterceptor() {
    super("LogInterceptor", null, null);

    EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
      if (logs.isEmpty()) return;
      WebhookBuilder builder = new WebhookBuilder(WEBHOOK_URL);
      try {
        builder.addEmbed(new Embed()
          .title("MLGRush - Error")
          .description(String.join(System.lineSeparator(), this.logs))
          .footer(new FooterElement("Sent from " + InetAddress.getLocalHost().getHostName()))
        ).build().execute();
        logs.clear();
      } catch (Exception ignored) {
      }
    }, 0, 2, TimeUnit.SECONDS);
  }

  @Override
  public void append(LogEvent event) {
    if (event.getLevel().equals(Level.INFO)) {
      return;
    }
    this.logs.add(event.getMessage().getFormattedMessage());
  }
}
