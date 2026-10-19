/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { html, type TemplateResult } from 'lit';
import { toKey } from '../../algorithms/CellKeys.js';
import type { CellGridRenderState } from '../types.js';

/** The workbook's style tables, which the cells' rules are written from. */
export type StyleTables = Pick<
  CellGridRenderState,
  | 'cellStyleToCSSStyle'
  | 'displayGridlines'
  | 'rowIndexToStyleIndex'
  | 'columnIndexToStyleIndex'
  | 'conditionalFormattingStyles'
  | 'shiftedCellBorderStyles'
>;

/**
 * What the rules are written from. While none of it changes, the rules
 * written last still hold: a workbook's rules run to hundreds of kilobytes
 * of selectors, and a smooth scroll draws every frame.
 */
export function styleRuleInputs(state: StyleTables): readonly unknown[] {
  return [
    state.cellStyleToCSSStyle,
    state.displayGridlines,
    state.rowIndexToStyleIndex,
    state.columnIndexToStyleIndex,
    state.conditionalFormattingStyles,
    state.shiftedCellBorderStyles
  ];
}

/**
 * The most selectors one rule lists.
 *
 * Blink and WebKit keep the index of a selector inside its rule in 13 bits
 * and silently drop every selector that starts past the 8192nd simple
 * selector: a rule listing 3432 cells as `.cell.col<N>.row<N>` styled the
 * first 2731 of them and no more. A thousand selectors of
 * the three classes these carry stay well inside the limit.
 */
const MAX_SELECTORS_PER_RULE = 1000;

/** One rule per {@link MAX_SELECTORS_PER_RULE} selectors, each with the block. */
function chunkedRules(selectors: readonly string[], block: string): string[] {
  const rules: string[] = [];
  for (let start = 0; start < selectors.length; start += MAX_SELECTORS_PER_RULE) {
    rules.push(`${selectors.slice(start, start + MAX_SELECTORS_PER_RULE).join(', ')} ${block}`);
  }
  return rules;
}

/** The value a style body gives a property last, if it gives one. */
function lastValueOf(body: string | undefined, property: string): string | undefined {
  let value: string | undefined;
  for (const declaration of (body ?? '').split(';')) {
    const colon = declaration.indexOf(':');
    if (colon > 0 && declaration.slice(0, colon).trim() === property) {
      value = declaration.slice(colon + 1).trim();
    }
  }
  return value;
}

/** Rows or columns by the style index they are formatted with. */
function groupByStyle(byIndex: Readonly<Record<number, number>>): Map<number, number[]> {
  const out = new Map<number, number[]>();
  for (const [where, style] of Object.entries(byIndex)) {
    const list = out.get(style) ?? [];
    list.push(Number(where));
    out.set(style, list);
  }
  return out;
}

/**
 * The rules that style the cells, in the order they cascade: one per entry
 * in {@code cellStyleToCSSStyle}, of the form {@code .cell.cs<N> { <body> }}
 * with the body taken verbatim from the map - the server composes the
 * declarations - and the conditional formats and shifted borders after them.
 */
export function cellStyleRules(state: StyleTables): string[] {
  const rules: string[] = [];
  const styles = state.cellStyleToCSSStyle;
  // A sheet with its gridlines turned off. First, and no more specific than the style rules, so the
  // borders the workbook gives its cells - in their styles, conditional
  // formats and the borders shifted onto a neighbour - still draw.
  if (state.displayGridlines === false) {
    rules.push('.cell.cell { border-right: 0; border-bottom: 0; }');
  }
  // The workbook's default style under every cell. A style is sent with only what it changes from the default, so a
  // cell styled cs5 whose font colour is automatic carries no colour of its
  // own: scoped to .cs0 alone, the default reached none of those cells and
  // they fell through to the host's grey. Two classes, so it beats the
  // element's own .cell rule and loses to every cs<N> rule below it.
  //
  // The font goes through the documented properties so an application can
  // still set the cell font, which the workbook's default would otherwise
  // override on every cell. Unset, they resolve to the workbook's own font.
  if (styles[0] !== undefined) {
    rules.push(
      `.cell.cell { ${styles[0]} ` +
        'font-family: var(--vaadin-spreadsheet-cell-font, var(--default-font-family)); ' +
        'font-size: var(--vaadin-spreadsheet-cell-font-size, var(--default-font-size)); }'
    );
  }
  // Links: --vaadin-spreadsheet-link-fg when an application sets it, for a
  // link whose own style sets no colour. Unset, the default style's colour.
  // After the default rule, before the
  // cell styles, so a workbook's styled link keeps its own colour.
  rules.push(
    `.cell.link-cell { color: var(--vaadin-spreadsheet-link-fg, ${
      lastValueOf(styles[0], 'color') ?? 'var(--default-color)'
    }); }`
  );
  // A whole row or column formatted in the workbook styles those of its
  // cells that have no style of their own - which wear cs0.
  const rowsByStyle = groupByStyle(state.rowIndexToStyleIndex ?? {});
  const colsByStyle = groupByStyle(state.columnIndexToStyleIndex ?? {});
  for (const [key, body] of Object.entries(styles)) {
    const index = Number(key);
    if (index === 0) {
      // Already laid under every cell above. A second .cs0 rule would come
      // later and set the font directly, past the documented properties.
      continue;
    }
    const selectors = [`.cell.cs${index}`];
    for (const row of rowsByStyle.get(index) ?? []) {
      selectors.push(`.cell.row${row}.cs0`);
    }
    for (const col of colsByStyle.get(index) ?? []) {
      selectors.push(`.cell.col${col}.cs0`);
    }
    rules.push(...chunkedRules(selectors, `{ ${body} }`));
  }
  // Conditional formatting after the per-cell styles, and in ascending
  // key order: a rule that matches beats the cell's own style, and a
  // later rule beats an earlier one.
  const conditional = state.conditionalFormattingStyles ?? {};
  for (const index of Object.keys(conditional)
    .map(Number)
    .sort((a, b) => a - b)) {
    rules.push(`.cell.cf${index} { ${conditional[index]} }`);
  }
  // Shifted borders last. These arrive as complete rules selecting
  // `.col<N>.row<N>`; scope them to cells.
  for (const rule of state.shiftedCellBorderStyles ?? []) {
    const open = rule.indexOf('{');
    if (open < 0) {
      continue;
    }
    const selectors = rule
      .slice(0, open)
      .split(',')
      .map((selector) => selector.trim().replace(/\.col/g, '.cell.col'));
    rules.push(...chunkedRules(selectors, rule.slice(open)));
  }
  return rules;
}

