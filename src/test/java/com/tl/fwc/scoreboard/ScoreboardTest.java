package com.tl.fwc.scoreboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tl.fwc.scoreboard.exceptions.GameNotExistsException;
import com.tl.fwc.scoreboard.exceptions.InvalidScoreException;
import com.tl.fwc.scoreboard.exceptions.TeamAlreadyPlaysGameException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


class ScoreboardTest {

  @Test
  void shouldStartNewGame() {
    Scoreboard scoreboard = new Scoreboard();
    Game game = scoreboard.startGame("Poland", "Norway");
    assertThat(game).isNotNull();
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Poland:Portugal",
      "Sweden:Norway"
  }, delimiter = ':')
  void shouldNotStartNewGameWithAlreadyPlayingTeam(String homeTeam, String awayTeam) {
    Scoreboard scoreboard = new Scoreboard();
    scoreboard.startGame("Poland", "Norway");
    assertThatThrownBy(() -> scoreboard.startGame(homeTeam, awayTeam))
        .isInstanceOf(TeamAlreadyPlaysGameException.class);
  }

  @Test
  void shouldFinishStartedGame() {
    Scoreboard scoreboard = new Scoreboard();
    Game startedGame = scoreboard.startGame("Poland", "Norway");
    Game finishedGame = scoreboard.finishGame("Poland", "Norway");
    assertThat(finishedGame)
        .isNotNull()
        .isEqualTo(startedGame);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Poland:Portugal",
      "Sweden:Norway",
      "Poland:Norway"
  }, delimiter = ':')
  void shouldStartNewGameWhenTeamFinishedPreviousMatch(String homeTeam, String awayTeam) {
    Scoreboard scoreboard = new Scoreboard();
    scoreboard.startGame("Poland", "Norway");
    scoreboard.finishGame("Poland", "Norway");
    assertThatNoException().isThrownBy(() -> scoreboard.startGame(homeTeam, awayTeam));
  }

  @Test
  void shouldThrowExceptionWhenTryingToFinishGameNotPresentInScoreboard() {
    Scoreboard scoreboard = new Scoreboard();
    assertThatThrownBy(() -> scoreboard.finishGame("Poland", "Norway"))
        .isInstanceOf(GameNotExistsException.class);
  }

  @Test
  void shouldUpdateScoreOfStartedGame() {
    Scoreboard scoreboard = new Scoreboard();
    scoreboard.startGame("Poland", "Norway");
    Game updatedGame = scoreboard.updateScore("Poland", "Norway", 1, 1);
    assertThat(updatedGame).isNotNull();
    assertThat(updatedGame.homeTeamScore()).isEqualTo(1);
    assertThat(updatedGame.awayTeamScore()).isEqualTo(1);
  }

  @Test
  void shouldThrowExceptionWhenUpdatingScoreOfGameThatHasNotStarted() {
    Scoreboard scoreboard = new Scoreboard();
    assertThatThrownBy(() -> scoreboard.updateScore("Poland", "Norway", 1, 1))
        .isInstanceOf(GameNotExistsException.class);
  }

  @Test
  void shouldThrowExceptionWhenUpdatingScoreWithInvalidValues() {
    Scoreboard scoreboard = new Scoreboard();
    scoreboard.startGame("Poland", "Norway");
    scoreboard.updateScore("Poland", "Norway", 1, 1);

    assertThatThrownBy(() -> scoreboard.updateScore("Poland", "Norway", 0, 0))
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
    assertThat(games).isEqualTo(expectedGamesOrder);
  }

  // add game to the scoreboard with the desired score and returns that game
  private Game addGame(Scoreboard scoreboard,
      String homeTeam, String awayTeam, int homeScore, int awayScore) {
    scoreboard.startGame(homeTeam, awayTeam);
    return scoreboard.updateScore(homeTeam, awayTeam, homeScore, awayScore);
  }
}
