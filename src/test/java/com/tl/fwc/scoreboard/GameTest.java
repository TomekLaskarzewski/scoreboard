package com.tl.fwc.scoreboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tl.fwc.scoreboard.Game.Players;
import com.tl.fwc.scoreboard.exceptions.InvalidScoreException;
import com.tl.fwc.scoreboard.exceptions.InvalidTeamNameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class GameTest {

  @Test
  void shouldCreateNewGame() {
    Game game = Game.create("Poland", "Norway");
    Players players = Game.players("Poland", "Norway");
    assertThat(game.players()).isEqualTo(players);
    assertThat(game.homeTeam()).isEqualTo("Poland");
    assertThat(game.awayTeam()).isEqualTo("Norway");
    assertThat(game.homeTeamScore()).isZero();
    assertThat(game.awayTeamScore()).isZero();
  }

  @Test
  void shouldIdentifyTheSameGameByPlayingTeams() {
    Game game1 = Game.create("Poland", "Norway");
    Game game2 = Game.create("Poland", "Norway");
    Game game3 = Game.create("Norway", "Poland");
    assertThat(game1)
        .isEqualTo(game2)
        .isNotEqualTo(game3);
  }

  @Test
  void shouldProvideGameComparatorOnTotalScore() {
    Game game1 = Game.create("Poland", "Norway")
        .updateScore(1,0);
    Game game2 = Game.create("Poland", "Norway");
    Game game3 = Game.create("Norway", "Poland");

    // total score of game1 (1) is greater than game2 (0)
    assertThat(Game.TOTAL_SCORE_COMPARATOR_ASC.compare(game1, game2)).isGreaterThan(0);
    assertThat(Game.TOTAL_SCORE_COMPARATOR_ASC.compare(game2, game1)).isLessThan(0);
    assertThat(Game.TOTAL_SCORE_COMPARATOR_ASC.compare(game2, game3)).isZero();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "\t", "\n"})
  void shouldRejectStartingGameForHomeTeamWithInvalidName(String teamName) {
    assertThatThrownBy(() -> Game.create(teamName, "Poland"))
        .isInstanceOf(InvalidTeamNameException.class);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "\t", "\n"})
  void shouldRejectStartingGameForAwayTeamWithInvalidName(String teamName) {
    assertThatThrownBy(() -> Game.create("Poland", teamName))
        .isInstanceOf(InvalidTeamNameException.class);
  }

  @Test
  void shouldReturnNewGameInstanceWithUpdatedScore() {
    Game game = Game.create("Poland", "Norway");
    Players players = Game.players("Poland", "Norway");
    Game updatedGame = game.updateScore(1, 1);
    assertThat(updatedGame).isNotSameAs(game);
    assertThat(game.players()).isEqualTo(players);
    assertThat(game.homeTeamScore()).isZero();
    assertThat(game.awayTeamScore()).isZero();
    assertThat(updatedGame.homeTeamScore()).isEqualTo(1);
    assertThat(updatedGame.awayTeamScore()).isEqualTo(1);
  }

  @ParameterizedTest
  @CsvSource(value = {"-1:0", "0:-1", "0:0"}, delimiter = ':')
  void shouldRejectUpdateWhichReduceScore(int homeTeamScore, int awayTeamScore) {
    Game game = Game.create("Poland", "Norway").updateScore(1, 1);
    assertThatThrownBy(() -> game.updateScore(homeTeamScore, awayTeamScore))
      .isInstanceOf(InvalidScoreException.class);
  }
}