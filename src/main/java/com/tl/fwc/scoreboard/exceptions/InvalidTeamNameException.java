package com.tl.fwc.scoreboard.exceptions;

import com.tl.fwc.scoreboard.Game;

public class InvalidTeamNameException extends RuntimeException {

  public InvalidTeamNameException(String teamName) {
    super(String.format("Team name must not be null or empty. Provided: '%s'", teamName));
  }
}
