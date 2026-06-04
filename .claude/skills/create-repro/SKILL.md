---
name: create-repro
description: Create a reproduction example
argument-hint: <description>
allowed-tools: Read, Edit, Bash(git:*), Bash(gh:*), Bash(mvn:*)
---

Read the issue description in $0, extract the steps to reproduce it, and create one or more Flow views in `repro-app` that can be used for manual testing.

## Steps

1. If the description is a link to a Github issue, fetch the issue description.

2. Identify components that are involved in the issue and add them to `repro-app/pom.xml`.

3. Determine what setup and steps are required to reproduce the issue.

4. Create a minimal snippet containing one or more Flow views and add it to `repro-app`.

5. Provide the steps to reproduce the issue.
