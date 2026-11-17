#!/bin/bash
# Runs the forbiddenapis checks on all modules and reports the combined errors
# as log output. Compiles the main and test classes of every module, and checks
# them against the signatures in forbidden-apis/ (see the forbiddenapis plugin
# in the root pom.xml).

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$(dirname "$SCRIPT_DIR")" && pwd)"

cd "$PROJECT_ROOT"

log_file="$(mktemp)"
trap 'rm -f "$log_file"' EXIT

# A violation is logged instead of failing its module, so that the modules
# that depend on it are still checked
mvn -B --fail-at-end test-compile \
    de.thetaphi:forbiddenapis:check@main \
    de.thetaphi:forbiddenapis:testCheck@test \
    -Dforbiddenapis.failOnViolation=false > "$log_file" 2>&1
build_status=$?

# A violation is two lines: the forbidden API and the class that calls it
awk '/^\[WARNING\] Forbidden / { sub(/^\[WARNING\]/, "[ERROR]"); print; getline; sub(/^\[WARNING\]/, "[ERROR]"); print }' "$log_file"
error_count=$(grep -c '^\[WARNING\] Forbidden ' "$log_file")

if [[ $build_status -ne 0 ]]; then
    # A compilation error, or a signature that no longer matches Flow
    grep -E '^\[ERROR\] (/|Failed to execute goal)' "$log_file" |
        awk '!seen[$0]++' | sed "s|$PROJECT_ROOT/||"
    ((error_count++))
fi

echo ""
if [[ $error_count -gt 0 ]]; then
    echo "Found $error_count checkstyle error(s)"
    exit 1
else
    echo "No checkstyle errors found"
    exit 0
fi
