package com.tl.fwc.scoreboard;

import com.tl.fwc.scoreboard.exceptions.GameNotExistsException;
import com.tl.fwc.scoreboard.exceptions.TeamAlreadyPlaysGameException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Scoreboard {

  private final Set<String> teams = new HashSet<>();
  private final Map<UUID, Game> activeGames = Collections.synchronizedMap(new LinkedHashMap<>());

  public Game startGame(String homeTeam, String awayTeam) {
    Game game = Game.create(homeTeam, awayTeam);
    registerTeams(game);
    activeGames.put(game.id(), game);
    log.info("Created new game: {}", game);
    return game;
  }

  private void registerTeams(Game game) {
    synchronized (teams) {
      if (teams.add(game.homeTeam())) {
        if (teams.add(game.awayTeam())) {
          return;
        }
        teams.remove(game.homeTeam());
        throw new TeamAlreadyPlaysGameException(game.awayTeam());
      }
      throw new TeamAlreadyPlaysGameException(game.homeTeam());
    }
  }

  private void unregisterTeams(Game game) {
    synchronized (teams) {
      teams.remove(game.homeTeam());
      teams.remove(game.awayTeam());
    }
  }

  public Game finishGame(UUID gameId) {
    return Optional.ofNullable(activeGames.remove(gameId))
        .map(finishedGame -> {
          unregisterTeams(finishedGame);
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

  public List<Game> gamesByTotalScore() {
    return activeGames.values().stream().sorted().toList().reversed();
  }
}
