package de.rexlmanu.mlgrush.plugin.detection;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Accessors(fluent = true)
@Data
public class Detection {

  private boolean digging;
  private long lastDiggingAction, transactionPing, startTransactionTime;

  private int clicks, transactionId, lastClicks, places, lastPlaces;
  private double clickAverage, clickAverageSecondly, placeAverage, placeAverageSecondly;
  private List<Integer> clickHistory, placeHistory;

  public Detection() {
    this.digging = false;
    this.lastDiggingAction = 0;
    this.startTransactionTime = 0;
    this.clicks = 0;
    this.transactionId = 0;
    this.lastClicks = 0;
    this.places = 0;
    this.lastPlaces = 0;
    this.clickAverage = 0;
    this.clickAverageSecondly = 0;
    this.placeAverage = 0;
    this.placeAverageSecondly = 0;
    this.clickHistory = new ArrayList<>();
    this.placeHistory = new ArrayList<>();
  }
}
