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
    Game game = Game.create(homeTeam, awayTeam);
    activeGames.put(game.id(), game);
    log.info("Created new game: {}", game);
    return game;
  }

  public Game finishGame(UUID gameId) {
    return Optional.ofNullable(activeGames.remove(gameId))
        .map(finishedGame -> {
          log.info("Finished game: {}", finishedGame);
          return finishedGame;
        })
        .orElseThrow(() -> new GameNotExistsException(gameId));
  }

  public Game updateScore(UUID gameId, int homeTeamScore, int awayTeamScore) {
    return activeGames.compute(gameId, (id, game) -> {
      if (game == null) {
        throw new GameNotExistsException(gameId);
      }
      Game updatedGame = game.updateScore(homeTeamScore, awayTeamScore);
      log.info("Updated game score: {}", updatedGame);
      return updatedGame;
    });
  }
}
