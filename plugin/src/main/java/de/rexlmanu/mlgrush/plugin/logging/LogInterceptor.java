package de.rexlmanu.mlgrush.plugin.logging;

import de.raik.webhook.WebhookBuilder;
import de.raik.webhook.elements.Embed;
import de.raik.webhook.elements.embedelements.FooterElement;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Accessors(fluent = true)
public class LogInterceptor extends AbstractAppender {
  private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/856648546412658688/hNfmi10sSnOL2jNqpTnR80C_C8M-b5wHThq6-c8v9z762H6Ahez_BB1C07z_M586upeE";
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

  public LogInterceptor() {
    super("LogInterceptor", null, null);
  }

  @Override
  public void append(LogEvent event) {
    if (event.getLevel().equals(Level.INFO)) {
      return;
    }
    EXECUTOR_SERVICE.submit(() -> {
      WebhookBuilder builder = new WebhookBuilder(WEBHOOK_URL);
      try {
        builder.addEmbed(new Embed()
          .title("MLGRush - Error")
          .description(event.getMessage().getFormattedMessage())
          .footer(new FooterElement("Sent from " + InetAddress.getLocalHost().getHostName()))
        ).build().execute();
      } catch (Exception ignored) {
      }
    });
  }
}
