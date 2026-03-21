package com.tl.fwc.scoreboard;

import com.tl.fwc.scoreboard.exceptions.InvalidScoreException;
import com.tl.fwc.scoreboard.exceptions.InvalidTeamNameException;
import jakarta.annotation.Nonnull;
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

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game implements Comparable<Game> {

  public record Players(String homeTeam, String awayTeam) {

    @Override
    @Nonnull
    public String toString() {
      return homeTeam + " - " + awayTeam;
    }
  }

  @EqualsAndHashCode.Include
  @Delegate
  private final Players players;
  @Default
  private final int homeTeamScore = 0;
  @Default
  private final int awayTeamScore = 0;

  public static Players players(String homeTeam, String awayTeam) {
    return new Players(homeTeam, awayTeam);
  }

  public static Game create(String homeTeam, String awayTeam) {
    Players players = players(trimTeamName(homeTeam), trimTeamName(awayTeam));
    return Game.builder().players(players).build();
  }

  private static String trimTeamName(String teamName) {
    return Optional.ofNullable(StringUtils.trimToNull(teamName))
        .orElseThrow(() -> new InvalidTeamNameException(teamName));
  }

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

  @Override
  public int compareTo(Game o) {
    return Integer.compare(this.totalScore(), o.totalScore());
  }
}
