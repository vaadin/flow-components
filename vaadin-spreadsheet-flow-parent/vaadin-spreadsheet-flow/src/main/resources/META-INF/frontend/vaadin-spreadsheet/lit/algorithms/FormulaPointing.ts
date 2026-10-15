/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { cellCoordToAddress } from './CellRef.js';
import type { CellRect } from './SelectionPaint.js';

/**
 * Writing a formula by pointing at cells, as the GWT client let the user:
 * the references in the formula being written are coloured, on the sheet
 * and in the text, and after an operator the arrows and the mouse put a
 * reference into the formula instead of moving on.
 *
 * Pure functions on the text and the caret. Coordinates are 1-based.
 */

/** A cell, 1-based. */
export interface CellAt {
  col: number;
  row: number;
}

/**
 * The colours the GWT client gave a formula's references, in the order it
 * handed them out, as "R,G,B". A reference's cells are tinted at 0.25 and
 * the one the caret is in is outlined at 0.75.
 */
export const REFERENCE_COLORS: readonly string[] = [
  '48,144,240',
  '236,100,100',
  '152,223,88',
  '249,221,81',
  '36,220,212',
  '236,100,165',
  '104,92,176',
  '255,125,66',
  '51,97,144',
  '170,81,77',
  '127,176,83',
  '187,168,91',
  '36,121,129',
  '150,57,112',
  '75,86,168',
  '154,89,61'
];

/** A reference in a formula, where it is in the text and what it covers. */
export interface FormulaReference {
  /** Where it starts in the text. */
  start: number;
  /** Just after where it ends. */
  end: number;
  /** As it is written. */
  text: string;
  /** The cells it covers on the active sheet. */
  range: CellRect;
  /** Its colour, as "R,G,B". */
  color: string;
}

/** The largest column and row a reference can name: XFD and 1048576. */
const MAX_COL = 16384;
const MAX_ROW = 1048576;

/** A cell address, with or without $ - one capture for the column, one for the row. */
const CELL = '\\$?([A-Za-z]{1,3})\\$?([0-9]{1,7})';

/** An optional sheet - quoted or plain - and a cell or a range of cells. */
const REFERENCE = new RegExp(`(?:(?:'((?:[^']|'')+)'|([A-Za-z_][A-Za-z0-9_.]*))!)?${CELL}(?::${CELL})?`, 'y');

/** What may not touch a reference on either side: it would be part of a longer name. */
const NAME_CHAR = /[A-Za-z0-9_.$]/;

/** Whether a value being typed is a formula: it starts with = or +, as the GWT client took it. */
export function isFormula(text: string): boolean {
  return text.startsWith('=') || text.startsWith('+');
}

function columnIndex(letters: string): number {
  let col = 0;
  for (const ch of letters.toUpperCase()) {
    col = col * 26 + (ch.charCodeAt(0) - 64);
  }
  return col;
}

/** The cell a matched address names, or null past the sheet's last column or row. */
function cellOf(letters: string, digits: string): CellAt | null {
  const col = columnIndex(letters);
  const row = Number(digits);
  return col >= 1 && col <= MAX_COL && row >= 1 && row <= MAX_ROW ? { col, row } : null;
}

/** The key colours are shared by: the same cells, however they are written. */
function colorKey(range: CellRect): string {
  return `${range.c1},${range.r1}:${range.c2},${range.r2}`;
}

/**
 * The references to the active sheet in a formula, in the order they are
 * written, each with its colour. The same cells get the same colour
 * wherever they are named; the colours go round after sixteen.
 *
 * Not references: text in quotes, function names (LOG10 followed by a
 * bracket), a name that only starts like an address (TAX2024A), and an
 * address past the sheet's last column or row. A reference to another
 * sheet is not drawn on this one and takes no colour, as in the GWT
 * client.
 */
export function formulaReferences(text: string, activeSheet: string): FormulaReference[] {
  const refs: FormulaReference[] = [];
  if (!isFormula(text)) {
    return refs;
  }
  const colors = new Map<string, string>();
  let inString = false;
  for (let i = 1; i < text.length; i++) {
    const ch = text[i];
    if (ch === '"') {
      inString = !inString;
      continue;
    }
    if (inString || (i > 0 && NAME_CHAR.test(text[i - 1]))) {
      continue;
    }
    REFERENCE.lastIndex = i;
    const m = REFERENCE.exec(text);
    if (m === null) {
      continue;
    }
    const end = i + m[0].length;
    const next = text[end];
    if (next !== undefined && (NAME_CHAR.test(next) || next === '(' || next === '!')) {
      continue;
    }
    const sheet = m[1] !== undefined ? m[1].replace(/''/g, "'") : m[2];
    const first = cellOf(m[3], m[4]);
    const last = m[5] !== undefined ? cellOf(m[5], m[6]) : first;
    if (first === null || last === null) {
      continue;
    }
    i = end - 1;
    if (sheet !== undefined && sheet.toLowerCase() !== activeSheet.toLowerCase()) {
      continue;
    }
    const range: CellRect = {
      c1: Math.min(first.col, last.col),
      c2: Math.max(first.col, last.col),
      r1: Math.min(first.row, last.row),
      r2: Math.max(first.row, last.row)
    };
    const key = colorKey(range);
    if (!colors.has(key)) {
      colors.set(key, REFERENCE_COLORS[colors.size % REFERENCE_COLORS.length]);
    }
    refs.push({ start: i - m[0].length + 1, end, text: m[0], range, color: colors.get(key)! });
  }
  return refs;
}

/** The characters a reference can follow, so that the arrows point after them. */
const POINTS_AFTER = '=+-*/^&(,;:<>';

/**
 * Whether the arrows put a reference into the formula now: the caret
 * follows an operator, an opening bracket or a separator - the GWT client
 * took only ( + - / * and a lone =, and gave a comma nothing to point
 * with.
 */
export function canPoint(text: string, caret: number): boolean {
  if (!isFormula(text) || caret < 1) {
    return false;
  }
  const before = text.slice(0, caret).trimEnd();
  return before.length > 0 && POINTS_AFTER.includes(before[before.length - 1]);
}

/** The reference the caret is in or right after, if any. */
export function referenceAt(refs: ReadonlyArray<FormulaReference>, caret: number): FormulaReference | null {
  return refs.find((ref) => ref.start <= caret && caret <= ref.end) ?? null;
}

/** How a pointed cell or range is written: A1, or A1:C3 from its top left. */
export function referenceText(anchor: CellAt, end: CellAt): string {
  const first = { col: Math.min(anchor.col, end.col), row: Math.min(anchor.row, end.row) };
  const last = { col: Math.max(anchor.col, end.col), row: Math.max(anchor.row, end.row) };
  const a = cellCoordToAddress(first);
  return first.col === last.col && first.row === last.row ? a : `${a}:${cellCoordToAddress(last)}`;
}

/**
 * Puts a reference into a formula: in place of the span given - the
 * reference pointed at a moment ago, or the one the caret is in - or at
 * the caret. Returns the new text and where the reference now is; the
 * caret goes after it.
 */
export function placeReference(
  text: string,
  caret: number,
  span: { start: number; end: number } | null,
  reference: string
): { text: string; start: number; end: number } {
  const start = span !== null ? span.start : caret;
  const end = span !== null ? span.end : caret;
  return {
    text: text.slice(0, start) + reference + text.slice(end),
    start,
    end: start + reference.length
  };
}
