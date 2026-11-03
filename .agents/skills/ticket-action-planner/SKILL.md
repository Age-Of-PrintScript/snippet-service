---
description: Analyze a development ticket and produce a technical,
  educational, and exhaustive implementation plan before any code is
  changed. Use when the user asks to understand, analyze, break down, or
  plan a ticket, feature, bug, refactor, or development task. Inspect
  the actual repository, existing architecture, similar implementations,
  and tests. Explain what should change, why, design decisions,
  alternatives, files/classes/methods involved, concepts, tests, edge
  cases, risks, and assumptions. This skill is planning-only and must
  not modify, create, delete, rename, or execute implementation changes.
name: ticket-action-planner
---

# Ticket Action Planner

## Purpose

Turn a development ticket into a concrete, technically grounded,
educational implementation plan.

The plan must be based on the actual repository rather than assumptions.

This skill is **planning-only**.

Do not implement the ticket while this skill is active.

Do not create, modify, delete, rename, move, or overwrite project files.

Do not apply patches.

Do not run commands whose purpose is to implement or modify the
requested feature.

Read-only inspection is allowed and encouraged.

If the user later explicitly asks to implement the approved plan, stop
treating the task as a planning task and hand the implementation to the
normal coding workflow. Do not continue planning indefinitely.

## Instruction Priority

The user's explicit instructions take precedence over this skill.

Project instructions from applicable `AGENTS.md` files remain in effect.

If another instruction conflicts with this skill, follow the
higher-priority instruction and clearly distinguish the conflict when
relevant.

## Planning Workflow

Follow this sequence.

### 1. Understand the Ticket

Read the complete ticket and identify:

-   Goal
-   Current behavior
-   Desired behavior
-   Functional requirements
-   Non-functional requirements
-   Explicit constraints
-   Acceptance criteria
-   Implied requirements
-   Ambiguities
-   Missing information

First explain the ticket in simple language, as if explaining it to a
developer who understands programming but has not seen the project
before.

Do not jump directly into implementation details.

### 2. Inspect the Repository

Before proposing changes, inspect the relevant codebase.

Determine:

-   Project structure
-   Technology stack
-   Relevant modules/packages
-   Architecture
-   Data flow
-   Existing abstractions
-   Related models
-   Related APIs
-   Persistence layer
-   Existing validation
-   Existing error handling
-   Existing tests
-   Configuration relevant to the ticket

Search for functionality similar to the requested change.

Explicitly identify the existing implementation(s) that should serve as
references.

Do not invent architecture when the repository already establishes a
pattern.

### 3. Explain the Existing Architecture

Describe only the architecture relevant to the ticket.

For each relevant layer/component, explain:

-   Responsibility
-   Important classes/modules
-   How data flows through it
-   How it interacts with other layers
-   Why it matters for this ticket

When useful, present a simple flow such as:

`Controller -> Service -> Repository -> Database`

or the project's actual equivalent.

### 4. Identify What Must Change

Clearly distinguish:

-   Existing behavior
-   Desired behavior
-   Required changes
-   Reusable existing functionality
-   New functionality
-   Unchanged functionality

For every proposed change, explain why it is needed.

Prefer reusing existing abstractions over creating parallel
implementations.

### 5. Produce the Step-by-Step Action Plan

Create an ordered implementation plan.

For every step, include as much of the following as applies:

-   Step number
-   Objective
-   Files to inspect/change
-   Classes/components
-   Methods/functions
-   Data structures
-   Interfaces
-   Inputs and outputs
-   Dependencies
-   Logic to add or modify
-   Validation
-   Error handling
-   Interaction with existing code
-   Tests affected
-   Why the step is necessary

Be specific enough that another developer could implement the ticket
from the plan without having to rediscover the architecture.

Do not pretend to know exact code details that were not verified. Mark
uncertain details as assumptions.

### 6. Explain Relevant Technical Concepts

Explain concepts that are relevant to the ticket and that help the user
understand the proposed implementation.

Examples include:

-   DTOs
-   Dataclasses
-   Entities/models
-   Repositories
-   Services
-   Controllers
-   Interfaces
-   Dependency injection
-   Mappers
-   Validators
-   Serialization/deserialization
-   State management
-   Async/reactive programming
-   Transactions
-   Caching
-   Factories
-   Adapters
-   Middleware

For each relevant concept:

1.  Define it in simple language.
2.  Explain its role in this project.
3.  Explain why it is useful here.
4.  Relate it to the concrete classes/files involved.

Do not explain unrelated concepts just to make the plan longer.

### 7. Explain Design Decisions

For important design decisions, explain:

