package com.tl.fwc.scoreboard;

import com.tl.fwc.scoreboard.exceptions.GameNotExistsException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Scoreboard {

  private final Map<UUID, Game> activeGames = new ConcurrentHashMap<>();

  public Game startGame(String homeTeam, String awayTeam) {
    Game game = new Game(homeTeam, awayTeam);
    activeGames.put(game.id(), game);
    return game;
  }

  public Game finishGame(UUID gameId) {
    return Optional.ofNullable(activeGames.remove(gameId))
        .orElseThrow(() -> new GameNotExistsException(gameId));
  }
}
