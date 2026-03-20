package com.tl.fwc.scoreboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tl.fwc.scoreboard.exceptions.GameNotExistsException;
import com.tl.fwc.scoreboard.exceptions.InvalidScoreException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;


public class ScoreboardTest {

  @Test
  void shouldStartNewGame() {
    Scoreboard scoreboard = new Scoreboard();
    Game game = scoreboard.startGame("Poland", "Norway");
    assertThat(game).isNotNull();
  }

  @Test
  void shouldFinishStartedGame() {
    Scoreboard scoreboard = new Scoreboard();
    Game startedGame = scoreboard.startGame("Poland", "Norway");
    Game finishedGame = scoreboard.finishGame(startedGame.id());
    assertThat(finishedGame).isNotNull();
    assertThat(finishedGame).isEqualTo(startedGame);
  }

  @Test
  void shouldThrowExceptionWhenTryingToFinishGameNotPresentInScoreboard() {
    Scoreboard scoreboard = new Scoreboard();
    assertThatThrownBy(() -> scoreboard.finishGame(UUID.randomUUID()))
        .isInstanceOf(GameNotExistsException.class);
  }

  @Test
  void shouldUpdateScoreOfStartedGame() {
    Scoreboard scoreboard = new Scoreboard();
    Game startedGame = scoreboard.startGame("Poland", "Norway");
    Game updatedGame = scoreboard.updateScore(startedGame.id(), 1, 1);
    assertThat(updatedGame).isNotNull();
    assertThat(updatedGame.homeTeamScore()).isEqualTo(1);
    assertThat(updatedGame.awayTeamScore()).isEqualTo(1);
  }

  @Test
  void shouldThrowExceptionWhenUpdatingScoreOfGameThatHasNotStarted() {
    Scoreboard scoreboard = new Scoreboard();
    assertThatThrownBy(() -> scoreboard.updateScore(UUID.randomUUID(), 1, 1))
        .isInstanceOf(GameNotExistsException.class);
  }

  @Test
  void shouldThrowExceptionWhenUpdatingScoreWithInvalidValues() {
    Scoreboard scoreboard = new Scoreboard();
    Game game = scoreboard.startGame("Poland", "Norway");
    scoreboard.updateScore(game.id(), 1, 1);

    assertThatThrownBy(() -> scoreboard.updateScore(game.id(), 0, 0))
        .isInstanceOf(InvalidScoreException.class);
  }

  /*
  given:
A. Mexico - Canada: 0 – 5
B. Spain - Brazil: 10 – 2
C. Germany - France: 2 – 2
D. Uruguay - Italy: 6 – 6
E. Argentina - Australia: 3 - 1

  expected:
D -> 1. Uruguay 6 - Italy 6
B -> 2. Spain 10 - Brazil 2
A -> 3. Mexico 0 - Canada 5
E -> 4. Argentina 3 - Australia 1
C -> 5. Germany 2 - France 2
   */
  @Test
  void shouldReturnSummaryOfGamesByTotalScore() {
    Scoreboard scoreboard = new Scoreboard();

    // start games and update their score to expected values
    Game gameA = addGame(scoreboard, "Mexico", "Canada", 0, 5);
    Game gameB = addGame(scoreboard, "Spain", "Brazil", 10, 2);
    Game gameC = addGame(scoreboard, "Germany", "France", 2, 2);
    Game gameD = addGame(scoreboard, "Uruguay", "Italy", 6, 6);
    Game gameE = addGame(scoreboard, "Argentina", "Australia", 3, 1);

    List<Game> expectedGamesOrder = List.of(gameD, gameB, gameA, gameE, gameC);

    List<Game> games = scoreboard.gamesByTotalScore();
    assertThat(games.equals(expectedGamesOrder)).isTrue();
  }

  // add game to the scoreboard with the desired score and returns that game
  private Game addGame(Scoreboard scoreboard,
      String homeTeam, String awayTeam, int homeScore, int awayScore) {
    Game game = scoreboard.startGame(homeTeam, awayTeam);
    return scoreboard.updateScore(game.id(), homeScore, awayScore);
  }
}
