/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, type TemplateResult } from 'lit';
import { SpreadsheetPart } from './SpreadsheetPart.js';
import { live } from 'lit/directives/live.js';
import { composing } from '../algorithms/KeyboardMoves.js';
import { borderColor, formulabarBg, formulabarFont, formulabarFontSize, headerBorderColor } from './ThemeProperties.js';

/**
 * Input state for the {@link SpreadsheetFormulaBar} element.
 * Two display fields drive the strip:
 *
 * - {@code address} — the active cell's address ("A1") or its
 *   associated named-range name, from
 *   {@code updateFormulaBar(possibleName, col, row)}.
 * - {@code formula} — the active cell's formula or display value.
 *   From {@code showSelectedCell(name, col, row, cellValue, ...)}.
 *
 * Both fields are editable text inputs. Editing the address +
 * pressing Enter dispatches {@code sheet-address-changed} from
 * this element; the connector forwards as the
 * {@code sheetAddressChanged} server RPC.
 */
export interface FormulaBarRenderState {
  /** Active cell's address (e.g. "A1") or named-range name. */
  address: string;
  /** Active cell's formula or display value. */
  formula: string;
  /**
   * Named ranges defined in the workbook. Rendered as a dropdown beside the
   * address field; picking one navigates to it. Empty hides the dropdown.
   */
  namedRanges: ReadonlyArray<string>;
  /**
   * The references of a formula being written, coloured behind its text.
   * Null or absent while no formula is.
   */
  highlights?: { text: string; references: ReadonlyArray<{ start: number; end: number; color: string }> } | null;
}

/** Helper: build an empty formula-bar render state. */
export function emptyFormulaBarRenderState(): FormulaBarRenderState {
  return { address: '', formula: '', namedRanges: [] };
}

/**
 * Internal formula-bar element rendered above the cell grid.
 *
 * Two inputs side-by-side:
 *
 * - Address field (left). Carries {@code part="address-field"} for
 *   the documented public selector
 *   {@code vaadin-spreadsheet::part(address-field)}.
 * - Formula field (right). Carries {@code part="formula-field"}.
 *
 * The bar is positioned above the spreadsheet's scroll container.
 * The connector wires it into the flex column layout +
 * appends/removes it depending on whether the consumer requested
 * a formula bar (default = visible; hide via
 * {@code Spreadsheet.HIDE_FUNCTION_BAR_STYLE}).
 *
 * No-decorator pattern matching every other internal element.
 */
export class SpreadsheetFormulaBar extends SpreadsheetPart {
  static readonly EXPORTED_PARTS = 'address-field,formula-field,named-range-box';

  static properties = {
    renderState: { attribute: false },
    formulaLocked: { attribute: false }
  };

