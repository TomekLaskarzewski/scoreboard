package com.tl.fwc.scoreboard.exceptions;

import com.tl.fwc.scoreboard.Game.Players;

/**
 * Thrown when an operation is performed on a game that does not exist in the scoreboard.
 */
public class GameNotExistsException extends RuntimeException{

  public GameNotExistsException(Players players) {
    super("Game not exists: " + players);
  }
}
