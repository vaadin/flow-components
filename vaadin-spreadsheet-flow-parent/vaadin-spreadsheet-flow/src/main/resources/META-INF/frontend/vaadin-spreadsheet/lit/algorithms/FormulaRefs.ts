/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Extract cell references from a formula string. The formula bar uses
 * this to discover ranges to highlight as the user types — e.g.
 * `"=A1+SUM(B2:D5)"` produces `["A1", "B2:D5"]`.
 *
 * Pure functions on strings — no DOM, no state.
 *
 * Behavior contract: cell references are 1-3 letters + 1-7 digits
 * (the Excel A1-notation limits), optionally preceded by a sheet name
 * + `!`, optionally followed by `:` + another cell ref to form a
 * range. References are returned in document order with duplicates
 * preserved (the formula-bar's 16-color cycle treats each occurrence
 * as a distinct position). The 16-color list itself is a rendering
 * concern + lives in the rendering layer (Phase 3.3).
 */

/**
 * Build the per-call regex that recognizes a cell-reference token.
 * Pattern:
 *
 *     ^((sheet1|sheet2|...)!){0,1}([A-Za-z]{1,3}[0-9]{1,7})(:([A-Za-z]{1,3}[0-9]{1,7})){0,1}
 *
 * - Optional sheet prefix: a sheet-name from the active workbook
 *   followed by `!`.
 * - The cell ref: 1-3 letters + 1-7 digits.
 * - Optional range: `:` + another cell ref.
 *
 * Sheet names are interpolated with their literal text into the regex
 * — names that contain regex metacharacters (rare but legal in Excel:
 * the spec allows `.` and similar) MUST be escaped first. This util
 * escapes the names defensively.
 *
 * @param sheetNames Names of all sheets in the active workbook. Used
 *                   to compose the optional sheet-prefix alternation.
 *                   Must be non-empty — the caller (formula bar)
 *                   always provides at least one (the active sheet).
 */
function buildCellRefRegex(sheetNames: readonly string[]): RegExp {
    const escapedNames = sheetNames.map(escapeRegex).join('|');
    const singleCellRef = '([A-Za-z]{1,3}[0-9]{1,7})';
    // Build:    ^((sheet1|sheet2)!)?singleCellRef(:singleCellRef)?
    // The `{0,1}` in the Java original is `?` in JS regex — semantically
    // identical, more compact.
    const pattern =
        '^((' +
        escapedNames +
        ')!)?' +
        singleCellRef +
        '(:' +
        singleCellRef +
        ')?$';
    return new RegExp(pattern);
}

/**
 * Escape regex metacharacters so a sheet name with a `.` or other
 * special character is matched as a literal string in the cell-ref
 * regex. Excel permits some metacharacters in sheet names; the GWT
 * version didn't escape them, which would break refs against those
 * sheets. The TS version fixes it.
 *
 * Open question: should the new client preserve the GWT bug for
 * byte-equivalence with the GWT highlighting, or fix it? Fixing seems
 * obviously correct — a sheet named `"Q1.2026"` should produce working
 * references — but flagging here for review.
 */
function escapeRegex(name: string): string {
    return name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * Returns `true` iff `token` is a cell reference matching the active
 * workbook's sheet names (or unprefixed). Pure function.
 *
 * @param token       The candidate token (already split out of the
 *                    formula text — see `parseCellReferences`).
 * @param sheetNames  The active workbook's sheet names.
 */
export function isCellRef(
    token: string,
    sheetNames: readonly string[]
): boolean {
    if (sheetNames.length === 0) {
        return false;
    }
    return buildCellRefRegex(sheetNames).test(token);
}

/**
 * Extract all cell references from a formula string. Ranges
 * (`"A1:B3"`) are returned as a single string (NOT split into two
 * separate refs). The order is left-to-right as they appear in the
 * formula; duplicates are NOT deduplicated (the formula bar's color
 * cycle treats them as distinct positions).
 *
 * Algorithm:
 *
 * 1. Split the formula on any run of characters that aren't valid in a
 *    cell ref (`[^A-Za-z0-9:!]+`). This isolates each potential ref
 *    token along with any sheet prefix or colon-range.
 *
 * 2. Each token is tested against the cell-ref regex
 *    (`isCellRef(token, sheetNames)`).
 *
 * 3. Surviving tokens are returned in document order.
 *
 * @param formula     The formula text (typically starts with `=`).
 * @param sheetNames  Active workbook's sheet names, used by the
 *                    cell-ref regex's sheet-prefix alternation.
 * @returns The list of cell references found in document order.
 *          Ranges remain as a single token (`"A1:B3"`).
 */
export function parseCellReferences(
    formula: string,
    sheetNames: readonly string[]
): string[] {
    if (sheetNames.length === 0) {
        return [];
    }
    const regex = buildCellRefRegex(sheetNames);
    const tokens = formula.split(/[^A-Za-z0-9:!]+/);
    const refs: string[] = [];
    for (const token of tokens) {
        if (regex.test(token)) {
            refs.push(token);
        }
    }
    return refs;
}