-   Proposed approach
-   Why it fits the existing architecture
-   Benefits
-   Costs/trade-offs
-   Alternatives
-   Why the alternatives were rejected

Always distinguish between:

**Consistency:** following an established project convention.

**Improvement:** introducing a genuinely better approach.

Existing conventions should normally win.

If a significantly better approach would require changing an established
convention, do not silently adopt it. Present it as an alternative and
ask the user whether they want to make that architectural change.

### 8. Detail Tests

Design a concrete test strategy.

Cover, where relevant:

#### Happy paths

Expected successful behavior.

#### Edge cases

Boundary values, empty data, missing optional values, unusual
combinations, duplicate data, limits, etc.

#### Error cases

Invalid input, unavailable resources, exceptions, validation failures,
authorization failures, network/database failures, etc.

#### Regression cases

Existing behavior that could be affected by the change.

For each important test, describe:

-   What is being tested
-   Setup/input
-   Expected result
-   Why the test matters
-   Appropriate test location/file

Follow the project's existing testing conventions.

Do not write tests during the planning phase.

### 9. Identify Risks and Side Effects

Identify realistic risks such as:

-   Breaking existing behavior
-   API compatibility
-   Database migrations
-   Data consistency
-   Backward compatibility
-   Performance
-   Concurrency
-   State management
-   Security
-   Validation gaps
-   Error propagation
-   Coupling
-   Duplicate implementations
-   Inconsistent conventions

For each meaningful risk, explain a mitigation.

Avoid hypothetical risk lists that are unrelated to the ticket.

### 10. State Assumptions and Open Questions

Separate:

-   Facts verified from the repository
-   Reasonable assumptions
-   Questions that require user/product clarification

Do not block the entire plan because of a minor ambiguity that can be
safely resolved by an explicit assumption.

If an ambiguity could materially change the implementation, flag it
clearly before implementation.

### 11. Final Implementation Checklist

Finish with a concise checklist of the work required.

Example:

-   [ ] Update model
-   [ ] Add validation
-   [ ] Reuse existing repository pattern
-   [ ] Update service
-   [ ] Add mapper
-   [ ] Update controller
-   [ ] Add unit tests
-   [ ] Add integration tests
-   [ ] Verify regression behavior

Then explicitly state:

**Planning complete. No implementation changes were made.**

## Required Output Structure

Use this structure unless the ticket clearly requires a different
format:

# Ticket in Simple Terms

# Current Context and Architecture

# Existing Similar Implementations

# Current Behavior vs. Desired Behavior

# What Needs to Change

# Detailed Step-by-Step Plan

## Step 1

### Files

### Classes / Methods

### Changes

### Why

### Interactions

### Tests

## Step 2

...

# Relevant Technical Concepts

# Design Decisions and Alternatives

# Testing Strategy

# Edge Cases and Error Cases

# Risks and Mitigations

# Assumptions and Open Questions

# Implementation Checklist

# Planning Status

Planning complete. No implementation changes were made.

## Code and File Specificity

Be concrete.

Prefer:

-   `src/users/UserService.java`
-   `UserService.createUser()`
-   `UserRepository.findByEmail()`
-   `UserMapper.toDto()`

over vague descriptions such as:

-   "update the user logic"
-   "change the backend"
-   "add some validation"

When exact names are unknown, inspect the repository first. If they
still cannot be established, describe the expected responsibility and
mark the name as an assumption.

Small illustrative snippets, method signatures, pseudocode, data shapes,
or before/after examples may be included in the plan when they clarify
the intended implementation.

Do not turn those examples into actual repository changes.

## Repository Convention Rule

Before proposing a new implementation, ask:

> Has this project already solved a similar problem?

If yes, use that implementation as the primary reference.

Compare:

-   Structure
-   Naming
-   Data flow
-   Error handling
-   Testing
-   Dependency management
-   State management
-   API patterns
-   Persistence patterns

If the proposed implementation differs, explain why.

Never introduce a new pattern solely because it is more fashionable,
theoretically cleaner, or personally preferred.

## Planning Completeness Rule

A plan is complete when the user can answer:

-   What is the ticket asking for?
-   How does the current system work?
-   Where does the change belong?
-   Which files/classes/methods are involved?
-   What exactly needs to change?
-   Why is that design appropriate?
-   What alternatives exist?
-   How will it be tested?
-   What edge cases matter?
-   What could go wrong?
-   What assumptions remain?

If one of these cannot be answered from the repository, explicitly say
what information is missing.

## Stop Condition

Once the plan is complete, stop.

Do not begin implementation.

Do not modify files as a way of "preparing" the implementation.

Do not create placeholder files.

Wait for the user to explicitly request implementation in a subsequent
instruction.
