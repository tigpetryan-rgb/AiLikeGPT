# AiLikeGPT — Project Execution Workflow

This repository uses a strict separation between **technical discussion**, **preparation/design**, **failure investigation**, and **production execution**.

Authoritative operational packages are stored in Google Drive under:

`AiLikeGPT / Project Information / Production Work Packages`

Read `CHAT_WORKFLOW.md` first for the compact four-role state machine.

## Shared online-bootstrap invariant

Every specialized chat must inspect the required GitHub/Drive workflow structure before role work. If role-specific folders/protocols/templates are missing, create only the missing pieces.

Bootstrap is idempotent: never create duplicate artifacts, never silently overwrite an existing authoritative protocol, and never modify the Locked Master Plan without explicit owner authorization.

## Four chat types

### 1. Discussion chat

The persistent Discussion chat owns sequencing and project-level technical decisions.

It must:

- choose the next Locked Master Plan point to prepare or execute;
- decide when a staged Production Work Package is eligible to become Active;
- create/activate one Preparation assignment when design work is needed;
- give the user one short launch sentence for the next specialized chat;
- avoid doing the specialized chat's work itself unless the owner explicitly changes the workflow.

Discussion is the only role that promotes a normal staged package from `Staging` to `Active`.

### 2. Preparation chat

A Preparation chat designs **exactly one future Locked Master Plan point** from its assigned Drive package under `Preparation / Active`.

It must:

1. Read `AGENTS.md`, `PROJECT_PLAN_LOCKED.md`, `PROJECT_CONTEXT.md`, `CHAT_WORKFLOW.md`, this file, `PREPARATION_WORKFLOW.md`, and the assigned Preparation package.
2. Inspect the relevant current repository files.
3. Resolve architecture, component boundaries, file-by-file implementation changes, data/state models, APIs/contracts, UI flows, migration/compatibility, tests/verification, risks and explicit boundaries as applicable.
4. Keep repository production code read-only by default.
5. Create a production-ready Work Package for the same Locked Plan point under `Production Work Packages / Staging`.
6. Verify the staged package by Drive readback.
7. Record the staged package in the Preparation package and move the Preparation package from `Preparation / Active` to `Preparation / Completed`.
8. Stop without implementing production code or starting another plan point.

A Preparation chat is incomplete if it ends with only an outline or recommendations and no executable staged Production Work Package.

Detailed rule: `PREPARATION_WORKFLOW.md` and the Google Drive `Preparation Chat Protocol — One Plan Point, No Production Code`.

### 3. Failure Investigation chat

A Failure Investigation chat diagnoses **one concrete failed CI/build/production result** from its assigned Google Drive Failure package.

It must:

1. Read the assigned Failure package and Failure Investigation Protocol in Google Drive.
2. Read `AGENTS.md`, `PROJECT_PLAN_LOCKED.md`, `PROJECT_CONTEXT.md`, `CHAT_WORKFLOW.md`, this file, and the related production Work Package.
3. Inspect the exact failed run/job/commit/workflow and gather the strongest available evidence.
4. Identify the earliest defensible failure boundary, root cause, confidence, evidence, and rejected hypotheses.
5. Remain **read-only with respect to the production fix**: do not implement or commit the fix.
6. Write the diagnosis back into the Failure package.
7. Automatically create or update the corresponding production repair Work Package tied to the same Locked Plan point.
8. Verify production routing in Drive, then move the Failure package from `Failures / Active` to `Failures / Resolved`.
9. End with one short launch sentence naming the exact production repair package.

A Failure Investigation chat is incomplete if it produces only analysis/reporting without production routing.

### 4. Production chat

A Production chat executes **exactly one** assigned Locked Master Plan point from its Google Drive Work Package, including a repair package tied to that same point.

It must:

1. Read `AGENTS.md`.
2. Read `PROJECT_PLAN_LOCKED.md` in full.
3. Read `PROJECT_CONTEXT.md` and `CHAT_WORKFLOW.md`.
4. Read this file and the assigned Google Drive Work Package in full.
5. Read any referenced resolved Failure package before repair work.
6. Inspect current repository state before changing code.
7. Execute all implementation/repair phases required by the package.
8. Continue through debugging, tests, repairs, reruns, and verification until every Definition of Done item is satisfied.
9. Update repository/project context and the Work Package with completion evidence.
10. Stop after the assigned plan point is complete and do **not** begin another plan point.

A Production chat must not end merely because a subphase, commit, scaffold, first build attempt, failure repair, or intermediate milestone is complete.

The only permitted incomplete ending is a **proven external blocker** outside the repository and outside the tools available to the chat. Source errors, compiler errors, test failures, build failures, and integration bugs are work to repair in the same Production chat.

## Definition of Ready — Staging to Active

A staged Work Package may be promoted to Active only when it contains:

- exactly one Locked Master Plan point;
- explicit in-scope and out-of-scope boundaries;
- exact implementation objectives;
- affected files/components/contracts;
- required dependencies and migration/rollback needs where relevant;
- test/build/runtime/integration verification requirements;
- risks and edge cases;
- an objective Definition of Done;
- enough detail that Production does not need to repeat major architecture/design work.

If these conditions are not satisfied, Discussion must return the package to Preparation rather than starting Production.

## Single-active rule

By default there is at most **one Active production Work Package**. Do not start an unrelated plan point while the current gate or same-point repair is Active/Blocked unless the project owner explicitly changes sequencing.

Failure Investigation may create or update an Active repair package only for the currently failing Locked Plan point; this does not authorize unrelated production work.

## Evidence and context-sync rules

Never claim build, test, deploy, runtime, migration, or integration success without executed evidence. Record relevant run IDs, commit SHAs, commands, artifacts, logs, screenshots, or equivalent objective proof.

After a meaningful lifecycle transition, synchronize the Google Drive package state and `PROJECT_CONTEXT.md` so a brand-new chat can reconstruct current truth without conversation memory.

## Drive states

Main production packages:

- `Staging` — production-ready packages prepared in advance but not yet authorized to execute.
- `Active` — exactly the package currently eligible for Production execution.
- `Completed` — plan points finished with verification evidence.
- `Blocked` — externally blocked production packages with documented evidence.
- `Templates` — reusable production package structure.

Preparation packages:

- `Preparation / Active` — one point currently being designed.
- `Preparation / Completed` — design complete; production package staged.
- `Preparation / Blocked` — preparation blocked on one proven external datum/decision.
- `Preparation / Templates` — reusable preparation structure.

Failure packages:

- `Failures / Active` — one failure currently being diagnosed.
- `Failures / Resolved` — completed diagnoses already routed to production.
- `Failures / Templates` — reusable failure-investigation structure.

## Normal state machine

`Discussion -> Preparation/Active -> design only -> Production/Staging -> Discussion promotes one package -> Production/Active -> implement + verify -> Production/Completed`

## Failure state machine

`Production failure -> Failures/Active -> diagnose only -> create/update production repair package -> Failures/Resolved -> new Production chat -> repair + resume same plan point -> Production/Completed`

## Current production gate

Locked Master Plan point **87**, CI build pipeline, remains the current implementation gate until its full Definition of Done is verified. While it is externally blocked, future plan points may be **prepared and staged**, but they must not be implemented in Production until the Discussion chat explicitly promotes them after the gate policy allows it.

## Reusable system template

For applying this mechanism to another project, start from `MULTI_CHAT_PROJECT_WORKFLOW_TEMPLATE.md` and replace its project placeholders while preserving any stronger project-specific rules.

This workflow remains in force until the project owner explicitly changes it.
