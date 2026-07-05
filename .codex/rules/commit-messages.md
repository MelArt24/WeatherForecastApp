# Commit Message Rules

After every completed task that modifies code, always provide a Conventional Commit message.

The commit message must explain:

1. Why the change was needed.
2. What architectural or behavioral change was made.
3. What benefit was achieved.
4. Whether existing behavior was preserved.

## Format

type(scope): Summary

Problem / motivation.

Solution.

Result / guarantee.

## Allowed types

* feat
* fix
* refactor
* test
* docs
* build
* chore

## Requirements

### Title

Use Conventional Commits format:

type(scope): Summary

Examples:

* fix(wishlist): Handle empty server response
* refactor(cart): Consolidate screen state into CartUiState
* refactor(auth): Split screen-specific UiState models

Use imperative mood.

The title should describe the primary outcome of the change, not the files that were modified.

### Body

The body should explain:

* what problem existed before the change
* why the change was necessary
* what solution was introduced
* what benefit was achieved

Prefer architectural intent to implementation details.

### Good examples

refactor(cart): Consolidate screen state into CartUiState

CartViewModel exposed multiple independent StateFlows that
represented different parts of the same screen state.

Introduce CartUiState as a single screen-level state model and
combine repository-backed state with local ViewModel state into
one observable StateFlow.

This simplifies UI state collection and aligns the cart feature
with the UiState architecture without changing business logic
or repository behavior.

---

fix(wishlist): Reduce synchronization bottlenecks

Wishlist mutations were serialized through a global mutex,
causing slow UI updates and redundant refresh requests.

Introduce per-product and per-item synchronization together
with optimistic local updates and batched reconciliation.

This preserves existing behavior while allowing independent
wishlist operations to proceed concurrently.

## Bad examples

refactor(cart): Update CartViewModel

fix(wishlist): Improve logic

refactor(auth): Cleanup code

test(cart): Update tests

These examples describe implementation activity rather than
the intent and outcome of the change.

## Architecture And Refactoring Commits

For architecture and refactoring commits:

Focus on:
- the problem being solved
- the architectural decision
- the resulting benefit

Do not focus on:
- file names
- code movement
- implementation details

unless those details are essential for understanding the change.

## Formatting

- Keep the title concise (preferably under 50 characters).
- Wrap commit message body lines at 72 characters maximum.
- Separate title and body with a blank line.
- Use short paragraphs.