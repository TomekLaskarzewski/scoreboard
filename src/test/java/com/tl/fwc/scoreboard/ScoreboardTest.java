package com.tl.fwc.scoreboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tl.fwc.scoreboard.exceptions.GameNotExistsException;
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
    assertThat(finishedGame.id()).isEqualTo(startedGame.id());
  }

  @Test
  void shouldThrowExceptionWhenTryingToFinishGameNotPresentInScoreboard() {
    Scoreboard scoreboard = new Scoreboard();
    assertThatThrownBy(() -> scoreboard.finishGame(UUID.randomUUID()))
        .isInstanceOf(GameNotExistsException.class);
  }
}
