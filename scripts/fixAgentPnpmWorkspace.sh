#!/bin/bash
#
# fixAgentPnpmWorkspace.sh - repair a TeamCity agent whose pnpm installs
# escape the project directory.
#
# Symptom it fixes (flow-components 23.x/24.x ITs):
#
#   [INFO] Running `pnpm install` ...
#   ../..    | Progress: resolved 562, reused 562, added 562, done
#   Error: ERR_PNPM_IGNORED_BUILDS
#   ...
#   [ERROR] Vite process exited with non-zero exit code 1.
#   [ERROR] failed to load config from .../integration-tests/vite.config.ts
#   [ERROR] Error: Cannot find module 'rollup'
#
# Cause: a pnpm-workspace.yaml (and its pnpm-lock.yaml / node_modules) sitting
# in a PARENT of the agent's checkout directories. pnpm 11+ walks up, adopts it
# as the workspace root, and links dependencies there instead of into the
# project, so Vaadin Flow's generated vite config cannot resolve transitive
# deps such as `rollup`. A leftover generated `integration-tests/node_modules`
# from an earlier build then gets reused as is.
#
# Usage:
#   ./fixAgentPnpmWorkspace.sh                 # report only (default)
#   ./fixAgentPnpmWorkspace.sh --apply         # back up, then remove
#   ./fixAgentPnpmWorkspace.sh --work-dir DIR  # override work dir detection
#
# Exit codes: 0 = clean (or --apply succeeded), 2 = problems found in report
# mode, 1 = usage/precondition error. Safe to re-run; does nothing when clean.

set -u

APPLY=0
WORK_DIR=""
BACKUP_DIR="${TMPDIR:-/tmp}/pnpm-agent-fix-$(date +%Y%m%d-%H%M%S)"
found=0
removed=0

while [ $# -gt 0 ]; do
  case "$1" in
    --apply)    APPLY=1; shift;;
    --work-dir) WORK_DIR="${2:-}"; shift 2 || { echo "--work-dir needs a value" >&2; exit 1; };;
    -h|--help)  awk 'NR>1 && /^#/{sub(/^# ?/,""); print; next} NR>1{exit}' "$0"; exit 0;;
    *)          echo "unknown argument: $1 (see --help)" >&2; exit 1;;
  esac
done

say()  { printf '%s\n' "$*"; }
head2() { printf '\n== %s\n' "$*"; }

## Locate the agent work directory
if [ -z "$WORK_DIR" ]; then
  for props in /opt/agent/conf/buildAgent.properties /opt/buildagent/conf/buildAgent.properties \
               "$HOME/conf/buildAgent.properties"; do
    [ -f "$props" ] || continue
    WORK_DIR=$(sed -n 's/^[[:space:]]*workDir[[:space:]]*=[[:space:]]*//p' "$props" | tail -1)
    [ -n "$WORK_DIR" ] && break
  done
fi
if [ -z "$WORK_DIR" ]; then
  for cand in /opt/agent/work /opt/buildagent/work /home/teamcity/work "$HOME/work"; do
    [ -d "$cand" ] && { WORK_DIR="$cand"; break; }
  done
fi
[ -n "$WORK_DIR" ] && [ -d "$WORK_DIR" ] || {
  echo "could not find the agent work directory; pass --work-dir DIR" >&2; exit 1; }
WORK_DIR=$(cd "$WORK_DIR" && pwd -P)

say "agent work dir : $WORK_DIR"
say "mode           : $([ "$APPLY" = 1 ] && echo 'APPLY (files will be removed)' || echo 'report only')"

backup() {
  # backup <path> - copy into $BACKUP_DIR keeping the absolute path shape
  local src="$1" dest="$BACKUP_DIR${1}"
  mkdir -p "$(dirname "$dest")" 2>/dev/null || return 0
  cp -a "$src" "$dest" 2>/dev/null || say "    ! could not back up $src (removing anyway)"
}

