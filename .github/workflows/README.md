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
