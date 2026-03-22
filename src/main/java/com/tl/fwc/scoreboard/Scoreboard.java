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

/**
 * In-memory scoreboard for tracking live football games.
 *
 * <p>The scoreboard maintains a collection of active games and enforces the following rules:
 * <ul>
 *   <li>A team can participate in only one active game at a time</li>
 *   <li>Scores can only increase (no rollback allowed)</li>
 *   <li>Games are ordered by total score (descending)</li>
 *   <li>If total scores are equal, games started later appear first</li>
 * </ul>
 *
 * <p>The implementation is thread-safe and uses a single lock to ensure consistency
 * between internal data structures.
 */
@Slf4j
public class Scoreboard {

  private final Object lock = new Object();

  private final Set<String> teams = new HashSet<>();
  private final Map<Players, Game> activeGames = new LinkedHashMap<>();

  /**
   * Starts a new game between two teams.
   *
   * <p>The game is initialized with a score of {@code 0 - 0} and added to the scoreboard.
   * Both teams must not already be participating in another active game.
   *
   * @param homeTeam name of the home team (must be non-null and non-blank)
   * @param awayTeam name of the away team (must be non-null and non-blank)
   * @return the created {@link Game} instance
   * @throws TeamAlreadyPlaysGameException if either team is already playing another game
   */
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

  /**
   * Finishes an active game and removes it from the scoreboard.
   *
   * <p>Both teams are released and can participate in new games after this operation.
   *
   * @param homeTeam name of the home team
   * @param awayTeam name of the away team
   * @return the finished {@link Game}
   * @throws GameNotExistsException if the specified game does not exist
   */
  public Game finishGame(String homeTeam, String awayTeam) {
    Players players = new Players(homeTeam, awayTeam);
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

  /**
   * Updates the score of an existing game.
   *
   * <p>The update is validated to ensure that scores do not decrease.
   * The underlying {@link Game} instance is immutable, so a new instance is created and replaces
   * the previous one.
   *
   * @param homeTeam      name of the home team
   * @param awayTeam      name of the away team
   * @param homeTeamScore new score of the home team (must be >= current score)
   * @param awayTeamScore new score of the away team (must be >= current score)
   * @return updated {@link Game} instance
   * @throws GameNotExistsException if the specified game does not exist
   */
  public Game updateScore(String homeTeam, String awayTeam, int homeTeamScore, int awayTeamScore) {
    Players players = new Players(homeTeam, awayTeam);
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

  /**
   * Returns a list of active games ordered by total score.
   *
   * <p>Ordering rules:
   * <ul>
   *   <li>Higher total score comes first</li>
   *   <li>If scores are equal, the most recently started game comes first</li>
   * </ul>
   *
   * <p>The returned list is a snapshot and is not affected by subsequent updates.
   *
   * @return ordered list of active games
   */
  public List<Game> gamesByTotalScore() {
    synchronized (lock) {
      return activeGames.values().stream().sorted(Game.TOTAL_SCORE_COMPARATOR_ASC).toList()
          .reversed();
    }
  }
}
