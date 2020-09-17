#!/bin/bash

# Uncomment to debug
#DEBUG=1

set -euo pipefail

log() {
    [ -z  ${DEBUG+x} ] ||  echo "$@"
}

die() { 
    echo "$@" >&2 
    exit 1 
}

quit() {
    echo "$@"
    exit 0
}

CURRENT_BRANCH="$(git rev-parse --abbrev-ref HEAD)"
# Target branch in platform has same name as branch in vaadin-flow-components
TARGET_BRANCH="$CURRENT_BRANCH"

# Needs to point to a valid repo from vaadin platform
[ -z ${PATH_TO_PLATFORM+x} ] \
 || [ ! -f "$PATH_TO_PLATFORM/versions.json" ] \
  && die "PATH_TO_PLATFORM needs to be set to a valid directory"
VERSIONS_FILE="$PATH_TO_PLATFORM/versions.json"

(
    cd "$PATH_TO_PLATFORM"
    git checkout --quiet "$TARGET_BRANCH" || quit "Could not checkout branch $TARGET_BRANCH in $PATH_TO_PLATFORM"
)

# Given 2 versions as arguments, prints latest one
latestVersion() {
    echo -e "$1\n$2" | sed '/-/!{s/$/_/}' | sort -rV | sed 's/_$//' | head -1
}

# Extract versions from @NpmPackage annotations in the project.
declare -A packages
for item in $(grep @NpmPackage $(find . -name \*.java) | sed -e 's/.*"\([^"]\+\)".*"\([^"]\+\)".*/\1:\2/g')
do
    NAME="$(echo $item | cut -d: -f1)"
    VERSION="$(echo $item | cut -d: -f2)"
    if [ ${packages[$NAME]+_} ]
    then
        PREVIOUS_VERSION="${packages[$NAME]}"
        if [ "$VERSION" = "$PREVIOUS_VERSION" ]
        then
            log "Matching versions for $NAME ($VERSION)"
        else
            # Fail if there are 2 different versions for the same component
            die "Version mismatch for $NAME: Found $PREVIOUS_VERSION and $VERSION"
        fi
    fi
    packages[$NAME]=$VERSION
done

log "Versions found in NpmPackage annotations:"
for K in "${!packages[@]}"; do log $K --- ${packages[$K]}; done
log "---"

log "Comparing versions in $VERSIONS_FILE"
declare -A packagesForUpdate
for item in $(jq -r '.core + .vaadin | map(select(.npmName != null)) | map(.npmName + ":" + .jsVersion) | join("\n")' "$VERSIONS_FILE" )
do
    NAME="$(echo $item | cut -d: -f1)"
    VERSION_IN_PLATFORM="$(echo $item | cut -d: -f2)"
    if [ ${packages[$NAME]+_} ]
    then
        VERSION_IN_LOCAL_REPO="${packages[$NAME]}"
        if [ "$VERSION_IN_PLATFORM" = "$VERSION_IN_LOCAL_REPO" ]
        then
            log "$NAME is up-to-date ($VERSION_IN_PLATFORM)"
        elif [ "$VERSION_IN_PLATFORM" = "$(latestVersion $VERSION_IN_LOCAL_REPO $VERSION_IN_PLATFORM)" ]
        then
            # Ignore if version in platform is greater then version in this repo
            log "$NAME in platform ($VERSION_IN_PLATFORM) is grater then in local repo ($VERSION_IN_LOCAL_REPO)" 
        else
            # Mark for update 
            echo "$NAME needs update from $VERSION_IN_PLATFORM to $VERSION_IN_LOCAL_REPO" 
            packagesForUpdate[$NAME]=$VERSION_IN_LOCAL_REPO
        fi
    else
       log "Ignoring $NAME"
    fi
done

TEMP_FILE="$(mktemp)"
cp "$VERSIONS_FILE" "$TEMP_FILE"
for K in "${!packagesForUpdate[@]}"; do 
    # Gets the component name by the npmName.
    # Example value: core.vaadin-list-mixin
    FULL_ELEMENT=$(jq -r 'paths as $p | select ( $p[-1] == "npmName" and getpath($p) == "'"$K"'") | $p[0]+"."+$p[1]' "$TEMP_FILE" )
    # Possible values: "core" or "vaadin"
    ROOT_PATH="$(echo $FULL_ELEMENT | cut -d. -f1)"
    # Example value: vaadin-list-mixin
    ELEMENT="$(echo $FULL_ELEMENT | cut -d. -f2)"
    # Example value: 1.2.3-beta2
    VERSION="${packagesForUpdate[$K]}"
    # jq does not update in place, so each time a new temp file is created
    NEW_TEMP_FILE="$(mktemp)"
    jq --indent 4 ".$ROOT_PATH[\"$ELEMENT\"].jsVersion |= \"$VERSION\"" "$TEMP_FILE" > "$NEW_TEMP_FILE"
    rm "$TEMP_FILE"
    TEMP_FILE="$NEW_TEMP_FILE"
done
mv "$TEMP_FILE" "$VERSIONS_FILE"

cd "$PATH_TO_PLATFORM" 
if git diff --quiet --exit-code "versions.json"
then
    quit "No changes to versions.json"
fi

## TODO: Check if there is already an open pull-request
## curl "https://api.github.com/repos/vaadin/platform/pulls?state=open&base=$TARGET_BRANCH" | jq -r  'map_values(.head.ref) | join("\n")'

git checkout --quiet "$TARGET_BRANCH" || quit "Could not checkout branch $TARGET_BRANCH"

PR_BRANCH="update-versions-in-$TARGET_BRANCH-$(date +%s)"
git checkout -b "$PR_BRANCH"

git add versions.json
git commit --quiet -m "Updated versions.json"
git push --quiet -u origin HEAD

[ -z ${GITHUB_TOKEN+x} ] && quit "GITHUB_TOKEN not set. Pull request will not be created. Branch is $PR_BRANCH"

REPOSITORY=$(git config --get remote.origin.url | sed -e 's/git@github.com://' -e 's/\.git$//')

PR_NUMBER=$(jq -n \
        --arg title "Update versions.json in $TARGET_BRANCH" \
        --arg head "$PR_BRANCH" \
        --arg base "$TARGET_BRANCH" \
        '{"title": $title, "head": $head, "base": $base}' \
        | curl --silent -H "Content-Type: application/json" \
            -H "Authorization: token $GITHUB_TOKEN" \
            -X POST -d @- "https://api.github.com/repos/$REPOSITORY/pulls" \
        | jq -r .number )

echo "https://github.com/$REPOSITORY/pull/$PR_NUMBER"