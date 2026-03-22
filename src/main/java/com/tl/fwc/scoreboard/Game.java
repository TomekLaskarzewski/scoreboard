package com.tl.fwc.scoreboard;

import com.tl.fwc.scoreboard.exceptions.InvalidScoreException;
import com.tl.fwc.scoreboard.exceptions.InvalidTeamNameException;
import jakarta.annotation.Nonnull;
import java.util.Comparator;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a football game between two teams.
 *
 * <p>This class is immutable. Any update (e.g. score change) results in creation of a new instance
 * rather than modifying the existing one.
 *
 * <p>The identity of a game is defined by the participating teams ({@link Players}). Two games
 * with the same teams are considered equal regardless of their current score.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game {

  /**
   * Represents a pair of teams participating in a game.
   *
   * <p>This record acts as a compound key and uniquely identifies a game within the scoreboard.
   *
   * <p>Team names are validated and normalized during construction:
   * <ul>
   *   <li>leading and trailing whitespace is removed</li>
   *   <li>null or blank values are not allowed</li>
   * </ul>
   *
   * @param homeTeam name of the home team (must be non-null and non-blank)
   * @param awayTeam name of the away team (must be non-null and non-blank)
   */
  public record Players(String homeTeam, String awayTeam) {

    /**
     * Canonical constructor that validates and normalizes team names.
     *
     * @throws InvalidTeamNameException if any team name is null, blank or invalid
     */
    public Players {
      homeTeam = trimTeamName(homeTeam);
      awayTeam = trimTeamName(awayTeam);
    }

    private static String trimTeamName(String teamName) {
      return Optional.ofNullable(StringUtils.trimToNull(teamName))
          .orElseThrow(() -> new InvalidTeamNameException(teamName));
    }

    @Override
    @Nonnull
    public String toString() {
      return homeTeam + " - " + awayTeam;
    }
  }

  /**
   * Unique identifier of the game based on participating teams.
   */
  @EqualsAndHashCode.Include
  @Delegate
  private final Players players;

  /**
   * Current score of the home team.
   */
  @Default
  private final int homeTeamScore = 0;

  /**
   * Current score of the away team.
   */
  @Default
  private final int awayTeamScore = 0;

  /**
   * Comparator for sorting games by total score in ascending order.
   */
  public static final Comparator<Game> TOTAL_SCORE_COMPARATOR_ASC =
      Comparator.comparing(Game::totalScore);

  /**
   * Creates a new game with initial score {@code 0 - 0}.
   *
   * <p>Team names are validated and normalized (trimmed). Blank or null values are not allowed.
   *
   * @param homeTeam name of the home team
   * @param awayTeam name of the away team
   * @return new {@link Game} instance
   * @throws InvalidTeamNameException if any team name is null, blank or invalid
   */
  public static Game create(String homeTeam, String awayTeam) {
    return Game.builder()
        .players(new Players(homeTeam, awayTeam))
        .build();
  }

  /**
   * Updates the score of this game.
   *
   * <p>Scores are validated to ensure they do not decrease. The returned instance represents a new
   * state of the game.
   *
   * @param newHomeTeamScore new score for the home team (must be >= current score)
   * @param newAwayTeamScore new score for the away team (must be >= current score)
   * @return new {@link Game} instance with updated scores
   * @throws InvalidScoreException if the new score is lower than the current score
   */
  public Game updateScore(int newHomeTeamScore, int newAwayTeamScore) {
    if (newHomeTeamScore < homeTeamScore || newAwayTeamScore < awayTeamScore) {
      throw new InvalidScoreException(this, newHomeTeamScore, newAwayTeamScore);
    }

    return this.toBuilder()
        .homeTeamScore(newHomeTeamScore)
        .awayTeamScore(newAwayTeamScore)
        .build();
  }

  private int totalScore() {
    return homeTeamScore + awayTeamScore;
  }

  @Override
  public String toString() {
    return String.format("%s: %s - %s", players, homeTeamScore, awayTeamScore);
  }
}
