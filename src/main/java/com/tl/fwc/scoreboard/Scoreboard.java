package com.tl.fwc.scoreboard;

import com.tl.fwc.scoreboard.Game.Players;
import com.tl.fwc.scoreboard.exceptions.GameNotExistsException;
import com.tl.fwc.scoreboard.exceptions.TeamAlreadyPlaysGameException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Scoreboard {

  private final Set<String> teams = new HashSet<>();
  private final Map<Players, Game> activeGames = Collections.synchronizedMap(new LinkedHashMap<>());

  public Game startGame(String homeTeam, String awayTeam) {
    Game game = Game.create(homeTeam, awayTeam);
    registerTeams(game);
    activeGames.put(game.players(), game);
    log.info("Created new game: {}", game);
    return game;
  }

  private void registerTeams(Game game) {
    synchronized (teams) {
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
  }

  private void unregisterTeams(Game game) {
    synchronized (teams) {
      teams.remove(game.homeTeam());
      teams.remove(game.awayTeam());
    }
  }

  public Game finishGame(String homeTeam, String awayTeam) {
    Players players = Game.players(homeTeam, awayTeam);
    return Optional.ofNullable(activeGames.remove(players))
        .map(finishedGame -> {
          unregisterTeams(finishedGame);
          log.info("Finished game: {}", finishedGame);
          return finishedGame;
        })
        .orElseThrow(() -> new GameNotExistsException(players));
  }

  public Game updateScore(String homeTeam, String awayTeam, int homeTeamScore, int awayTeamScore) {
    return activeGames.compute(Game.players(homeTeam, awayTeam), (players, game) -> {
      if (game == null) {
        throw new GameNotExistsException(players);
      }
      Game updatedGame = game.updateScore(homeTeamScore, awayTeamScore);
      log.info("Updated game score: {}", updatedGame);
      return updatedGame;
    });
  }

  public List<Game> gamesByTotalScore() {
    return activeGames.values().stream()
        .sorted(Game.TOTAL_SCORE_COMPARATOR_ASC)
        .toList()
        .reversed();
  }
}
