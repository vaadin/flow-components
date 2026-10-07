/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, type TemplateResult } from 'lit';

/**
 * Input state for the {@link SpreadsheetFormulaBar} element.
 * Two display fields drive the strip:
 *
 * - {@code address} — the active cell's address ("A1") or its
 *   associated named-range name. Mirrors what the GWT path
 *   pushed via {@code updateFormulaBar(possibleName, col, row)}.
 * - {@code formula} — the active cell's formula or display value.
 *   Mirrors what the GWT path pushed via
 *   {@code showSelectedCell(name, col, row, cellValue, ...)}.
 *
 * Both fields are editable text inputs. Editing the address +
 * pressing Enter dispatches {@code sheet-address-changed} from
 * this element; the connector forwards as the
 * {@code sheetAddressChanged} server RPC. Slice 175.
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
 * No-decorator pattern matching every other internal element
 * (slice 132+).
 */
export class SpreadsheetFormulaBar extends LitElement {
  /**
   * Parts this element re-exports to the tree above it.
   *
   * `::part()` crosses exactly one shadow boundary. Every part the
   * spreadsheet documents is emitted inside one of these sub-elements,
   * which sit inside the component's own shadow root — two boundaries
   * away from an application's stylesheet. Without re-exporting them,
   * `vaadin-spreadsheet::part(cell)` matches nothing, and the whole
   * documented styling API silently does nothing.
   */
  static readonly EXPORTED_PARTS = 'address-field,formula-field,named-range-box';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
    :host {
      display: flex;
      align-items: stretch;
      box-sizing: border-box;
      height: var(--vaadin-spreadsheet-formulabar-height, var(--_vaadin-spreadsheet-formulabar-height, 30px));
      background: var(--vaadin-spreadsheet-formulabar-bg, var(--_vaadin-spreadsheet-formulabar-bg, #fff));
      border-bottom: 1px solid
        var(--vaadin-spreadsheet-formulabar-border, var(--_vaadin-spreadsheet-formulabar-border, #c7c7c7));
      font-family: var(--vaadin-spreadsheet-formulabar-font, var(--_vaadin-spreadsheet-formulabar-font, Helvetica));
      font-size: var(--vaadin-spreadsheet-formulabar-font-size, var(--_vaadin-spreadsheet-formulabar-font-size, 14px));
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

    /* The address field's panel, laid out as the GWT client's: wide
       enough for a long range or a named one, on its own ground. */
    .address-panel {
      position: relative;
      flex: 0 0 auto;
      box-sizing: border-box;
      width: 176px;
      height: 100%;
      border-right: 1px solid
        var(--vaadin-spreadsheet-formulabar-border, var(--_vaadin-spreadsheet-formulabar-border, #c7c7c7));
      background: #fafafa;
    }

    .address {
      width: 100%;
      text-align: center;
    }

    .formula {
      flex: 1 1 auto;
      min-width: 0;
    }

    /* The named-range picker, as the GWT client drew it: an arrow at the
       right end of the address field, over a select that cannot be seen
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

    .namedrange-arrow {
      position: absolute;
      top: 0;
      right: 0;
      width: 15px;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: x-small;
      pointer-events: none;
    }

    /* SpreadsheetTheme.LUMO, as the GWT client drew it: the address on a
       tinted panel, both fields indented by Lumo's spacing. */
    :host([theme~='lumo']) input {
      padding-left: var(--lumo-space-s);
    }

    :host([theme~='lumo']) .formula {
      padding-right: var(--lumo-space-xs);
    }

    :host([theme~='lumo']) .address-panel {
      background: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct)) var(--lumo-base-color);
    }

    :host([theme~='lumo']) .namedrange-arrow {
      font-size: var(--lumo-font-size-xs);
    }
  `;

  declare renderState: FormulaBarRenderState;

  constructor() {
    super();
    this.renderState = emptyFormulaBarRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    // Set on attach rather than in the constructor: a custom element
    // may not have attributes when it is created.
    this.setAttribute('exportparts', SpreadsheetFormulaBar.EXPORTED_PARTS);
  }

  /**
   * Whether the formula field is being typed into.
   *
   * Ending an edit blurs the field, and the blur would otherwise report
   * the same value all over again - and a blur with nothing typed is
   * not an edit at all.
   */
  private editingFormula = false;

  firstUpdated(): void {
    // Slice 175: when the user presses Enter inside the
    // address field, dispatch `sheet-address-changed` with
    // the input's current value. The connector translates
    // this into a `sheetAddressChanged` spreadsheet-event
    // for the server.
    const root = this.shadowRoot!;
    root.addEventListener('keydown', (e: Event) => {
      const ev = e as KeyboardEvent;
      const target = ev.target;
      if (!(target instanceof HTMLInputElement)) {
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
    // What is typed here shows in an open cell editor as it is typed, as
    // the GWT client had it.
    root.addEventListener('input', (e: Event) => {
      const target = e.target;
      if (target instanceof HTMLInputElement && target.classList.contains('formula')) {
        this.dispatchEvent(
          new CustomEvent('formula-input', { detail: { value: target.value }, bubbles: true, composed: true })
        );
      }
    });
  }

  /**
   * The keys that finish editing in the formula field.
   *
   * Enter commits and moves on to the next cell down, Tab to the next
   * one right; shift+Tab commits without moving, which is what the GWT
   * client did. Escape throws the edit away and puts the cell's own
   * value back. Every other key belongs to the field, and so does the
   * event - without stopping it, typing a formula would also be typing
   * into the sheet underneath.
   */
  private handleFormulaKeyDown(e: KeyboardEvent, input: HTMLInputElement): void {
    e.stopPropagation();
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
    const { address, formula, namedRanges = [] } = this.renderState;
    // The dropdown only appears once the workbook defines a named range,
    // matching Excel — an empty picker beside every sheet would be noise.
    const namedRangeBox =
      namedRanges.length === 0
        ? ''
        : html`<select class="namedrangebox" part="named-range-box" @change=${this.handleNamedRangeChange}>
              <option value=""></option>
              ${namedRanges.map((name) => html`<option value=${name}>${name}</option>`)}
            </select>
            <span class="namedrange-arrow">▼</span>`;
    return html`
      <div class="address-panel">
        <input class="address" part="address-field" .value=${address} />
        ${namedRangeBox}
      </div>
      <input class="formula" part="formula-field" .value=${formula} />
    `;
  }
}

customElements.define('vaadin-spreadsheet-internal-formula-bar', SpreadsheetFormulaBar);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-formula-bar': SpreadsheetFormulaBar;
  }
}
