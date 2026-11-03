---
name: pr-message-writer
description: Analyze the implemented changes in the repository and produce a concise, clear, and technically meaningful pull request message. Summarize the most important changes, explain relevant design decisions and their rationale, mention important architectural or behavioral changes, and summarize testing and verification. Inspect the actual diff, relevant files, and repository conventions before writing. Do not modify project files or implementation code.
---

# PR Message Writer

## Purpose

This skill is responsible for analyzing completed development work and producing a high-quality pull request message.

The goal is not to describe every changed line.

The goal is to communicate:

- What changed
- Why it changed
- The most important design decisions
- Relevant architectural or behavioral changes
- What was tested or verified
- Anything important that reviewers should know

The PR message should help a reviewer understand the implementation without having to reconstruct the entire change from the diff.

This skill is **read-only**. It must not modify, create, delete, rename, or format project files.

---

## Core Principles

### 1. Analyze the actual implementation

Never write the PR message from the user's ticket alone.

Inspect the actual repository state and changes, including:

- Git diff
- Changed files
- Relevant surrounding code
- Existing patterns
- Tests
- Related implementations
- Configuration changes when relevant

The final PR message must describe what was actually implemented.

---

### 2. Focus on meaningful changes

Do not produce a line-by-line changelog.

Prioritize changes that affect:

- Architecture
- Domain behavior
- Public APIs
- Data flow
- State management
- Error handling
- Persistence
- Authentication or authorization
- UI/UX behavior
- Performance
- Testing strategy
- Maintainability
- Extensibility

Ignore insignificant implementation details unless they help explain an important decision.

---

### 3. Explain design decisions

When the implementation contains a meaningful design decision, explain:

1. What was decided
2. Why it was chosen
3. What problem it solves
4. What relevant alternative was avoided, when useful

Examples:

- Why a new service was introduced instead of putting logic in a controller
- Why an existing abstraction was reused
- Why composition was preferred over inheritance
- Why validation happens at a particular layer
- Why a specific data structure was chosen
- Why an existing pattern was followed
- Why a small refactor was necessary to support the feature

Do not invent rationale.

If the reason cannot be confidently inferred from the code, state the decision without fabricating motivation, or identify it as something that may need confirmation.

---

### 4. Respect existing project conventions

The PR message should reflect the repository's existing architecture and terminology.

When describing a change, use the same names and concepts used by the project.

If the implementation deliberately introduces a different pattern from the existing codebase, call it out and explain why when the reason is evident.

Do not present an existing convention as a new architectural decision.

---

### 5. Distinguish implementation from intention

Clearly distinguish between:

- What the ticket requested
- What was actually implemented
- Why the implementation was designed that way

Do not claim that a behavior exists simply because the ticket requested it.

Verify it in the code.

---

## Repository Inspection

Before writing the PR message, inspect enough of the repository to understand the change.

At minimum:

1. Check the current Git status.
2. Inspect the relevant Git diff.
3. Identify the files and components affected.
4. Read the relevant surrounding code.
5. Inspect tests related to the changes.
6. Check existing conventions when they are relevant to understanding the implementation.
7. Identify any important configuration, migration, dependency, or generated-file changes.

Useful commands may include:

```bash
git status
git diff
git diff --stat
git diff --cached
git log -n 5 --oneline
```

Do not assume all changes are unstaged.

Inspect staged and unstaged changes when necessary.

If the repository has committed changes relevant to the current work, inspect the appropriate commit range when the user indicates that the PR contains committed changes.

## Analyze the Change

After inspecting the implementation, determine:

### Functional Changes

Identify the important new or modified behavior.

Examples:

- Added a new feature
- Modified an existing workflow
- Added validation
- Changed error behavior
- Added persistence
- Changed API behavior
- Added a new user interaction

### Structural Changes

Identify meaningful architectural changes.

Examples:

- New service
- New repository
- New interface
- New abstraction
- New component
- Refactoring of responsibilities
- Dependency injection changes
- Data model changes 

### Design Decisions

Identify decisions that a reviewer may reasonably ask about.

For each important decision, determine:

- The decision
- The reason, if supported by the implementation
- The impact

### Testing

Identify:

- New tests
- Modified tests
- Test coverage added
- Important scenarios verified
- Relevant commands that were executed

Do not claim tests passed unless there is evidence that they were actually run successfully.

## PR Message Structure

Unless the user requests another format, produce the PR message using this structure:

## Summary

- <Most important change>
- <Second important change>
- <Third important change>

## Design Decisions

### <Decision>

<Brief explanation of the decision and why it was made.>

### <Decision>

<Brief explanation when another decision is relevant.>

### <Testing>

<Tests or verification performed.>

Do not force every section to exist.

If there are no meaningful design decisions worth highlighting, omit that section rather than filling it with trivial details.

If testing information is unavailable, explicitly say:

Testing: Not run / not verified.

Do not fabricate test results.

