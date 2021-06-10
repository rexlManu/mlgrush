package de.rexlmanu.mlgrush.plugin.queue;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PlayerQueue extends AbstractQueue<GamePlayer> {

  private List<GamePlayer> players;

  public PlayerQueue() {
    this.players = new LinkedList<>();
  }

  @Override
  public Iterator<GamePlayer> iterator() {
    return this.players.iterator();
  }

  @Override
  public int size() {
    return this.players.size();
  }

  @Override
  public boolean offer(GamePlayer gamePlayer) {
    if (gamePlayer == null) return false;
    this.players.add(gamePlayer);
    return true;
  }

  @Override
  public GamePlayer poll() {
    Iterator<GamePlayer> iterator = this.iterator();
    GamePlayer player = iterator.next();
    if (player != null) {
      iterator.remove();
      return player;
    }
    return null;
  }

  @Override
  public GamePlayer peek() {
    return this.iterator().next();
  }
}