  static styles = css`
    :host {
      position: relative;
      display: flex;
      align-items: stretch;
      box-sizing: border-box;
      height: var(--vaadin-spreadsheet-formulabar-height, 30px);
      background: ${formulabarBg};
      /* The bar's own border colour, or each line in its documented colour:
         this one in the header border colour, as in earlier versions. */
      --_line-color: ${headerBorderColor};
      border-bottom: 1px solid var(--vaadin-spreadsheet-formulabar-border, var(--_line-color));
      font-family: ${formulabarFont};
      font-size: ${formulabarFontSize};
    }

    :host([hidden]) {
      display: none !important;
    }

    input {
      flex: 0 0 auto;
      box-sizing: border-box;
      height: 100%;
      padding: 0 0 0 5px;
      border: 0;
      outline: none;
      background: transparent;
      font: inherit;
      color: inherit;
    }

    /* The address field's panel: wide enough for a long range or a named one, on its own ground. */
    .address-panel {
      position: relative;
      flex: 0 0 auto;
      box-sizing: border-box;
      width: 176px;
      height: 100%;
      /* The divider in the sheet's border colour, as in earlier versions. */
      --_line-color: ${borderColor};
      border-right: 1px solid var(--vaadin-spreadsheet-formulabar-border, var(--_line-color));
      background: #fafafa;
    }

    /* The address has a size but never a font of its own, so the
       browser's own font for text fields shows there - Arial in Chrome -
       beside a formula in Helvetica. A font set for the bar, by an
       application or by the theme, still writes both. */
    .address {
      width: 100%;
      text-align: center;
      font-family: var(--vaadin-spreadsheet-formulabar-font, var(--_vaadin-spreadsheet-formulabar-font, revert));
    }

    .formula {
      position: relative;
      z-index: 1;
      flex: 1 1 auto;
      min-width: 0;
    }

    /* The references of a formula being written, coloured behind the
       field's text: the same text, laid out as the field lays it out, in
       no colour, with each reference on its colour. The field has no
       ground of its own, so the colours show through it. */
    .formula-highlights {
      position: absolute;
      top: 0;
      bottom: 0;
      right: 0;
      display: flex;
      align-items: center;
      box-sizing: border-box;
      padding: 0 0 0 5px;
      overflow: hidden;
      white-space: pre;
      color: transparent;
      pointer-events: none;
    }

    .formula-highlights > span {
      border-radius: 2px;
    }

    /* The named-range picker: an arrow at the right end of the address field, over a select that cannot be seen
       but opens the list of names. */
    .namedrangebox {
      position: absolute;
      top: 0;
      right: 0;
      width: 15px;
      height: 100%;
      margin: 0;
      opacity: 0;
      cursor: pointer;
    }

    /* Where the arrow goes: a pixel in from the left of
       the panel's last 15px, on a 33px line from the top of the bar - a
       little below the middle. */
    .namedrange-arrow {
      position: absolute;
      top: 0;
      right: 0;
      width: 15px;
      height: 100%;
      box-sizing: border-box;
      padding-left: 1px;
      font-size: x-small;
      line-height: 33px;
      pointer-events: none;
    }

    /* SpreadsheetTheme.LUMO: the address on a tinted panel, both fields indented by Lumo's spacing. */
    :host([theme~='lumo']) input,
    :host([theme~='lumo']) .formula-highlights {
      padding-left: var(--lumo-space-s);
    }

    :host([theme~='lumo']) .formula {
      padding-right: var(--lumo-space-xs);
    }

    /* In Lumo the divider is in the header border colour, as in earlier
       versions. */
    :host([theme~='lumo']) .address-panel {
      --_line-color: ${headerBorderColor};
      background: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct)) var(--lumo-base-color);
    }

    :host([theme~='lumo']) .namedrange-arrow {
      font-size: var(--lumo-font-size-xs);
    }
  `;

  declare renderState: FormulaBarRenderState;

  /**
   * Whether the active cell is locked on a protected sheet. The formula
   * field is then off: on, it would take a whole formula and refuse it only
   * once it was written.
   */
  declare formulaLocked: boolean;

  constructor() {
    super();
    this.renderState = emptyFormulaBarRenderState();
    this.formulaLocked = false;
  }

  /**
   * Whether the formula field is being typed into.
   *
   * Ending an edit blurs the field, and the blur would otherwise report
   * the same value all over again - and a blur with nothing typed is
   * not an edit at all.
   */
  private editingFormula = false;

  /**
   * Whether the formula field has been typed into since it took the
   * keyboard. Only the user sets it: a value the sheet puts there does not.
   */
  private formulaTyped = false;

  firstUpdated(): void {
    // When the user presses Enter inside the
    // address field, dispatch `sheet-address-changed` with
    // the input's current value. The connector translates
    // this into a `sheetAddressChanged` spreadsheet-event
    // for the server.
    const root = this.shadowRoot!;
    root.addEventListener('keydown', (e: Event) => {
      const ev = e as KeyboardEvent;
      const target = ev.target;
      if (!(target instanceof HTMLInputElement) || composing(ev)) {
        // A key an input method is composing with is its own.
        return;
      }
      if (target.classList.contains('formula')) {
        this.handleFormulaKeyDown(ev, target);
        return;
      }
      if (ev.key !== 'Enter' || !target.classList.contains('address')) {
        return;
      }
      ev.preventDefault();
      this.dispatchEvent(
        new CustomEvent('sheet-address-changed', {
          detail: { value: target.value },
          bubbles: true,
          composed: true
        })
      );
      target.blur();
    });
    root.addEventListener('focusin', (e: Event) => {
      const target = e.target;
      if (target instanceof HTMLInputElement && target.classList.contains('formula')) {
        this.editingFormula = true;
        this.formulaTyped = false;
      }
    });
    // Clicking away keeps what was typed, as leaving a cell editor does.
    // It is how a value typed here reaches the sheet when the user goes
    // straight back to the cell rather than pressing a key. Where the
    // keyboard went goes along: back to an open cell editor, the edit
    // carries on there.
    root.addEventListener('focusout', (e: Event) => {
      const target = e.target;
      if (target instanceof HTMLInputElement && target.classList.contains('formula')) {
        this.commitFormula(target.value, undefined, false, (e as FocusEvent).relatedTarget);
      }
    });
    // What is typed here shows in an open cell editor as it is typed.
    root.addEventListener('input', (e: Event) => {
      const target = e.target;
      if (target instanceof HTMLInputElement && target.classList.contains('formula')) {
        this.formulaTyped = true;
        this.dispatchEvent(
          new CustomEvent('formula-input', {
            detail: { value: target.value, caret: target.selectionStart ?? 0 },
            bubbles: true,
            composed: true
          })
        );
        this.alignHighlights();
      }
    });
    // Where the caret is, for the reference it is in to be outlined.
    const reportCaret = (e: Event): void => {
      const target = e.target;
      if (target instanceof HTMLInputElement && target.classList.contains('formula')) {
        this.dispatchEvent(
          new CustomEvent('formula-caret', {
            detail: { value: target.value, caret: target.selectionStart ?? 0 },
            bubbles: true,
            composed: true
          })
        );
        this.alignHighlights();
      }
    };
    root.addEventListener('keyup', reportCaret);
    root.addEventListener('mouseup', reportCaret);
    root.addEventListener('focusin', reportCaret);
  }

