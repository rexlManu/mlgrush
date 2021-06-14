package de.rexlmanu.mlgrush.plugin.detection;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Accessors(fluent = true)
@Data
public class Detection {

  private List<Integer> clickHistory, placeHistory;
  private boolean digging, placing;
  private long lastDiggingAction, lastPlacingAction, transactionPing, startTransactionTime;

  private int clicks, transactionId, lastClicks, places, lastPlaces;
  private double clickAverage, clickAverageSecondly, placeAverage, placeAverageSecondly;

  public Detection() {
    this.clickHistory = new ArrayList<>();
    this.placeHistory = new ArrayList<>();

    this.reset();
  }

  private void reset() {
    this.clickHistory.clear();
    this.placeHistory.clear();

    this.digging = false;
    this.placing = false;
    this.lastDiggingAction = 0;
    this.lastPlacingAction = 0;
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
  }
}
