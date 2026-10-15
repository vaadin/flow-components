/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/** Characters Excel will not have in a sheet name. */
const FORBIDDEN = new Set(['/', '\\', '?', '*', ']', '[', ':']);

/** The longest name Excel accepts. */
const MAX_LENGTH = 31;

/**
 * Whether a workbook would accept this as a sheet name.
 *
 * Excel's rules: between 1 and 31 characters, and none of `/ \ ? * ] [ :`,
 * which it keeps for referring to sheets and files inside a formula.
 */
export function isValidSheetName(name: string | null | undefined): boolean {
  if (name === null || name === undefined) {
    return false;
  }
  if (name.length < 1 || name.length > MAX_LENGTH) {
    return false;
  }
  for (const ch of name) {
    if (FORBIDDEN.has(ch)) {
      return false;
    }
  }
  return true;
}

export type SheetRenameOutcome = { kind: 'rename'; name: string } | { kind: 'revert'; reason: RevertReason };

/** Why a rename was not carried out. */
export type RevertReason = 'invalid' | 'unchanged' | 'taken';

export interface SheetRenameInput {
  /** What the user typed. */
  proposed: string;
  /** Which tab is being renamed, 0-based. */
  index: number;
  /** Every sheet's name, in tab order. */
  names: ReadonlyArray<string>;
}

/**
 * What to do with the name the user typed into a sheet tab.
 *
 * A name the workbook would refuse, a name that is what the sheet is
 * already called, and a name another sheet has taken all leave the tab as
 * it was. Only a name that is none of those is worth telling the server
 * about. Excel says so with a dialog; the GWT client left a TODO where the
 * message would go and simply put the old name back, which is what this
 * keeps doing - the reason is reported so a caller can do better.
 */
export function resolveSheetRename(input: SheetRenameInput): SheetRenameOutcome {
  const { proposed, index, names } = input;
  if (!isValidSheetName(proposed)) {
    return { kind: 'revert', reason: 'invalid' };
  }
  if (names[index] === proposed) {
    return { kind: 'revert', reason: 'unchanged' };
  }
  // Taken whatever its case, as Excel and POI see it: POI refused such a
  // rename on the server while the tab already showed it.
  const lower = proposed.toLowerCase();
  if (names.some((name, i) => i !== index && name.toLowerCase() === lower)) {
    return { kind: 'revert', reason: 'taken' };
  }
  return { kind: 'rename', name: proposed };
}
