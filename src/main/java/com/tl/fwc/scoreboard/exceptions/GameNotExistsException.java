package com.tl.fwc.scoreboard.exceptions;

import com.tl.fwc.scoreboard.Game.Players;

public class GameNotExistsException extends RuntimeException{

  public GameNotExistsException(Players players) {
    super("Game not exists: " + players);
  }
}
