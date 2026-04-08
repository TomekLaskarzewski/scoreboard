package com.tl.fwc.scoreboard;

import com.tl.fwc.scoreboard.Game.Players;
import com.tl.fwc.scoreboard.exceptions.GameNotExistsException;
import com.tl.fwc.scoreboard.exceptions.TeamAlreadyPlaysGameException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NoArgsConstructor;
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
 * <p>The implementation is thread-safe and uses a mixed concurrency model:
 * <ul>
 *   <li>{@code ConcurrentHashMap} allows concurrent score updates</li>
 *   <li>a dedicated lock protects operations that must keep {@code teams} and {@code activeGames}
 *   consistent (start/finish game)</li>
 * </ul>
 *
 * <p>Read operations are lock-free and return a snapshot view of the current state.
 * The snapshot is weakly consistent and may reflect concurrent updates.
 *
 * <p>{@link Game} instances are immutable, so readers never observe partially updated state.
 */
@NoArgsConstructor
@Slf4j
public class Scoreboard {

  private final Object lock = new Object();

  private final Set<String> teams = new HashSet<>();
  private final Map<Players, Game> activeGames = new ConcurrentHashMap<>();

  /**
   * Creates a scoreboard initialized with an existing collection of active games.
   *
   * <p>This constructor is package-private on purpose. It can be used internally to restore
   * scoreboard state, for example from a backup or persisted snapshot.
   *
   * <p>The provided games are re-registered using standard scoreboard rules.
   *
   * @param activeGames active games used to initialize the scoreboard
   * @throws TeamAlreadyPlaysGameException if restored games violate the rule that a team can
   *                                       participate in only one active game at a time
   */
  Scoreboard(List<Game> activeGames) {
    activeGames.forEach(this::startGame);
  }

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
    return startGame(Game.create(homeTeam, awayTeam));
  }

  private Game startGame(Game game) {
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

    updatedGame = activeGames.compute(players, (key, game) -> {
      if (game == null) {
        throw new GameNotExistsException(players);
      }
      return game.updateScore(homeTeamScore, awayTeamScore);
    });

    log.info("Updated game score: {}", updatedGame);
    return updatedGame;
  }

  /**
   * Returns active games ordered by total score.
   *
   * <p>Ordering rules:
   * <ul>
   *   <li>The higher total score comes first</li>
   *   <li>If scores are equal, the most recently started game comes first</li>
   * </ul>
   *
   * <p>This method does not use locking. It returns a snapshot based on the current
   * state of the underlying data structure.
   *
   * <p>The result is weakly consistent:
   * <ul>
   *   <li>it may reflect concurrent updates</li>
   *   <li>it does not block writes</li>
   *   <li>it never exposes partially updated {@link Game} instances</li>
   * </ul>
   *
   * @return ordered snapshot of active games
   */
  public List<Game> gamesByTotalScore() {
    return activeGames.values().stream().sorted(Game.TOTAL_SCORE_COMPARATOR_DESC).toList();
  }
}
