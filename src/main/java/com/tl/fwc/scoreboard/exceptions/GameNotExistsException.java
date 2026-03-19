package com.tl.fwc.scoreboard.exceptions;

import java.util.UUID;

public class GameNotExistsException extends RuntimeException{

  public GameNotExistsException(UUID gameId) {
    super("Game not exists: " + gameId);
  }
}
