package com.tl.fwc.scoreboard.exceptions;

import com.tl.fwc.scoreboard.Game;

public class InvalidScoreException extends RuntimeException{

  public InvalidScoreException(Game game, int newHomeTeamScore, int newAwayTeamScore) {
    super(String.format("Invalid values for new score: [%d:%s]. Current game: %s",
        newHomeTeamScore, newAwayTeamScore, game));
  }
}
