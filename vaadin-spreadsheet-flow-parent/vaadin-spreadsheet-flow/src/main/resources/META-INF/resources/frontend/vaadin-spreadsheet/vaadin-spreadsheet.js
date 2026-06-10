/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import { LitElement, html } from 'lit';
import { Spreadsheet } from './spreadsheet-export.js';
import { spreadsheetStyles, spreadsheetOverlayStyles } from './vaadin-spreadsheet-styles.js';

function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

const spreadsheetResizeObserver = new ResizeObserver((entries) => {
  entries.forEach((entry) => entry.target.api.resize());
});

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
      api: { type: Object },

      dirty: { type: Number },

      id: { type: String },

      class: { type: String },

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

  render() {
    return html``;
  }

  connectedCallback() {
    super.connectedCallback();
    spreadsheetResizeObserver.observe(this);
    if (!this.api) {
      this._overlays = document.getElementById('spreadsheet-overlays');
      if (!this._overlays) {
        this._overlays = document.createElement('div');
        this._overlays.id = 'spreadsheet-overlays';
        document.body.appendChild(this._overlays);
      }

      this.api = new Spreadsheet(this, this.renderRoot);
      this.api.setHeight('100%');
      this.api.setWidth('100%');
      this.createCallbacks();

      this._firstUpdate = true;
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    spreadsheetResizeObserver.unobserve(this);
  }

  updated(changedProperties) {
    super.updated(changedProperties);

    this._overlays.setAttribute('theme', this.getAttribute('theme'));

    changedProperties.forEach((oldValue, name) => {
      const newVal = this[name];
      // Most server properties map to a `set<Name>` method on the api. A few
      // need special handling, and dirty/api/theme are not forwarded at all
      // (still reported via notifyStateChanges below).
      if (name === 'dirty' || name === 'api' || name === 'theme') {
        // No-op.
      } else if (name === 'reload') {
        this.api.setReload(true);
      } else if (name === 'resources') {
        this.api.setResources(this, newVal);
      } else {
        const setter = `set${capitalize(name)}`;
        if (typeof this.api[setter] === 'function') {
          this.api[setter](newVal);
        } else {
          console.error(`<vaadin-spreadsheet> unsupported property received from server: property=${name}`);
        }
      }
    });

    this.api.notifyStateChanges([...changedProperties.keys()], this._firstUpdate);

    if (this._firstUpdate) {
      this.api.relayout();
      this._firstUpdate = false;
    }
  }

  /* CLIENT SIDE RPC METHODS */
  // Flow can send RPC calls in the same task as the property writes that
  // configure the api, before Lit's microtask-scheduled `updated()` runs to
  // propagate those properties to the api. Each method below calls
  // `performUpdate()` first to flush the pending update synchronously so the
  // api state is ready.
  updateBottomRightCellValues(cellData) {
    this.performUpdate();
    this.api.updateBottomRightCellValues(cellData);
  }

  updateTopLeftCellValues(cellData) {
    this.performUpdate();
    this.api.updateTopLeftCellValues(cellData);
  }

  updateTopRightCellValues(cellData) {
    this.performUpdate();
    this.api.updateTopRightCellValues(cellData);
  }

  updateBottomLeftCellValues(cellData) {
    this.performUpdate();
    this.api.updateBottomLeftCellValues(cellData);
  }

  updateFormulaBar(possibleName, col, row) {
    this.performUpdate();
    this.api.updateFormulaBar(possibleName, col, row);
  }

  invalidCellAddress() {
    this.performUpdate();
    this.api.invalidCellAddress();
  }

  showSelectedCell(name, col, row, cellValue, formula, locked, initialSelection) {
    this.performUpdate();
    this.api.showSelectedCell(name, col, row, cellValue, formula, locked, initialSelection);
  }

  showActions(actionDetails) {
    this.performUpdate();
    this.api.showActions(actionDetails);
  }

  setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll) {
    this.performUpdate();
    this.api.setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll);
  }

  cellsUpdated(updatedCellData) {
    this.performUpdate();
    this.api.cellsUpdated(updatedCellData);
  }

  refreshCellStyles() {
    this.performUpdate();
    this.api.refreshCellStyles();
  }

  editCellComment(col, row) {
    this.performUpdate();
    this.api.editCellComment(col, row);
  }

  onPopupButtonOpen(row, column, contentId, appId) {
    this.performUpdate();
    this.api.onPopupButtonOpened(row, column, contentId, appId);
  }

  closePopup(row, column) {
    this.performUpdate();
    this.api.closePopup(row, column);
  }

  addPopupButton(rawState) {
    this.performUpdate();
    this.api.addPopupButton(rawState);
  }

  removePopupButton(rawState) {
    this.performUpdate();
    this.api.removePopupButton(rawState);
  }

  /* SERVER RPC METHOD CALLBACKS */
  createCallbacks() {
    this.api.setGroupingCollapsedCallback((e) => {
      this.dispatchEvent(this.createEvent('groupingCollapsed', e));
    });

    this.api.setContextMenuClosedCallback((e) => {
      this.dispatchEvent(this.createEvent('contextMenuClosed', e));
    });

    this.api.setLevelHeaderClickedCallback((e) => {
      this.dispatchEvent(this.createEvent('levelHeaderClicked', e));
    });

    this.api.setOnSheetScrollCallback((e) => {
      this.dispatchEvent(this.createEvent('onSheetScroll', e));
    });

    this.api.setSheetAddressChangedCallback((e) => {
      this.dispatchEvent(this.createEvent('sheetAddressChanged', e));
    });

    this.api.setCellSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellSelected', e));
    });

    this.api.setCellRangeSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellRangeSelected', e));
    });

    this.api.setCellAddedToSelectionAndSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellAddedToSelectionAndSelected', e));
    });

    this.api.setCellsAddedToRangeSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('cellsAddedToRangeSelection', e));
    });

    this.api.setRowSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('rowSelected', e));
    });

    this.api.setRowAddedToRangeSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('rowAddedToRangeSelection', e));
    });

    this.api.setColumnSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('columnSelected', e));
    });

    this.api.setColumnAddedToSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('columnAddedToSelection', e));
    });

    this.api.setSelectionIncreasePaintedCallback((e) => {
      this.dispatchEvent(this.createEvent('selectionIncreasePainted', e));
    });

    this.api.setSelectionDecreasePaintedCallback((e) => {
      this.dispatchEvent(this.createEvent('selectionDecreasePainted', e));
    });

    this.api.setCellValueEditedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellValueEdited', e));
    });

    this.api.setSheetSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('sheetSelected', e));
    });

    this.api.setSheetRenamedCallback((e) => {
      this.dispatchEvent(this.createEvent('sheetRenamed', e));
    });

    this.api.setSheetCreatedCallback((e) => {
      this.dispatchEvent(this.createEvent('sheetCreated', e));
    });

    this.api.setCellRangePaintedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellRangePainted', e));
    });

    this.api.setDeleteSelectedCellsCallback((e) => {
      this.dispatchEvent(this.createEvent('deleteSelectedCells', e));
    });

    this.api.setLinkCellClickedCallback((e) => {
      this.dispatchEvent(this.createEvent('linkCellClicked', e));
    });

    this.api.setRowsResizedCallback((e) => {
      this.dispatchEvent(this.createEvent('rowsResized', e));
    });

    this.api.setColumnResizedCallback((e) => {
      this.dispatchEvent(this.createEvent('columnResized', e));
    });

    this.api.setOnRowAutofitCallback((e) => {
      this.dispatchEvent(this.createEvent('onRowAutofit', e));
    });

    this.api.setOnColumnAutofitCallback((e) => {
      this.dispatchEvent(this.createEvent('onColumnAutofit', e));
    });

    this.api.setOnUndoCallback((e) => {
      this.dispatchEvent(this.createEvent('onUndo', e));
    });

    this.api.setOnRedoCallback((e) => {
      this.dispatchEvent(this.createEvent('onRedo', e));
    });

    this.api.setSetCellStyleWidthRatiosCallback((e) => {
      this.dispatchEvent(this.createEvent('setCellStyleWidthRatios', e));
    });

    this.api.setProtectedCellWriteAttemptedCallback((e) => {
      this.dispatchEvent(this.createEvent('protectedCellWriteAttempted', e));
    });

    this.api.setOnPasteCallback((e) => {
      this.dispatchEvent(this.createEvent('onPaste', e));
    });

    this.api.setClearSelectedCellsOnCutCallback((e) => {
      this.dispatchEvent(this.createEvent('clearSelectedCellsOnCut', e));
    });

    this.api.setUpdateCellCommentCallback((e) => {
      this.dispatchEvent(this.createEvent('updateCellComment', e));
    });

    this.api.setOnConnectorInitCallback((e) => {
      this.dispatchEvent(this.createEvent('onConnectorInit', e));
    });

    this.api.setContextMenuOpenOnSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('contextMenuOpenOnSelection', e));
    });

    this.api.setActionOnCurrentSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('actionOnCurrentSelection', e));
    });

    this.api.setRowHeaderContextMenuOpenCallback((e) => {
      this.dispatchEvent(this.createEvent('rowHeaderContextMenuOpen', e));
    });

    this.api.setActionOnRowHeaderCallback((e) => {
      this.dispatchEvent(this.createEvent('actionOnRowHeader', e));
    });

    this.api.setColumnHeaderContextMenuOpenCallback((e) => {
      this.dispatchEvent(this.createEvent('columnHeaderContextMenuOpen', e));
    });

    this.api.setActionOnColumnHeaderCallback((e) => {
      this.dispatchEvent(this.createEvent('actionOnColumnHeader', e));
    });

    this.api.setPopupButtonClickCallback((e) => {
      this.dispatchEvent(this.createEvent('popupButtonClick', e));
    });

    this.api.setPopupCloseCallback((e) => {
      this.dispatchEvent(this.createEvent('popupClose', e));
    });

    this.dispatchEvent(this.createEvent('onConnectorInit'), []);
  }

  createEvent(type, data) {
    return new CustomEvent('spreadsheet-event', {
      detail: { type, data }
    });
  }
}

window.customElements.define('vaadin-spreadsheet', VaadinSpreadsheet);
