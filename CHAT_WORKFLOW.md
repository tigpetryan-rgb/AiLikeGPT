# AiLikeGPT — Chat Workflow Index

AiLikeGPT uses four strict chat roles. A new chat must identify its role before changing project state.

## Canonical flow

`Discussion -> Preparation -> Staging -> Production -> Completed`

Failure path:

`Production failure -> Failure Investigation -> Production Repair -> Completed`

Then return to Discussion for the next eligible Locked Master Plan point.

## Shared rules

1. Read `AGENTS.md`, `PROJECT_PLAN_LOCKED.md`, and `PROJECT_CONTEXT.md` before role work.
2. Never silently change the Locked Master Plan.
3. Keep one Locked Plan point per Preparation/Production assignment.
4. Do not claim build/test/runtime success without executed evidence.
5. Keep Google Drive Work Package lifecycle state synchronized with repository context.
6. Bootstrap is idempotent: verify existing Drive/GitHub structure first; create only missing role artifacts; never create duplicates or overwrite authoritative protocols silently.

## Discussion Chat

Discussion owns sequencing and decisions. It chooses the next eligible plan point, creates one PREP assignment under `Production Work Packages / Preparation / Active`, and gives the user the Preparation launch sentence. Discussion does not implement production code.

Discussion is the only role that promotes a normal staged WP from `Staging` to `Active`, and by default there is only one Active production package at a time.

## Preparation Chat

Preparation owns exactly one PREP assignment and follows `PREPARATION_WORKFLOW.md` plus the Drive Preparation protocol.

It must design that plan point to production-ready detail: architecture, file-by-file plan, models/state, APIs/contracts, UI flow, migrations/compatibility, tests, risks, edge cases, boundaries, and objective Definition of Done.

It writes no production implementation code and makes no implementation commit. It creates the resulting production package under `Staging`, verifies it, and moves PREP to `Preparation/Completed`.

## Definition of Ready

A staged package may move to Active only when it identifies one Locked Plan point, scope boundaries, exact implementation objectives, affected files/components/contracts, dependencies, verification evidence, risks/edge cases, migration/rollback needs when relevant, and objective DoD. Production must not need to repeat major design work.

## Production Chat

Production owns exactly one Active WP and follows `PRODUCTION_WORKFLOW.md` and the Drive Production protocol.

It implements the assigned point, runs tests/builds/verifications, repairs source/compiler/test/build/integration failures, records evidence, and continues until DoD is complete. It stops before the next plan point.

Only a proven external blocker outside the repository and connected tools permits incomplete exit; then the package moves to Blocked with evidence and the narrowest possible unblock action.

## Failure Investigation Chat

Failure Investigation owns exactly one FAIL package and follows the Drive Failure Investigation protocol.

It diagnoses only and never implements the production fix or makes a fix commit. It identifies root cause with evidence, automatically creates or updates an Active repair package tied to the same Locked Plan point, verifies that package, moves FAIL to Resolved, and returns the Production Repair launch sentence.

## Production Repair Chat

Production Repair is still the Production role, not a fifth role. It executes the Active repair package tied to the same Locked Plan point, fixes the diagnosed cause, resumes the parent point when required, and completes the original DoD.

## Current project gate

Locked Master Plan point 87 remains the current production gate until verified green. Preparation work for later plan points may continue and stage future packages, but unrelated production implementation must not bypass the gate unless the project owner explicitly changes sequencing.

## Reusable template

For other projects, copy and adapt `MULTI_CHAT_PROJECT_WORKFLOW_TEMPLATE.md`.