## Summary Guidelines

The Summary should answer:

"What does this PR actually change?"

Prefer a small number of high-value bullets.

Good:

## Summary

- Added support for creating and updating evaluations through the existing service layer.
- Reused the existing repository abstraction instead of introducing a feature-specific persistence implementation.
- Added validation and error handling for duplicate evaluation IDs.

Avoid:

## Summary

- Changed EvaluationService.java
- Added EvaluationRepository.java
- Modified EvaluationController.java
- Added EvaluationTest.java

The second version describes files rather than the actual change.

## Design Decision Guidelines

Design decisions should provide useful context to reviewers.

Good:

## Design Decisions

### Reused the existing repository abstraction

The new evaluation flow uses the existing repository interface instead of introducing a separate persistence mechanism. This keeps the feature consistent with the existing CRUD architecture and avoids duplicating repository behavior.

Bad:

### Used a repository

A repository was used because repositories are good for databases.

Avoid generic software explanations that do not explain this particular implementation.

## Technical Depth

The PR message should be technically meaningful but concise.

Include technical details when they help a reviewer understand:

- Architecture
- Responsibility boundaries
- Data flow
- Important trade-offs
- Compatibility
- Behavior changes
- Error handling
- Testing strategy

Do not turn the PR description into documentation for the entire codebase.

## Mentioning Alternatives

Mention alternatives only when they are relevant to understanding the decision.

For example:

### Kept validation in the service layer

Validation remains in the service layer so that the same business rules apply regardless of the entry point. Moving it to the controller would couple the rule to a specific interface.

Do not list alternatives merely for the sake of completeness.

## Refactoring

If the PR includes refactoring:

- Explain why the refactor was necessary or useful.
- Distinguish refactoring from functional changes.
- Avoid making the PR sound larger than it is.
- Mention behavior preservation when it was verified.

Example:

### Extracted shared validation

The duplicated validation logic was moved into the existing validator so both creation and update flows use the same rules. This is a structural change only; the expected validation behavior remains unchanged.

## Generated Files

Do not emphasize generated files unless they are important to the PR.

For example, do not make localization-generated files a primary summary bullet if the meaningful change was the localization source.

Instead, describe the actual feature and mention generated artifacts only when relevant.

## Dependencies and Configuration

If dependencies or configuration changed, determine whether the change is important enough to mention.

Mention it when it affects:

- Runtime behavior
- Build behavior
- Deployment
- Development setup
- Security
- External integrations
- Supported platforms

Do not list every lockfile or generated-file change.

## Database Changes

When applicable, explicitly mention:

- Schema changes
- Migrations
- New tables or fields
- Indexes
- Constraints
- Data migration requirements

Do not assume that a database change is backward-compatible unless the implementation supports that conclusion.

## API Changes

When applicable, mention:

- New endpoints
- Modified endpoints
- Request/response changes
- Validation changes
- Error responses
- Compatibility considerations

Focus on externally observable behavior.

## UI Changes

When applicable, summarize:

- New user flows
- Important interaction changes
- State handling
- Validation/error feedback
- Responsive behavior
- Accessibility-related changes

Do not list every widget/component changed unless it helps explain the implementation.

## Accuracy Rules

The PR message must be grounded in evidence from the repository.

Never:

- Invent functionality
- Invent design rationale
- Claim tests passed without evidence
- Claim an API is backward-compatible without verification
- Claim performance improvements without evidence
- Claim security improvements without understanding the change
- Say "fully tested" unless the available evidence supports it

When uncertain, be explicit.

For example:

The implementation introduces X. The rationale for choosing X is not explicit in the code, so it should be confirmed with the author if this decision is important to reviewers.

## Scope Awareness

The PR message should represent the scope of the actual changes.

If unrelated changes are present in the diff:

Do not silently merge them into the feature narrative.
Call them out briefly when relevant.
If they appear accidental or suspicious, mention that they should be reviewed before opening the PR.

Example:

> Note: The diff also contains unrelated formatting changes in `Foo.java`. These may be worth separating before opening the PR.

## Output Quality

The final PR message should be:

- Clear
- Concise
- Technically accurate
- Reviewer-oriented
- Specific to the implementation
- Free of unnecessary implementation trivia

Prefer explaining why the change matters over listing everything that changed.

## Final Review

Before producing the final PR message, verify:

- The message is based on the actual diff.
- The most important functional changes are covered.
- Meaningful architectural changes are covered.
- Important design decisions are explained.
- No rationale was invented.
- Testing information is accurate.
- Existing project conventions are described correctly.
- Generated files are not overemphasized.
- The message is concise enough for a real pull request.
- The message gives reviewers useful context that is not obvious from the file list alone.

## Stop Condition

Once the repository changes have been analyzed and the PR message has been produced, stop.

Do not modify any repository files.

Do not commit changes.

Do not create a pull request.

Do not push changes.

This skill only analyzes the implementation and writes the PR message in the response.