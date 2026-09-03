# AiLikeGPT — Production Workflow

This repository uses a strict separation between **technical discussion** and **production execution**.

The authoritative operational protocol and individual production Work Packages are stored in Google Drive under:

`AiLikeGPT / Project Information / Production Work Packages`

## Two chat types

### Discussion chat

The discussion chat is used to make technical decisions, choose the next unfinished Locked Master Plan point, and prepare the complete Work Package for that point before a production chat begins.

### Production chat

A production chat executes **exactly one** assigned Locked Master Plan point from its Google Drive Work Package.

It must:

1. Read `AGENTS.md`.
2. Read `PROJECT_PLAN_LOCKED.md` in full.
3. Read `PROJECT_CONTEXT.md`.
4. Read the assigned Google Drive Work Package in full.
5. Inspect current repository state before changing code.
6. Execute all implementation phases required by that package.
7. Continue through debugging, tests, repairs, and verification until every Definition of Done item is satisfied.
8. Update repository/project context and the Work Package with completion evidence.
9. Stop after the assigned plan point is complete and **do not begin another plan point**.

A production chat must not end merely because a subphase, commit, scaffold, first build attempt, or intermediate milestone is complete. If the assigned plan point expands into many technical phases, all necessary phases remain part of the same production chat.

The only permitted incomplete ending is a **proven external blocker** outside the repository and outside the tools available to the chat. Source errors, compiler errors, test failures, build failures, and integration bugs are work to repair in the same production chat, not blockers.

## Work Package states

Google Drive packages are organized as:

- `Active` — exactly the package currently ready for a production chat.
- `Completed` — plan points finished with verification evidence.
- `Blocked` — only externally blocked packages with documented evidence.
- `Templates` — standard package structure for future assignments.

## Current first production gate

The first package prepared under this workflow is:

`WP-087 — Complete and Verify CI Build Pipeline`

It owns Locked Master Plan point **87** only. Its detailed scope, repair loop, verification requirements, and Definition of Done live in the Google Drive Work Package and must be read there before execution.

This workflow remains in force until the project owner explicitly changes it.
