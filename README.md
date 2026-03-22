# Live Football World Cup Score Board

This project implements a simple in-memory scoreboard for tracking live football games.

The goal was not only to meet the functional requirements, but also to make clear and pragmatic design decisions around data modeling, ordering, and concurrency.

---

## Requirements

This project requires **Java 17 or newer**.

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

The scoreboard is implemented as a single in-memory aggregate:

* `activeGames` — stores currently active games
* `teams` — tracks teams already involved in a game

Both structures are protected by a single lock to ensure consistency.

### Why a single lock?

Although finer-grained locking could improve concurrency, both collections represent a single domain concept (active games).

Using multiple locks would:

* introduce risk of inconsistent state
* require careful lock ordering
* potentially lead to deadlocks

A single lock keeps the model simple and safe.

---

## Data structures

A `LinkedHashMap` is used to store active games.

This choice allows:

* O(1) average-time access by key
* preservation of insertion order

Insertion order is later used as a natural tie-breaker when sorting games with equal scores.

---

## Ordering strategy

The scoreboard is generated on demand:

1. Games are sorted by total score (ascending)
2. The result is reversed

Because sorting is stable, insertion order is preserved for games with equal scores.

This avoids the need for:

* additional fields (timestamps, sequence numbers)
* maintaining a continuously sorted structure

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

All operations are synchronized using a single lock.

This guarantees:

* atomic updates across multiple structures
* no intermediate inconsistent states
* deterministic ordering of results

### Why not snapshots?

Snapshot-based reads were considered to reduce lock contention.

However, they allow situations where:

* a newer result is returned before an older one
* responses are not globally ordered

This implementation favors consistency and predictability over maximum concurrency.

---

## Complexity

* Insert / update / remove: **O(1)** average
* Scoreboard generation: **O(n log n)**

---

## Trade-offs

The implementation intentionally prioritizes:

* simplicity
* correctness
* clarity of domain rules

over:

* maximum throughput
* advanced concurrent data structures

This makes the code easier to reason about and maintain.