  /**
   * Keeps a formula field nobody is typing in showing the cell's value.
   *
   * The field is bound by property, so Lit writes it only when the bound
   * value changes - and after an edit is committed or thrown away the
   * state often holds the same value as before the edit, while the input
   * still holds what was typed, so the field is written every time. While
   * the field has the keyboard it is the user's, and is left
   * alone.
   */
  updated(changed: Map<string, unknown>): void {
    if (!changed.has('renderState')) {
      return;
    }
    this.alignHighlights();
    const formula = this.shadowRoot?.querySelector<HTMLInputElement>('.formula');
    if (formula && this.shadowRoot!.activeElement !== formula && formula.value !== this.renderState.formula) {
      formula.value = this.renderState.formula;
    }
  }

  /**
   * The keys that finish editing in the formula field.
   *
   * Enter commits and moves on to the next cell down, Tab to the next
   * one right; shift+Tab commits without moving. Escape throws the edit away and puts the cell's own
   * value back. Every other key belongs to the field, and so does the
   * event - without stopping it, typing a formula would also be typing
   * into the sheet underneath.
   */
  private handleFormulaKeyDown(e: KeyboardEvent, input: HTMLInputElement): void {
    e.stopPropagation();
    if (e.key === 'ArrowUp' || e.key === 'ArrowDown' || e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
      // In a formula the sheet may take the arrow, to point at a cell
      // with it.
      const pointed = !this.dispatchEvent(
        new CustomEvent('formula-arrow', {
          detail: { key: e.key, shiftKey: e.shiftKey, value: input.value, caret: input.selectionStart ?? 0 },
          bubbles: true,
          composed: true,
          cancelable: true
        })
      );
      if (pointed) {
        e.preventDefault();
      }
      return;
    }
    if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault();
      this.commitFormula(input.value, e.key, e.shiftKey);
      input.blur();
      return;
    }
    if (e.key === 'Escape') {
      e.preventDefault();
      this.editingFormula = false;
      this.dispatchEvent(new CustomEvent('formula-cancel', { bubbles: true, composed: true }));
      input.blur();
    }
  }

  private commitFormula(
    value: string,
    key: string | undefined,
    shiftKey: boolean,
    relatedTarget: EventTarget | null = null
  ): void {
    if (!this.editingFormula) {
      return;
    }
    this.editingFormula = false;
    this.dispatchEvent(
      new CustomEvent('formula-commit', {
        detail: { value, key, shiftKey, relatedTarget },
        bubbles: true,
        composed: true
      })
    );
  }

  /**
   * Lines the coloured references up with the field's text: from where the
   * field starts, and scrolled as far as the field is when its text runs
   * past its end.
   */
  private alignHighlights(): void {
    const field = this.shadowRoot?.querySelector<HTMLInputElement>('.formula');
    const layer = this.shadowRoot?.querySelector<HTMLElement>('.formula-highlights');
    if (!field || !layer) {
      return;
    }
    layer.style.left = `${field.offsetLeft}px`;
    layer.scrollLeft = field.scrollLeft;
  }

  /** What the formula field holds, and where its caret is. */
  formulaFieldState(): { value: string; caret: number } | null {
    const field = this.shadowRoot?.querySelector<HTMLInputElement>('.formula');
    return field ? { value: field.value, caret: field.selectionStart ?? field.value.length } : null;
  }

  /** Puts a value into the formula field being typed into, the caret where given. */
  setFormulaFieldValue(value: string, caret: number): void {
    const field = this.shadowRoot?.querySelector<HTMLInputElement>('.formula');
    if (!field) {
      return;
    }
    field.value = value;
    field.setSelectionRange(caret, caret);
    this.formulaTyped = true;
    this.alignHighlights();
  }

  /** Whether the formula field has the keyboard. */
  formulaFieldHasFocus(): boolean {
    return this.shadowRoot?.activeElement?.classList.contains('formula') === true;
  }

  /**
   * Ends an edit in the formula field without committing it, and takes the
   * keyboard out of the field: the sheet it was for has gone.
   */
  abandonFormulaEdit(): void {
    if (!this.editingFormula) {
      return;
    }
    this.editingFormula = false;
    this.formulaTyped = false;
    this.shadowRoot?.querySelector<HTMLInputElement>('.formula')?.blur();
  }

  /**
   * Whether the user is writing in the formula field: it has the keyboard,
   * and has been typed into, or pointed from, since it took it.
   */
  formulaFieldEdited(): boolean {
    return this.formulaTyped && this.formulaFieldHasFocus();
  }

  /**
   * Picking a named range navigates to it. The server treats it exactly like
   * typing the name into the address field.
   *
   * Bound on the `<select>` itself rather than delegated from the shadow
   * root: a native `change` bubbles, but a synthetic one dispatched straight
   * at the element (which is how TestBench drives the picker) does not, and
   * a delegated listener would never see it.
   */
  private handleNamedRangeChange = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof HTMLSelectElement)) {
      return;
    }
    // The blank first entry is the "nothing picked" state, not an address
    // the server can navigate to.
    if (target.value === '') {
      return;
    }
    this.dispatchEvent(
      new CustomEvent('sheet-address-changed', {
        detail: { value: target.value },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Puts the address field back to the active cell's address.
   *
   * The field is bound by property, so Lit leaves the input alone whenever
   * the bound address has not changed - which is exactly the case after the
   * server rejects a typed address and the selection stays put. The input
   * has to be written directly or the rejected text sits there naming a
   * cell nobody is on.
   */
  revertAddress(): void {
    const input = this.shadowRoot?.querySelector('.address') as HTMLInputElement | null;
    if (input !== null && input !== undefined) {
      input.value = this.renderState.address;
    }
  }

  render(): TemplateResult {
    const { address, formula, namedRanges = [], highlights = null } = this.renderState;
    // The picker follows the address field: the range the selection is on, or the blank entry. Left on the range
    // picked before, picking that range again would not be a change.
    // `live` compares with the option as it is, which the user may have
    // changed since the last render.
    const selectedName = namedRanges.includes(address) ? address : '';
    // The dropdown only appears once the workbook defines a named range,
    // matching Excel — an empty picker beside every sheet would be noise.
    const namedRangeBox =
      namedRanges.length === 0
        ? ''
        : html`<select class="namedrangebox" part="named-range-box" @change=${this.handleNamedRangeChange}>
              <option value="" .selected=${live(selectedName === '')}></option>
              ${namedRanges.map(
                (name) => html`<option value=${name} .selected=${live(name === selectedName)}>${name}</option>`
              )}
            </select>
            <span class="namedrange-arrow">▼</span>`;
    return html`
      <div class="address-panel">
        <input class="address" part="address-field" .value=${address} />
        ${namedRangeBox}
      </div>
      ${highlights === null ? '' : this.renderHighlights(highlights)}
      <input class="formula" part="formula-field" .value=${formula} ?disabled=${this.formulaLocked} />
    `;
  }

  /** The text of a formula with its references on their colours - as text, never as markup. */
  private renderHighlights(highlights: NonNullable<FormulaBarRenderState['highlights']>): TemplateResult {
    const { text, references } = highlights;
    const pieces: TemplateResult[] = [];
    let at = 0;
    for (const ref of references) {
      if (ref.start > at) {
        pieces.push(html`<span>${text.slice(at, ref.start)}</span>`);
      }
      pieces.push(
        html`<span style="background-color:rgba(${ref.color},0.25)">${text.slice(ref.start, ref.end)}</span>`
      );
      at = ref.end;
    }
    if (at < text.length) {
      pieces.push(html`<span>${text.slice(at)}</span>`);
    }
    return html`<div class="formula-highlights" aria-hidden="true">${pieces}</div>`;
  }
}

customElements.define('vaadin-spreadsheet-internal-formula-bar', SpreadsheetFormulaBar);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-formula-bar': SpreadsheetFormulaBar;
  }
}
