package com.tl.fwc.scoreboard.exceptions;

import com.tl.fwc.scoreboard.Game;

/**
 * Thrown when an invalid score update is attempted.
 */
public class InvalidScoreException extends RuntimeException{

  public InvalidScoreException(Game game, int newHomeTeamScore, int newAwayTeamScore) {
    super(String.format(
        "Invalid score update for game %s. New score (%d - %d) must not be lower than current",
        game, newHomeTeamScore, newAwayTeamScore));
  }
}
