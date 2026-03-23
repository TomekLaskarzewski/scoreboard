# Live Football World Cup Score Board

This project implements a simple in-memory scoreboard for tracking live football games.

The goal was not only to meet the functional requirements, but also to make clear and pragmatic design decisions around data modeling, ordering, and concurrency.

---

## Requirements

This project requires Java 17 or newer.

The solution uses modern Java language features (e.g. records), so it is not compatible with older Java versions.

--- 

## Build setup

The project was initially bootstrapped using Spring Initializr to quickly generate a working Gradle setup and test environment.

Although the implementation itself does not depend on Spring, Spring Boot dependencies were kept for convenience (dependency management and test setup).

It also provides a straightforward starting point in case this project evolves into a Spring-based application.

For a production-ready library, a lighter setup (plain Gradle with only required dependencies) would be more appropriate.

---

## Key assumptions

* Games are ordered by total score (descending)
* If two games have the same total score, the one started later appears first
* Score updates must be non-decreasing (scores cannot be reduced)
* A team cannot participate in more than one active game at the same time

---

## Design overview

The scoreboard is implemented as an in-memory model composed of:

* `activeGames` — stores currently active games
* `teams` — tracks teams already involved in a game

`activeGames` is backed by `ConcurrentHashMap`, which allows concurrent score updates.

A separate lock is used for lifecycle operations that must keep both internal structures consistent, such as:
* starting a game
* finishing a game

---

## Data structures

A `ConcurrentHashMap` is used to store active games.

This choice allows:

* O(1) average-time access by key
* concurrent score updates
* lock-free reads for scoreboard generation

Game ordering does not depend on map insertion order. Instead, it is derived explicitly from game data:
* total score
* game start time

---

## Ordering strategy

Games are ordered by:

1. Total score (descending)
2. Start time (most recently started game first for equal scores)

The implementation sorts games using an ascending comparator and then reverses the final result.

---

## Immutability

`Game` is implemented as an immutable object.

Each update produces a new instance instead of mutating existing state.

Benefits:

* no shared mutable state
* no need for defensive copying of returned objects
* simpler reasoning about concurrency

---

## Concurrency model

The implementation uses a mixed concurrency model:

* `ConcurrentHashMap` is used for active games
* a dedicated lock protects operations that must keep `teams` and `activeGames` consistent
  (starting and finishing games)

This allows:

* concurrent score updates without global locking
* lock-free read operations for the scoreboard view

The scoreboard view is generated from a weakly consistent snapshot of the current state:

* it does not block writes
* it may reflect concurrent updates
* it may contain a mix of slightly older and newer values

Because `Game` is immutable, readers never observe partially updated objects.

---

## Complexity

* Start / update / finish operations: **O(1)** average for map access and update
* Scoreboard generation: **O(n log n)** due to sorting

This is a deliberate trade-off: writes remain simple, while ordering is computed on demand.

---

## Trade-offs

The implementation intentionally prioritizes:

* simplicity
* correctness of domain rules
* low-friction concurrent reads
* clear separation between lifecycle consistency and scoreboard view generation

over:

* strict read consistency
* maintaining a continuously sorted structure
* more advanced synchronization strategies

This keeps the implementation relatively simple while allowing concurrent updates and non-blocking reads.
