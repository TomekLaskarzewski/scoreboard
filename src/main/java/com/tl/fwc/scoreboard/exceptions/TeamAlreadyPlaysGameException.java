package com.tl.fwc.scoreboard.exceptions;

public class TeamAlreadyPlaysGameException extends RuntimeException {

  public TeamAlreadyPlaysGameException(String teamName) {
    super(String.format("Team already plays a game: %s", teamName));
  }
}
