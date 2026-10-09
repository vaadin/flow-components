/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import { LitElement } from 'lit';
import { Spreadsheet } from './lit/Spreadsheet.js';
import { spreadsheetStyles, spreadsheetOverlayStyles } from './vaadin-spreadsheet-styles.js';

const spreadsheetResizeObserver = new ResizeObserver((entries) => {
  entries.forEach((entry) => entry.target.$connector.resize());
});

function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

/**
 * The connector namespace, as the other components' connectors have one:
 * the element calls `initLazy(this)` from its `connectedCallback`, and the
 * connector instance is the element's `$connector`.
 */
window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.spreadsheetConnector = window.Vaadin.Flow.spreadsheetConnector || {
  initLazy: (element) => {
    if (element.$connector) {
      return;
    }
    element.$connector = new Spreadsheet(element, element.renderRoot);
    element.$connector.setHeight('100%');
    element.$connector.setWidth('100%');
  }
};

const overlayStyles = (() => {
  const $tpl = document.createElement('template');
  $tpl.innerHTML = `<style>${spreadsheetOverlayStyles.toString()}</style>`;
  return $tpl.content;
})();

export class VaadinSpreadsheet extends LitElement {
  static get styles() {
    return spreadsheetStyles;
  }

  static get properties() {
    return {
      dirty: { type: Number },

      id: { type: String },

      resources: { type: String },

      popupbuttons: { type: String },

      rowBufferSize: { type: Number },

      columnBufferSize: { type: Number },

      rows: { type: Number },

      cols: { type: Number },

      colGroupingData: { type: Object },

      rowGroupingData: { type: Object },

      colGroupingMax: { type: Number },

      rowGroupingMax: { type: Number },

      colGroupingInversed: { type: Boolean },

      rowGroupingInversed: { type: Boolean },

      defRowH: { type: Number },

      defColW: { type: Number },

      rowH: { type: Object },

      colW: { type: Object },

      reload: { type: Number },

      sheetIndex: { type: Number },

      sheetNames: { type: Object },

      cellStyleToCSSStyle: { type: Object },

      rowIndexToStyleIndex: { type: Object },

      columnIndexToStyleIndex: { type: Object },

      lockedColumnIndexes: { type: Object },

      lockedRowIndexes: { type: Object },

      shiftedCellBorderStyles: { type: Object },

      conditionalFormattingStyles: { type: Object },

      hiddenColumnIndexes: { type: Object },

      hiddenRowIndexes: { type: Object },

      verticalScrollPositions: { type: Object },

      horizontalScrollPositions: { type: Object },

      sheetProtected: { type: Boolean },

      workbookProtected: { type: Boolean },

      cellKeysToEditorIdMap: { type: Object },

      componentIDtoCellKeysMap: { type: Object },

      hyperlinksTooltips: { type: Object },

      cellComments: { type: Object },

      cellCommentAuthors: { type: Object },

      visibleCellComments: { type: Object },

      invalidFormulaCells: { type: Object },

      hasActions: { type: Boolean },

      overlays: { type: Object },

      mergedRegions: { type: Object },

      displayGridlines: { type: Boolean },

      displayRowColHeadings: { type: Boolean },

      verticalSplitPosition: { type: Number },

      horizontalSplitPosition: { type: Number },

      infoLabelValue: { type: String },

      workbookChangeToggle: { type: Boolean },

      invalidFormulaErrorMessage: { type: String },

      lockFormatColumns: { type: Boolean },

      lockFormatRows: { type: Boolean },

      namedRanges: { type: String },

      theme: { type: String, reflectToAttribute: true },

      showCustomEditorOnFocus: { type: Boolean }
    };
  }

  constructor() {
    super();

    if (!overlayStyles.parentElement) {
      // Append spreadsheet overlay styles to the document head
      document.head.appendChild(overlayStyles);
    }
  }

