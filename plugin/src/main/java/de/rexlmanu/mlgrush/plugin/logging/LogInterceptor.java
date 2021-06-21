package de.rexlmanu.mlgrush.plugin.logging;

import de.raik.webhook.WebhookBuilder;
import de.raik.webhook.elements.Embed;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Accessors(fluent = true)
public class LogInterceptor extends AbstractAppender {
  private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/856639465716187208/WyXMFe2zQe9qtu37oO-548znmkIqeQyqYFVhu9YplNxF4c1l6-PUC92OKNx4u4bhwHb8";
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
      builder.addEmbed(new Embed()
        .title("MLGRush - Error")
        .description(event.getMessage().getFormattedMessage())
      ).build().execute();
    });
  }
}
