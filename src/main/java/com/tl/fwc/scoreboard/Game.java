package com.tl.fwc.scoreboard;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class Game {

  private final UUID id = UUID.randomUUID();
  private final String homeTeam;
  private final String awayTeam;
}