/**
 * The {@code <style>} element the cells' rules go in - see
 * {@link cellStyleRules}, which always has the rule for links at least.
 */
export function buildCellStyleBlock(state: StyleTables): TemplateResult {
  const rules = cellStyleRules(state);
  // The rules go in as text, never as markup. A workbook names its own
  // fonts, and a name is written into these rules as it is.
  return html`<style>
    ${rules.join('\n')}
  </style>`;
}

/**
 * What of a cell's style the editor over it wears: how its text looks, and
 * its fill, so typing over a yellow cell stays on yellow. Not its borders,
 * indent or wrapping - those are the cell's box, and the editor keeps a box
 * of its own.
 */
const EDITOR_PROPERTIES: ReadonlySet<string> = new Set([
  'background-color',
  'color',
  'font-family',
  'font-size',
  'font-style',
  'font-weight',
  'text-align',
  'text-decoration',
  '--default-font-family',
  '--default-font-size'
]);

/** The declarations of a style body the editor wears, in order. */
function editorDeclarations(body: string | undefined): string[] {
  const out: string[] = [];
  for (const declaration of (body ?? '').split(';')) {
    const colon = declaration.indexOf(':');
    if (colon > 0 && EDITOR_PROPERTIES.has(declaration.slice(0, colon).trim())) {
      out.push(declaration.trim());
    }
  }
  return out;
}

/**
 * Inline style that dresses the editor as the cell it edits.
 *
 * The user types in the cell's font, colour, fill and alignment. The cell's
 * own classes would bring its borders and indent too, and holding those off would take
 * `!important` - which no `::part(cell-input)` rule of an application could
 * then override. So the styles are gathered here instead, in the order the
 * rules of {@link cellStyleRules} and the element's `.cell.r` resolve
 * for the cell: the default, its own style, the conditional formats it
 * matches, the right alignment of a number, and - for a cell with no style
 * of its own - its row or column style, whose rule is the most specific.
 */
export function editorCellStyle(
  state: Pick<
    CellGridRenderState,
    | 'cellData'
    | 'cellStyleToCSSStyle'
    | 'conditionalFormattingStyles'
    | 'rowIndexToStyleIndex'
    | 'columnIndexToStyleIndex'
  >,
  col: number,
  row: number
): string {
  const classes = (state.cellData.get(toKey(col, row))?.cellStyle ?? 'cs0').split(/\s+/);
  const styles = state.cellStyleToCSSStyle;
  const indexes = (prefix: string): number[] =>
    classes
      .filter((c) => new RegExp(`^${prefix}\\d+$`).test(c))
      .map((c) => Number(c.slice(prefix.length)))
      .sort((a, b) => a - b);

  // The default's font reaches the editor through the documented
  // properties its own rule reads, as the default rule does for a cell.
  const declarations = editorDeclarations(styles[0]).filter((d) => !/^font-(family|size)\s*:/.test(d));
  const own = indexes('cs')[0] ?? 0;
  if (own !== 0) {
    declarations.push(...editorDeclarations(styles[own]));
  }
  for (const index of indexes('cf')) {
    declarations.push(...editorDeclarations(state.conditionalFormattingStyles?.[index]));
  }
  if (classes.includes('r')) {
    declarations.push('text-align: right');
  }
  if (own === 0) {
    const lines = [state.rowIndexToStyleIndex?.[row], state.columnIndexToStyleIndex?.[col]];
    for (const index of lines.filter((i): i is number => !!i).sort((a, b) => a - b)) {
      declarations.push(...editorDeclarations(styles[index]));
    }
  }
  return declarations.map((d) => `${d};`).join('');
}
