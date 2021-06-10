package de.rexlmanu.mlgrush.plugin.event.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class GameEvent<E> {

  private E target;

}
