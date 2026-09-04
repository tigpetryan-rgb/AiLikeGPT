# AiLikeGPT — Chat Workflow Index

AiLikeGPT uses four strict chat roles. A new chat must identify its role and canonical chat title before changing project state.

## Canonical flow

`Discussion -> Preparation -> Staging -> Production -> Completed`

Failure path:

`Production failure -> Failure Investigation -> Production Repair -> Completed`

Then return to Discussion for the next eligible Locked Master Plan point.

## Shared rules

1. Read `AGENTS.md`, `PROJECT_PLAN_LOCKED.md`, `PROJECT_CONTEXT.md`, and `CHAT_NAMING.md` before role work.
2. Never silently change the Locked Master Plan.
3. Keep one Locked Plan point per Preparation/Production assignment.
4. Do not claim build/test/runtime success without executed evidence.
5. Keep Google Drive Work Package lifecycle state synchronized with repository context.
6. Bootstrap is idempotent: verify existing Drive/GitHub structure first; create only missing role artifacts; never create duplicates or overwrite authoritative protocols silently.

## Mandatory chat titles

Every project chat must be immediately discoverable in the Project sidebar by project, role, and package ID.

Canonical titles:

- Discussion: `[AiLikeGPT][DISCUSSION] Project Control`
- Preparation: `[AiLikeGPT][PREPARATION][PREP-###] <short title>`
- Production: `[AiLikeGPT][PRODUCTION][WP-###] <short title>`
- Failure Investigation: `[AiLikeGPT][FAILURE][FAIL-###] <short title>`
- Production Repair: `[AiLikeGPT][PRODUCTION][REPAIR][WP-###R#] <short title>`

Production Repair remains the Production role; `[REPAIR]` is only an extra discoverability tag.

Every handoff/launch instruction must begin with:

`REQUIRED CHAT TITLE: <exact canonical title>`

If the assistant cannot directly set the UI chat title, it must state the exact required title before substantive role work so the user can rename it manually. The exact package ID in Drive must be preserved in the title. Full naming rules are in `CHAT_NAMING.md`.

## Hard Role Boundary — mandatory

Chat roles are hard execution boundaries, not suggestions.

A chat must never perform work owned by another role merely because the user asks for that work inside the wrong chat. A direct request such as “implement it here”, “fix it now”, “do the analysis here”, “prepare the next point here”, or equivalent wording does **not** authorize a role switch.

When a requested action is outside the current chat's role, the chat must:

1. refuse only the out-of-role execution, without refusing the project goal;
2. state its current role and the specific boundary that prevents the requested action;
3. preserve/update any handoff artifact that its own role is responsible for;
4. identify the correct target role;
5. return a short ready-to-copy launch instruction for that target chat, including the required canonical title;
6. remain inside its current role and not partially perform the target role's work.

Examples:

- Discussion asked to implement code -> do not implement; route to Production after the required Preparation/Staging/Active lifecycle.
- Preparation asked to implement/fix code -> do not implement; complete/stage the production package and route to Production.
- Failure Investigation asked to fix the diagnosed failure -> do not fix; complete diagnosis, create/update the repair package, and route to Production Repair.
- Production asked to redesign unrelated future architecture or start another plan point -> do not do it; finish/stop at the assigned point and route future design to Discussion/Preparation.

Only an **explicit owner instruction to change the workflow/role model itself** may alter these boundaries. A task-level request to do another role's work is not such authorization.

## Discussion Chat

Discussion owns sequencing and decisions. Its persistent title is `[AiLikeGPT][DISCUSSION] Project Control`.

It chooses the next eligible plan point, creates one PREP assignment under `Production Work Packages / Preparation / Active`, and gives the user the Preparation launch sentence with the exact `[AiLikeGPT][PREPARATION][PREP-###] ...` title. Discussion does not implement production code.

Discussion is the only role that promotes a normal staged WP from `Staging` to `Active`, and by default there is only one Active production package at a time.

## Preparation Chat

Preparation owns exactly one PREP assignment and follows `PREPARATION_WORKFLOW.md` plus the Drive Preparation protocol. Its title must include the exact PREP ID.

It must design that plan point to production-ready detail: architecture, file-by-file plan, models/state, APIs/contracts, UI flow, migrations/compatibility, tests, risks, edge cases, boundaries, and objective Definition of Done.

It writes no production implementation code and makes no implementation commit. It creates the resulting production package under `Staging`, verifies it, and moves PREP to `Preparation/Completed`. Its Production handoff must name the exact future `[AiLikeGPT][PRODUCTION][WP-###] ...` chat title.

## Definition of Ready

A staged package may move to Active only when it identifies one Locked Plan point, scope boundaries, exact implementation objectives, affected files/components/contracts, dependencies, verification evidence, risks/edge cases, migration/rollback needs when relevant, and objective DoD. Production must not need to repeat major design work.

## Production Chat

Production owns exactly one Active WP and follows `PRODUCTION_WORKFLOW.md` and the Drive Production protocol. Its title must include the exact WP ID. A failure-derived repair chat uses the same Production role with the additional `[REPAIR]` tag and exact repair package ID.

It implements the assigned point, runs tests/builds/verifications, repairs source/compiler/test/build/integration failures, records evidence, and continues until DoD is complete. It stops before the next plan point.

Only a proven external blocker outside the repository and connected tools permits incomplete exit; then the package moves to Blocked with evidence and the narrowest possible unblock action.

## Failure Investigation Chat

Failure Investigation owns exactly one FAIL package and follows the Drive Failure Investigation protocol. Its title must include the exact FAIL ID.

It diagnoses only and never implements the production fix or makes a fix commit. It identifies root cause with evidence, automatically creates or updates an Active repair package tied to the same Locked Plan point, verifies that package, moves FAIL to Resolved, and returns the Production Repair launch sentence with the exact `[AiLikeGPT][PRODUCTION][REPAIR][WP-###R#] ...` title.

## Production Repair Chat

Production Repair is still the Production role, not a fifth role. It executes the Active repair package tied to the same Locked Plan point, fixes the diagnosed cause, resumes the parent point when required, and completes the original DoD.

## Current project gate

Locked Master Plan point 87 remains the current production gate until verified green. Preparation work for later plan points may continue and stage future packages, but unrelated production implementation must not bypass the gate unless the project owner explicitly changes sequencing.

## Reusable template

For other projects, copy and adapt `MULTI_CHAT_PROJECT_WORKFLOW_TEMPLATE.md` and carry over the same chat-title convention with the target project's name.