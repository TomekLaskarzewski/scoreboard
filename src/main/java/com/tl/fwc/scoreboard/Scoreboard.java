package com.tl.fwc.scoreboard;

import com.tl.fwc.scoreboard.Game.Players;
import com.tl.fwc.scoreboard.exceptions.GameNotExistsException;
import com.tl.fwc.scoreboard.exceptions.TeamAlreadyPlaysGameException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Scoreboard {

  private final Object lock = new Object();

  private final Set<String> teams = new HashSet<>();
  private final Map<Players, Game> activeGames = new LinkedHashMap<>();

  public Game startGame(String homeTeam, String awayTeam) {
    Game game = Game.create(homeTeam, awayTeam);

    synchronized (lock) {
      registerTeams(game);
      activeGames.put(game.players(), game);
    }

    log.info("Created new game: {}", game);
    return game;
  }

  private void registerTeams(Game game) {
    // this method is called already inside the synchronized block
    if (teams.add(game.homeTeam())) {
      if (teams.add(game.awayTeam())) {
        return;
      }
      log.warn("Away team '{}' plays another game", game.awayTeam());
      teams.remove(game.homeTeam());
      throw new TeamAlreadyPlaysGameException(game.awayTeam());
    }
    log.warn("Home team '{}' plays another game", game.homeTeam());
    throw new TeamAlreadyPlaysGameException(game.homeTeam());
  }

  private void unregisterTeams(Game game) {
    // this method is called already inside the synchronized block
    teams.remove(game.homeTeam());
    teams.remove(game.awayTeam());
  }

  public Game finishGame(String homeTeam, String awayTeam) {
    Players players = Game.players(homeTeam, awayTeam);
    Game finishedGame;

    synchronized (lock) {
      finishedGame = activeGames.remove(players);
      if (finishedGame == null) {
        throw new GameNotExistsException(players);
      }
      unregisterTeams(finishedGame);
    }

    log.info("Finished game: {}", finishedGame);
    return finishedGame;
  }

  public Game updateScore(String homeTeam, String awayTeam, int homeTeamScore, int awayTeamScore) {
    Players players = Game.players(homeTeam, awayTeam);
    Game updatedGame;

    synchronized (lock) {
      updatedGame = activeGames.compute(players, (key, game) -> {
        if (game == null) {
          throw new GameNotExistsException(players);
        }
        return game.updateScore(homeTeamScore, awayTeamScore);
      });
    }

    log.info("Updated game score: {}", updatedGame);
    return updatedGame;
  }

  public List<Game> gamesByTotalScore() {
    synchronized (lock) {
      return activeGames.values().stream()
          .sorted(Game.TOTAL_SCORE_COMPARATOR_ASC)
          .toList()
          .reversed();
    }
  }
}
