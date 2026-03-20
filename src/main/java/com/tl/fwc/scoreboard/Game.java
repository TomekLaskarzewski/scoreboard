package com.tl.fwc.scoreboard;

import com.tl.fwc.scoreboard.exceptions.InvalidScoreException;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PACKAGE, toBuilder = true) // package private for testing
@Getter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game implements Comparable<Game> {

  @Default
  private UUID id = UUID.randomUUID();
  @EqualsAndHashCode.Include
  private String homeTeam;
  @EqualsAndHashCode.Include
  private String awayTeam;
  @Default
  private int homeTeamScore = 0;
  @Default
  private int awayTeamScore = 0;

  public static Game create(String homeTeam, String awayTeam) {
    return Game.builder()
        .homeTeam(homeTeam)
        .awayTeam(awayTeam)
        .build();
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
  public int compareTo(Game o) {
    return Integer.compare(this.totalScore(), o.totalScore());
  }
}
