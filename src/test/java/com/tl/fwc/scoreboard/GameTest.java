package com.tl.fwc.scoreboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tl.fwc.scoreboard.exceptions.InvalidScoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GameTest {

  @Test
  void shouldCreateNewGame() {
    Game game = Game.create("Poland", "Norway");
    assertThat(game.id()).isNotNull();
    assertThat(game.homeTeam()).isEqualTo("Poland");
    assertThat(game.awayTeam()).isEqualTo("Norway");
    assertThat(game.homeTeamScore()).isEqualTo(0);
    assertThat(game.awayTeamScore()).isEqualTo(0);
  }

  @Test
  void shouldReturnNewGameInstanceWithUpdatedScore() {
    Game game = Game.create("Poland", "Norway");
    Game updatedGame = game.updateScore(1, 1);
    assertThat(updatedGame).isNotSameAs(game);
    assertThat(game.id()).isEqualTo(updatedGame.id());
    assertThat(game.homeTeamScore()).isEqualTo(0);
    assertThat(game.awayTeamScore()).isEqualTo(0);
    assertThat(updatedGame.homeTeamScore()).isEqualTo(1);
    assertThat(updatedGame.awayTeamScore()).isEqualTo(1);
  }

  @ParameterizedTest
  @CsvSource(value = {"-1:0", "0:-1", "0:0"}, delimiter = ':')
  void shouldRejectUpdateWhichReduceScore(int homeTeamScore, int awayTeamScore) {
    Game game = Game.create("Poland", "Norway").toBuilder()
        .homeTeamScore(1)
        .awayTeamScore(1)
        .build();
    assertThatThrownBy(() -> game.updateScore(homeTeamScore, awayTeamScore))
      .isInstanceOf(InvalidScoreException.class);
  }


}