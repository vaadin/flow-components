#!/bin/bash
# Runs checkstyle on all modules and reports the combined errors as log output.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$(dirname "$SCRIPT_DIR")" && pwd)"

cd "$PROJECT_ROOT"

# Install custom checkstyle rules module
mvn install -pl vaadin-flow-components-shared-parent/vaadin-flow-components-checkstyle-rules -q

# Run checkstyle with plain text output
mvn checkstyle:checkstyle -Dcheckstyle.output.format=plain -Dcheckstyle.output.file=target/checkstyle-result.txt -q

# Find all checkstyle-result.txt files and collect errors
error_count=0

while IFS= read -r result_file; do
    while IFS= read -r line; do
        # Make path relative to project root
        line="${line//$PROJECT_ROOT\//}"
        echo "$line"
        ((error_count++))
    done < <(grep "^\[ERROR\]" "$result_file" 2>/dev/null)
done < <(find . -path "*/target/checkstyle-result.txt" -type f)

echo ""
if [[ $error_count -gt 0 ]]; then
    echo "Found $error_count checkstyle error(s)"
    exit 1
else
    echo "No checkstyle errors found"
    exit 0
fi
