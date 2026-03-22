package com.tl.fwc.scoreboard.exceptions;

/**
 * Thrown when a team is already participating in another active game.
 */
public class TeamAlreadyPlaysGameException extends RuntimeException {

  public TeamAlreadyPlaysGameException(String teamName) {
    super("Team already plays another game: " + teamName);
  }
}
