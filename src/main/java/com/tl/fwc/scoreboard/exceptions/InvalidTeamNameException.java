package com.tl.fwc.scoreboard.exceptions;

/**
 * Thrown when a team name is null, blank or otherwise invalid.
 */
public class InvalidTeamNameException extends RuntimeException {

  public InvalidTeamNameException(String teamName) {
    super("Invalid team name: '" + teamName + "' (must not be null or blank)");
  }
}
