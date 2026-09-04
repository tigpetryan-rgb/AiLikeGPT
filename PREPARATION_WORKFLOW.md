# AiLikeGPT — Preparation Workflow

A **Preparation chat** designs exactly one future Locked Master Plan point for later production execution. It does **not** implement production code.

Authoritative Drive location:

`AiLikeGPT / Project Information / Production Work Packages / Preparation`

## Mandatory read order

1. `AGENTS.md`
2. `PROJECT_PLAN_LOCKED.md` in full
3. `PROJECT_CONTEXT.md`
4. `PRODUCTION_WORKFLOW.md`
5. this file
6. the assigned Drive Preparation package under `Preparation / Active`
7. relevant current repository files

## One-point rule

One Preparation chat owns exactly one Locked Master Plan point. It may inspect any repository files needed to design that point, but it must not begin another plan point.

## Required preparation output

Resolve, where applicable:

- architecture and component boundaries;
- file-by-file implementation plan;
- data models and state ownership;
- APIs/interfaces/contracts and error semantics;
- UI/user flows;
- migration/compatibility sequence;
- tests and objective verification evidence;
- risks, edge cases and explicit in/out-of-scope boundaries;
- a production-ready Work Package for the same Locked Plan point.

## No-production-code rule

A Preparation chat must not implement application/native/workflow production changes, make implementation commits, fix build/source failures, or claim unexecuted tests/builds passed. Repository state is read-only by default. Drive planning/documentation writes required by the preparation assignment are allowed.

## Handoff

When preparation is complete, create the detailed future Production Work Package under:

`Production Work Packages / Staging`

Do **not** put future queued work directly in `Production Work Packages / Active`. The Discussion chat promotes exactly one staged package to Active when it is eligible for execution.

Then update the Preparation package with the staged package name/location and move it from `Preparation / Active` to `Preparation / Completed`.

## Completion rule

Do not stop at an outline. Continue until the staged production package is concrete enough for a Production chat to execute directly without repeating architecture/design work.

If one external datum or owner decision is genuinely required, narrow the blocker to that one item and move the Preparation package to `Preparation / Blocked`.

## Role separation

- **Discussion:** chooses sequencing and creates preparation assignments.
- **Preparation:** designs one point and stages its production package; no production code.
- **Production:** implements one point through verified Definition of Done.
- **Failure Investigation:** diagnoses one failure only and routes repair back to Production.

This workflow remains in force until the project owner explicitly changes it.
