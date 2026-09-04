# AiLikeGPT — Mandatory Chat Naming Convention

Every project chat must have a title that makes its project, role, and assigned package immediately visible in the Project sidebar.

## Canonical title format

Use square-bracket tags in this exact order:

`[AiLikeGPT][ROLE][PACKAGE-ID] Short title`

The project tag and role tag are mandatory. The package ID is mandatory for Preparation, Production, Failure Investigation, and Production Repair chats.

## Exact role titles

- Persistent Discussion chat: `[AiLikeGPT][DISCUSSION] Project Control`
- Preparation chat: `[AiLikeGPT][PREPARATION][PREP-###] <short plan-point title>`
- Production chat: `[AiLikeGPT][PRODUCTION][WP-###] <short implementation title>`
- Failure Investigation chat: `[AiLikeGPT][FAILURE][FAIL-###] <short failure title>`
- Production Repair chat: `[AiLikeGPT][PRODUCTION][REPAIR][WP-###R#] <short repair title>`

Production Repair is still the Production role. `[REPAIR]` is an extra discoverability tag, not a fifth role.

## Naming invariants

1. Use the exact project name `AiLikeGPT` as the first tag.
2. Use only the canonical uppercase role tags: `DISCUSSION`, `PREPARATION`, `PRODUCTION`, `FAILURE`; add `REPAIR` only for a failure-derived Production repair chat.
3. Preserve the exact Drive package ID in the title. Do not invent a different numbering scheme.
4. Keep the human-readable suffix short enough to scan in a sidebar.
5. Do not reuse one specialized chat for another package merely to avoid creating a new chat.
6. Do not rename a chat to a different role without an explicit owner instruction that changes the workflow/role model itself.
7. A task-level request to do another role's work never changes the title or role.
8. If the chat interface does not allow the assistant to set its own title, the chat must state the exact required title before substantive role work so the user can rename it manually.

## Launch-title contract

Every launch instruction produced by Discussion, Preparation, Failure Investigation, or Production handoff must include a first line in this form:

`REQUIRED CHAT TITLE: <exact canonical title>`

The target chat must verify that the title matches its role and package before changing project state. If it does not match, it must identify the expected title and proceed only within the role/package named by the authoritative assignment.

## Examples

- `[AiLikeGPT][DISCUSSION] Project Control`
- `[AiLikeGPT][PREPARATION][PREP-088] Model Validation Metadata`
- `[AiLikeGPT][PRODUCTION][WP-088] Model Validation Metadata`
- `[AiLikeGPT][FAILURE][FAIL-002] WP-088 Android Build Failure`
- `[AiLikeGPT][PRODUCTION][REPAIR][WP-088R1] Repair Android Build`

This naming convention is mandatory for all new AiLikeGPT project chats.