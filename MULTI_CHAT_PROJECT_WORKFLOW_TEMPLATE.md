# Reusable Multi-Chat Project Workflow Template

Use this file as a bootstrap template for any project that needs strict separation between discussion, preparation, production, and failure diagnosis.

## Placeholders

- `{{PROJECT_NAME}}`
- `{{GITHUB_REPO}}`
- `{{DRIVE_PROJECT_ROOT}}`
- `{{LOCKED_PLAN_FILE}}`
- `{{PROJECT_CONTEXT_FILE}}`

## State machine

`Discussion -> Preparation -> Staging -> Production -> Completed`

Failure path:

`Production failure -> Failure Investigation -> Production Repair -> Completed`

Then return to Discussion for the next plan point.

## Required Drive structure

```text
{{PROJECT_NAME}}/
  Project Information/
    Production Work Packages/
      Preparation/
        Active/
        Completed/
        Blocked/
        Templates/
      Staging/
      Active/
      Completed/
      Blocked/
      Templates/
      Failures/
        Active/
        Resolved/
        Templates/
```

## Required GitHub read-first files

- `AGENTS.md`
- `{{LOCKED_PLAN_FILE}}`
- `{{PROJECT_CONTEXT_FILE}}`
- `CHAT_WORKFLOW.md`
- `PREPARATION_WORKFLOW.md`
- `PRODUCTION_WORKFLOW.md`
- `FAILURE_WORKFLOW.md` or an equivalent failure section in `PRODUCTION_WORKFLOW.md`

## Bootstrap invariant

Every role chat first inspects GitHub and Drive. If its required role folders/files are missing, it creates only the missing pieces before doing role work.

Bootstrap must be idempotent:

- never create duplicate folders or documents;
- never silently overwrite an existing authoritative protocol;
- never change the locked plan unless the owner explicitly authorizes it;
- preserve existing project-specific rules when adapting this generic template.

## Naming

- Preparation: `PREP-### — <plan point>`
- Production: `WP-### — <plan point>`
- Failure: `FAIL-### — <concrete failure>`
- Repair: `WP-###R# — <repair title>`

## Discussion Chat

Discussion owns sequencing and project decisions. It selects one next eligible Locked Plan point, checks dependencies and gates, creates exactly one Preparation assignment under `Preparation/Active`, and gives the user the short Preparation launch instruction. It does not implement production code.

## Preparation Chat

Preparation owns exactly one PREP assignment. It resolves architecture, file-by-file implementation plan, data models, APIs/contracts, UI flows, migrations/compatibility, tests, risks, edge cases, scope boundaries, and objective Definition of Done. It writes no production implementation code and makes no implementation commit.

When complete it creates the production-ready WP under `Staging`, verifies it by readback, records the handoff, and moves the PREP package to `Preparation/Completed`.

If exactly one external decision or datum blocks design, move PREP to `Preparation/Blocked` and name the single unblock requirement.

## Definition of Ready: Staging -> Active

A staged WP may be promoted only when it includes:

- exactly one Locked Plan point;
- in-scope and out-of-scope boundaries;
- exact implementation objectives;
- affected files/components and contracts;
- dependencies and migration/rollback needs where relevant;
- tests, builds, or other verification evidence required;
- risks and edge cases;
- objective Definition of Done;
- enough detail that Production does not need to repeat major architecture work.

## Production Chat

Production owns exactly one Active WP, including a repair package tied to that same plan point. It reads the package and repository state, implements all required work, runs tests/builds/verifications, repairs source/compiler/test/build/integration failures, records objective evidence, and continues until DoD is complete. It stops before the next plan point.

Only a proven external blocker outside the repository and connected tools permits incomplete exit. In that case move the package to Blocked with evidence and, where possible, exactly one unblock action.

## Failure Investigation Chat

Failure Investigation owns exactly one FAIL package. It diagnoses and gathers evidence only. It never implements the production fix or makes a fix commit.

It must identify the earliest defensible failure boundary, root cause, confidence, evidence, and rejected hypotheses; update the FAIL package; automatically create or update an Active production repair package tied to the same plan point; verify that repair package; move FAIL to Resolved; and return one short Production Repair launch sentence.

## Single-active rule

By default there is at most one Active production WP. Discussion is the only role that promotes a normal staged package from `Staging` to `Active`. Failure Investigation may create or update an Active repair package only for the currently failing plan point.

Do not start an unrelated plan point while a gate or repair for the current point is active unless the owner explicitly changes sequencing.

## Evidence rule

Never claim build, test, deploy, runtime, migration, or integration success without executed evidence. Record run IDs, commit SHAs, commands, artifacts, logs, screenshots, or equivalent objective proof when applicable.

## Context-sync rule

After meaningful lifecycle changes, update `{{PROJECT_CONTEXT_FILE}}` and the online Work Package state so a brand-new chat can reconstruct current truth without conversation memory.

## Four launch prompts

### Discussion

`@Google Drive @GitHub Open {{PROJECT_NAME}} and act as the persistent Discussion Chat. Read AGENTS.md, {{LOCKED_PLAN_FILE}}, {{PROJECT_CONTEXT_FILE}}, and CHAT_WORKFLOW.md. Verify/create only missing Discussion/workflow online structure without duplicating existing artifacts. Choose exactly one next eligible Locked Plan point, create its PREP assignment under Production Work Packages -> Preparation -> Active, and return only the short launch instruction for the Preparation Chat. Do not implement production code.`

### Preparation

`@Google Drive @GitHub Open {{PROJECT_NAME}}'s assigned item under Production Work Packages -> Preparation -> Active and perform it completely according to PREPARATION_WORKFLOW.md. First verify/create only missing Preparation role folders/protocol/template online without duplicating existing artifacts. Design exactly that one Locked Plan point to production-ready detail, create its Production Work Package under Staging, verify the package by readback, move the PREP assignment to Preparation/Completed, and return the short Production handoff. Do not write production code or make implementation commits.`

### Production

`@Google Drive @GitHub Open {{PROJECT_NAME}}'s assigned Production Work Package under Production Work Packages -> Active and execute it completely according to PRODUCTION_WORKFLOW.md. First verify/create only missing Production role online protocol/template structure without duplicating existing artifacts. Implement exactly that one Locked Plan point, continue through build/test/debug/repair loops until every Definition of Done item has objective evidence, update project context and package lifecycle, then stop before the next plan point. Do not treat source/compiler/test/build/integration failures as blockers.`

### Failure Investigation

`@Google Drive @GitHub Open {{PROJECT_NAME}}'s assigned FAIL item under Production Work Packages -> Failures -> Active and perform it completely according to the Failure Investigation protocol. First verify/create only missing Failure role online folders/protocol/template without duplicating existing artifacts. Diagnose only: do not implement the fix and do not make a fix commit. Establish root cause with evidence, automatically create/update the Active production repair package for the same Locked Plan point, verify the repair package, move the FAIL package to Resolved, and return only the short launch instruction for the Production Repair Chat.`

## Owner-change rule

Only explicit owner instruction can alter the locked master plan, role boundaries, or sequencing rules. Technical details may evolve inside an assigned plan point when evidence requires it, but product goals and locked requirements remain authoritative.
