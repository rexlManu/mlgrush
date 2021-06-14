package de.rexlmanu.mlgrush.plugin.detection;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Accessors(fluent = true)
@Data
public class Detection {

  private boolean digging;
  private long lastDiggingAction, lastTransactionPing, startTransactionPing;

  private int clicks, transactionId, lastClicks;
  private double average, standardDeviation, averageSecondly;
  private List<Integer> clickHistory;

  public Detection() {
    this.digging = false;
    this.lastDiggingAction = 0;
    this.startTransactionPing = 0;
    this.clicks = 0;
    this.transactionId = 0;
    this.lastClicks = 0;
    this.average = 0;
    this.averageSecondly = 0;
    this.standardDeviation = 0;
    this.clickHistory = new ArrayList<>();
  }
}
