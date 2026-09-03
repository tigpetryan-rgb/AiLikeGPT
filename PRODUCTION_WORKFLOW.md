# AiLikeGPT — Production Workflow

This repository uses a strict separation between **technical discussion**, **failure investigation**, and **production execution**.

The authoritative operational protocol and individual Work Packages are stored in Google Drive under:

`AiLikeGPT / Project Information / Production Work Packages`

## Three chat types

### Discussion chat

The discussion chat is used to make technical decisions, choose the next unfinished Locked Master Plan point, and prepare the complete Work Package for that point before a production chat begins.

### Failure Investigation chat

A Failure Investigation chat diagnoses **one concrete failed CI/build/production result** from its assigned Google Drive Failure package.

It must:

1. Read the assigned Failure package and the Failure Investigation Protocol in Google Drive.
2. Read `AGENTS.md`, `PROJECT_PLAN_LOCKED.md`, `PROJECT_CONTEXT.md`, this file, and the related production Work Package.
3. Inspect the exact failed run/job/commit/workflow and gather the strongest available evidence.
4. Identify the earliest defensible failure boundary, root cause, confidence, evidence, and rejected hypotheses.
5. Remain **read-only with respect to the production fix**: do not implement the fix, do not commit a fix, and do not turn the failure chat into a production chat.
6. Write the diagnosis back into the Failure package.
7. **Automatically route the result to production before ending**: create a new repair Work Package under `Production Work Packages / Active`, or update the explicitly designated production package when the Failure package requires reuse.
8. The repair package must contain the diagnosed root cause, exact fix objective, allowed changes, repair loop, verification requirements, relationship to the same Locked Plan point, and full Definition of Done.
9. Verify the repair package by Drive readback.
10. Move the Failure package from `Failures / Active` to `Failures / Resolved` only after production routing succeeds.
11. End with one short launch sentence naming the exact Active production repair package for the next production chat.

A Failure Investigation chat is incomplete if it produces only analysis/reporting without creating or updating the production repair package.

Even when the diagnosis ends at a proven external account/platform/UI restriction, the Failure chat must still create an Active production repair package. That production package must exhaust connected capabilities and reduce any truly unavoidable external requirement to exactly one user/account action or datum.

### Production chat

A production chat executes **exactly one** assigned Locked Master Plan point from its Google Drive Work Package, including any repair package tied to that same point.

It must:

1. Read `AGENTS.md`.
2. Read `PROJECT_PLAN_LOCKED.md` in full.
3. Read `PROJECT_CONTEXT.md`.
4. Read the assigned Google Drive Work Package in full.
5. If the package is a failure-derived repair, read the referenced resolved Failure package before changing code/settings.
6. Inspect current repository state before changing code.
7. Execute all implementation/repair phases required by that package.
8. Continue through debugging, tests, repairs, reruns, and verification until every Definition of Done item is satisfied.
9. Update repository/project context and the Work Package with completion evidence.
10. If the repair clears a previously Blocked parent Work Package, resume and finish that original package in the same assigned plan point unless the repair package explicitly states otherwise.
11. Stop after the assigned plan point is complete and **do not begin another plan point**.

A production chat must not end merely because a subphase, commit, scaffold, first build attempt, failure repair, or intermediate milestone is complete. If the assigned plan point expands into many technical phases, all necessary phases remain part of the same production chat.

The only permitted incomplete ending is a **proven external blocker** outside the repository and outside the tools available to the chat. Source errors, compiler errors, test failures, build failures, and integration bugs are work to repair in the same production chat, not blockers.

## Work Package states

Google Drive packages are organized as:

- `Active` — package currently ready for a production chat, including failure-derived repair packages.
- `Completed` — plan points finished with verification evidence.
- `Blocked` — externally blocked parent packages with documented evidence.
- `Templates` — standard package structure for future assignments.
- `Failures / Active` — one failure currently being diagnosed.
- `Failures / Resolved` — completed diagnoses that have already been routed to production.
- `Failures / Templates` — standard failure-investigation package structure.

## Failure-to-production state machine

`Production failure -> Failure/Active -> diagnose only -> create/update Production/Active repair package -> verify routing -> Failure/Resolved -> new Production chat -> repair + resume same plan point -> Production/Completed`

The Failure chat must never perform both the diagnosis and the production fix. The handoff between those two chats is the Active repair Work Package plus the one-line launch sentence.

## Current production gate

The active project gate remains Locked Master Plan point **87**, CI build pipeline, until its full Definition of Done is verified green. Failure-derived repair packages for WP-087 remain part of point 87 and do not authorize point 88 or any other plan point.

This workflow remains in force until the project owner explicitly changes it.
