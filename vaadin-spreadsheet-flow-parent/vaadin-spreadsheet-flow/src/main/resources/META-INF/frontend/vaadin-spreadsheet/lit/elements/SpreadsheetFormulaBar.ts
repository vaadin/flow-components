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
      height: var(--vaadin-spreadsheet-formulabar-height, 24px);
      background: var(--vaadin-spreadsheet-formulabar-bg, #fff);
      border-bottom: 1px solid var(--vaadin-spreadsheet-formulabar-border, #c0c0c0);
      font-family: var(--vaadin-spreadsheet-formulabar-font, inherit);
      font-size: var(--vaadin-spreadsheet-formulabar-font-size, inherit);
    }

    input {
      flex: 0 0 auto;
      box-sizing: border-box;
      height: 100%;
      padding: 0 4px;
      border: 0;
      outline: none;
      background: transparent;
      font: inherit;
      color: inherit;
    }

    .address {
      width: 80px;
      border-right: 1px solid var(--vaadin-spreadsheet-formulabar-border, #c0c0c0);
    }

    .formula {
      flex: 1 1 auto;
      min-width: 0;
    }

    .namedrangebox {
      flex: 0 0 auto;
      box-sizing: border-box;
      height: 100%;
      border: 0;
      border-right: 1px solid var(--vaadin-spreadsheet-formulabar-border, #c0c0c0);
      background: transparent;
      font: inherit;
      color: inherit;
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

  firstUpdated(): void {
    // Slice 175: when the user presses Enter inside the
    // address field, dispatch `sheet-address-changed` with
    // the input's current value. The connector translates
    // this into a `sheetAddressChanged` spreadsheet-event
    // for the server.
    const root = this.shadowRoot!;
    root.addEventListener('keydown', (e: Event) => {
      const ev = e as KeyboardEvent;
      if (ev.key !== 'Enter') {
        return;
      }
      const target = ev.target;
      if (!(target instanceof HTMLInputElement)) {
        return;
      }
      if (!target.classList.contains('address')) {
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
          </select>`;
    return html`
      <input class="address" part="address-field" .value=${address} />
      ${namedRangeBox}
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