drop() {
  # drop <path> <reason>
  local path="$1" reason="$2"
  found=$((found + 1))
  say "  - $path"
  say "      $reason"
  if [ "$APPLY" = 1 ]; then
    backup "$path"
    if rm -rf -- "$path"; then
      removed=$((removed + 1))
      say "      removed (backup: $BACKUP_DIR$path)"
    else
      say "      ! FAILED to remove - check permissions / run as the agent user"
    fi
  fi
}

## 1. pnpm workspace state in any PARENT of the checkout dirs
head2 "pnpm state above the checkout directories"
dir="$WORK_DIR"
while : ; do
  ws="$dir/pnpm-workspace.yaml"; lock="$dir/pnpm-lock.yaml"; mods="$dir/node_modules"
  has_marker=0
  [ -e "$ws" ] && has_marker=1
  [ -e "$lock" ] && has_marker=1

  [ -e "$ws" ]   && drop "$ws"   "workspace root adopted by pnpm for every checkout below it"
  [ -e "$lock" ] && drop "$lock" "shared lockfile written outside any project"
  if [ -d "$mods" ]; then
    if [ "$has_marker" = 1 ] && [ "$dir" != "/" ]; then
      drop "$mods" "dependencies linked here instead of into the project"
    else
      say "  ? $mods exists but has no pnpm-workspace.yaml/pnpm-lock.yaml next to it"
      say "      left alone on purpose - inspect it yourself if installs still escape"
    fi
  fi

  [ "$dir" = "/" ] && break
  parent=$(dirname "$dir")
  [ "$parent" = "$dir" ] && break
  dir="$parent"
done
[ "$found" = 0 ] && say "  (nothing found - no stray workspace above $WORK_DIR)"

## 2. stale generated merged-IT modules inside the checkouts
head2 "stale generated integration-tests/node_modules in checkouts"
before=$found
for d in "$WORK_DIR"/*/; do
  [ -d "${d}integration-tests/node_modules" ] || continue
  drop "${d%/}/integration-tests" "generated module reused from an earlier build (never reinstalled)"
done
[ "$found" = "$before" ] && say "  (none)"

## 3. config that could reintroduce the same layout - reported, never touched
head2 "config to review (not modified by this script)"
for f in "$HOME/.config/pnpm/rc" "$HOME/.npmrc" /etc/npmrc; do
  [ -f "$f" ] || continue
  hits=$(grep -nE 'node-linker|nodeLinker|shamefully|hoist|workspace|store-dir|lockfile' "$f" 2>/dev/null || true)
  [ -n "$hits" ] || continue
  say "  $f:"
  printf '%s\n' "$hits" | sed 's/^/      /'
done
env_hits=$(env | grep -E '^(PNPM_CONFIG_|NPM_CONFIG_|PNPM_HOME|COREPACK_)' || true)
if [ -n "$env_hits" ]; then
  say "  environment:"; printf '%s\n' "$env_hits" | sed 's/^/      /'
fi
if command -v pnpm >/dev/null 2>&1; then
  say "  global pnpm on PATH: $(command -v pnpm) -> $(pnpm --version 2>/dev/null)"
else
  say "  no global pnpm on PATH (Flow falls back to 'npx --yes pnpm', i.e. pnpm latest)"
fi

## Summary
head2 "summary"
if [ "$APPLY" = 1 ]; then
  say "  problems found : $found"
  say "  removed        : $removed"
  [ "$removed" -gt 0 ] && say "  backup         : $BACKUP_DIR"
  say ""
  say "  Re-run the failing build. Repeat this on every agent in the pool."
  exit 0
fi
if [ "$found" -gt 0 ]; then
  say "  $found problem(s) found, nothing changed."
  say "  Re-run with --apply to back up to $BACKUP_DIR and remove them."
  exit 2
fi
say "  agent looks clean."
exit 0
