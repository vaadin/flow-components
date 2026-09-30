# GitHub Workflows

## Pull request snapshots

`pr-snapshot.yml` builds and publishes a Flow Components snapshot for a single
pull request, so a change can be tried out from a Maven repository before it is
merged. It is adopted from the workflow of the same name in
[vaadin/flow](https://github.com/vaadin/flow/blob/main/.github/workflows/pr-snapshot.yml).

The build is opt-in per pull request: add the `snapshot build` label and it
starts, and every commit pushed while the label is there republishes the
snapshot. Removing the label stops that.

The version comes from the branch name — everything up to the last slash is
dropped, so `fix/my-thing` on a `25.4-SNAPSHOT` branch publishes
`25.4.my-thing-SNAPSHOT`. That is the scheme the TeamCity feature branch
snapshot builds of the other Vaadin repositories use, so snapshots built from
equally named branches of two repositories carry the same qualifier and can be
imported as a pair. The workflow comments the version and a copy-pasteable
`flow-components-bom` import on the pull request, editing the same comment on
every rebuild.

The build is the shape of a release rather than of a validation run: `-Drelease`
leaves the integration test modules out of the reactor, `-Dwith-docs` attaches
the sources and javadoc jars, and the `flow.version` of the branch is left
alone, so the snapshot resolves the same Flow version the branch normally builds
against.

Dropping the prefix means the version is only as unique as the part of the
branch name after the last slash: `fix/npe` and `issues/npe` both publish
`25.4.npe-SNAPSHOT`, and the later build replaces the earlier one without
warning. Two snapshot builds running at once want two names that differ by more
than their prefix.

Publishing needs the credentials, and GitHub only hands secrets to pull requests
from a branch of this repository. That is also what limits who can trigger a
snapshot: pushing such a branch takes write access. Labeling a pull request from
a fork does nothing.

Configuration:

| Name | Kind | Purpose |
|---|---|---|
| `SNAPSHOT_USERNAME` | organization secret | User the snapshot is deployed as, shared with the other builds that publish, so the account stays inventoried in one place. |
| `SNAPSHOT_PASSWORD` | organization secret | Its password or token. |
| `MAVEN_SNAPSHOT_DEPLOY_URL` | variable | Address the snapshot is uploaded to, which is the deploy endpoint of the repository rather than `MAVEN_SNAPSHOT_READ_URL` below: uploads go to the repository itself, resolving goes through the public address in front of it. It is the deploy endpoint the TeamCity snapshot builds of this repository publish to. Pointing this at the read address is refused with a 403. A variable rather than a secret, so the upload lines in the run log stay readable. |
| `MAVEN_SNAPSHOT_READ_URL` | variable (optional) | Address the pull request comment tells people to resolve from. Defaults to `https://maven.vaadin.com/vaadin-prereleases`, so only a repository publishing elsewhere has to set it. Public by nature — it is handed out in a comment. |

The `snapshot build` label has to exist in the repository for it to be
selectable.

## Cherry picks

`cherry-pick.yml` cherry-picks merged pull requests to maintenance branches
through the
[`cherry-pick`](https://github.com/vaadin/github-actions/tree/main/cherry-pick)
action of vaadin/github-actions. Add a `target/<branch>` label to a pull
request, for example `target/25.3`, and once it is merged the change is picked
onto that branch and a pull request titled `<title> (#<number>) (CP: <branch>)`
is opened for it. The original pull request is then labeled
`cherry-picked-<branch>`.

It runs on every push to main and every three hours. The schedule is what picks
up pull requests merged into a maintenance branch, and labels added after the
merge. Every run looks at all pull requests merged in the last 30 days, into
any branch, and skips targets that already have `cherry-picked-<branch>` or
`need to pick manually <branch>`, so it can run as often as needed.

Merge conflicts are handed to Claude Code, which resolves them, runs the unit
tests of the affected modules and `mvn spotless:apply`, and commits. Pull
requests picked that way say so in their description and are labeled
`ai-resolved-conflict`; review them with that in mind. A pick that cannot be
completed is labeled `need to pick manually <branch>` and is left to a human.

The workflow is currently in dry-run mode: it only logs what it would pick,
while the existing cherry-pick job keeps doing the picking.

Configuration:

| Name | Kind | Purpose |
|---|---|---|
| `CHERRY_PICK_TOKEN` | organization secret | Token used to read the cherry-pick script from vaadin/platform-build-script, push the pick branches, open the pull requests and label them. The workflow token is not enough: pull requests it opens do not trigger the validation workflow. |
| `ANTHROPIC_API_KEY` | secret | Used by Claude Code to resolve conflicts, shared with `claude.yml`. |