  connectedCallback() {
    super.connectedCallback();
    spreadsheetResizeObserver.observe(this);
    if (!this.$connector) {
      window.Vaadin.Flow.spreadsheetConnector.initLazy(this);

      this._firstUpdate = true;
    } else {
      // Moved, or back in the page after leaving it: listen to it again.
      this.$connector.connect();
    }

    // The filter popup's content is the application's components, in this
    // element's light DOM, so its styles are injected into `document.head`
    // (see constructor). That does not reach them when `<vaadin-spreadsheet>`
    // is nested inside another element's shadow root. In that case, also
    // adopt the overlay styles onto that root so the rules apply there too.
    const root = this.getRootNode();
    if (root instanceof ShadowRoot && !root.adoptedStyleSheets.includes(spreadsheetOverlayStyles.styleSheet)) {
      root.adoptedStyleSheets.push(spreadsheetOverlayStyles.styleSheet);
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    spreadsheetResizeObserver.unobserve(this);
    // The connector's page listeners hold it, and this element with it:
    // released, a sheet removed from the page can be collected.
    this.$connector?.disconnect();
  }

  updated(changedProperties) {
    super.updated(changedProperties);
    changedProperties.forEach((oldValue, name) => {
      const newVal = this[name];
      // Most server properties map to a `set<Name>` method on the connector.
      // A few need special handling, and dirty and theme are not forwarded
      // at all (still reported via notifyStateChanges below).
      if (name === 'dirty' || name === 'theme') {
        // No-op.
      } else if (name === 'reload') {
        this.$connector.setReload(true);
      } else if (name === 'resources') {
        this.$connector.setResources(this, newVal);
      } else {
        const setter = `set${capitalize(name)}`;
        if (typeof this.$connector[setter] === 'function') {
          this.$connector[setter](newVal);
        } else {
          console.error(`<vaadin-spreadsheet> unsupported property received from server: property=${name}`);
        }
      }
    });
    this.$connector.notifyStateChanges([...changedProperties.keys()], this._firstUpdate);
    this._firstUpdate = false;
  }

  // Flow can send RPC calls in the same task as the property writes that
  // configure the connector, before Lit's microtask-scheduled `updated()`
  // runs to pass those properties on. Calling `performUpdate()` flushes the
  // pending update synchronously so the connector's state is ready.
  _flush() {
    this.performUpdate();
  }

  /* CLIENT SIDE RPC METHODS */
  updateBottomRightCellValues(cellData) {
    this._flush();
    this.$connector.updateBottomRightCellValues(cellData);
  }

  updateTopLeftCellValues(cellData) {
    this._flush();
    this.$connector.updateTopLeftCellValues(cellData);
  }

  updateTopRightCellValues(cellData) {
    this._flush();
    this.$connector.updateTopRightCellValues(cellData);
  }

  updateBottomLeftCellValues(cellData) {
    this._flush();
    this.$connector.updateBottomLeftCellValues(cellData);
  }

  updateFormulaBar(possibleName, col, row) {
    this._flush();
    this.$connector.updateFormulaBar(possibleName, col, row);
  }

  invalidCellAddress() {
    this._flush();
    this.$connector.invalidCellAddress();
  }

  showSelectedCell(name, col, row, cellValue, formula, locked, initialSelection) {
    this._flush();
    this.$connector.showSelectedCell(name, col, row, cellValue, formula, locked, initialSelection);
  }

  showActions(actionDetails) {
    this._flush();
    this.$connector.showActions(actionDetails);
  }

  setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll) {
    // `_flush()` is the whole ordering guard here: the connector reads the
    // row and column sizes it already holds and schedules its own repaint,
    // so passing on the pending property batch first is enough.
    this._flush();
    this.$connector.setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll);
  }

  cellsUpdated(updatedCellData) {
    this._flush();
    this.$connector.cellsUpdated(updatedCellData);
  }

  refreshCellStyles() {
    this._flush();
    this.$connector.refreshCellStyles();
  }

  editCellComment(col, row) {
    this._flush();
    this.$connector.editCellComment(col, row);
  }

  onPopupButtonOpen(row, column, contentId, appId) {
    this._flush();
    this.$connector.onPopupButtonOpen(row, column, contentId, appId);
  }

  closePopup(row, column) {
    this._flush();
    this.$connector.closePopup(row, column);
  }

  addPopupButton(rawState) {
    this._flush();
    this.$connector.addPopupButton(rawState);
  }

  removePopupButton(rawState) {
    this._flush();
    this.$connector.removePopupButton(rawState);
  }
}

window.customElements.define('vaadin-spreadsheet', VaadinSpreadsheet);
