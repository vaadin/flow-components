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
import { Spreadsheet } from './lit/Spreadsheet.js';
import { spreadsheetStyles, spreadsheetOverlayStyles } from './vaadin-spreadsheet-styles.js';

const spreadsheetResizeObserver = new ResizeObserver((entries) => {
  entries.forEach((entry) => entry.target.$connector.resize());
});

/**
 * Connector-registration namespace, matching the convention from
 * vaadin-grid-flow's `gridConnector.ts` (slice 131's architectural
 * survey). The wrapper element calls `initLazy(this)` from its
 * `connectedCallback` instead of `new Spreadsheet(...)` directly,
 * so future code paths (e.g. dev-tools introspection,
 * cross-component lookups via `window.Vaadin.Flow.spreadsheetConnector`)
 * can rely on the standard hook.
 *
 * The `$connector` property on each element is the connector
 * instance; matches `grid.$connector` exactly so the team's
 * mental model stays consistent across components.
 */
window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.spreadsheetConnector = window.Vaadin.Flow.spreadsheetConnector || {
  initLazy: (element) => {
    if (element.$connector) {
      return;
    }
    // Slice 178: the TS/Lit Spreadsheet is the only client
    // implementation. The use-lit / global flag opt-in from
    // slice 162 is gone; the GWT bundle was deleted in this
    // slice. Every spreadsheet instance uses the Lit path.
    element.$connector = new Spreadsheet(element, element.renderRoot);
    element.$connector.setHeight('100%');
    element.$connector.setWidth('100%');
    element.createCallbacks();
  },
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
    return html`<slot name="overlays"></slot>`;
  }

  connectedCallback() {
    super.connectedCallback();
    spreadsheetResizeObserver.observe(this);
    if (!this.$connector) {
      // Per-instance overlay container slotted into this element (not appended
      // to document.body), so overlay styles resolve correctly when the
      // spreadsheet is nested in another element's shadow root. Required by the
      // getRootNode() adoption below. From main #9303 / #9713.
      this._overlays = document.createElement('div');
      this._overlays.id = 'spreadsheet-overlays';
      this._overlays.slot = 'overlays';
      this.appendChild(this._overlays);

      window.Vaadin.Flow.spreadsheetConnector.initLazy(this);

      this._firstUpdate = true;
    }

    // The overlay container lives in light DOM, so the overlay styles are
    // injected into `document.head` (see constructor). That does not reach the
    // container when `<vaadin-spreadsheet>` is nested inside another element's
    // shadow root. In that case, also adopt the overlay styles onto the
    // container's root so the scoped rules apply there too.
    const root = this._overlays.getRootNode();
    if (root instanceof ShadowRoot && !root.adoptedStyleSheets.includes(spreadsheetOverlayStyles.styleSheet)) {
      root.adoptedStyleSheets.push(spreadsheetOverlayStyles.styleSheet);
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    spreadsheetResizeObserver.unobserve(this);
  }

  updated(changedProperties) {
    super.updated(changedProperties);
    this._overlays.setAttribute('theme', this.getAttribute('theme'));
    let propNames = [];
    let dirty = false;
    changedProperties.forEach((oldValue, name) => {
      let newVal = this[name];
      if ('dirty' == name) {
        dirty = true;
      } else if ('rowBufferSize' == name) {
        this.$connector.setRowBufferSize(newVal);
      } else if ('columnBufferSize' == name) {
        this.$connector.setColumnBufferSize(newVal);
      } else if ('rows' == name) {
        this.$connector.setRows(newVal);
      } else if ('cols' == name) {
        this.$connector.setCols(newVal);
      } else if ('colGroupingData' == name) {
        this.$connector.setColGroupingData(newVal);
      } else if ('rowGroupingData' == name) {
        this.$connector.setRowGroupingData(newVal);
      } else if ('colGroupingMax' == name) {
        this.$connector.setColGroupingMax(newVal);
      } else if ('rowGroupingMax' == name) {
        this.$connector.setRowGroupingMax(newVal);
      } else if ('colGroupingInversed' == name) {
        this.$connector.setColGroupingInversed(newVal);
      } else if ('rowGroupingInversed' == name) {
        this.$connector.setRowGroupingInversed(newVal);
      } else if ('defRowH' == name) {
        this.$connector.setDefRowH(newVal);
      } else if ('defColW' == name) {
        this.$connector.setDefColW(newVal);
      } else if ('rowH' == name) {
        this.$connector.setRowH(newVal);
      } else if ('colW' == name) {
        this.$connector.setColW(newVal);
      } else if ('reload' == name) {
        this.$connector.setReload(true);
      } else if ('sheetIndex' == name) {
        this.$connector.setSheetIndex(newVal);
      } else if ('sheetNames' == name) {
        this.$connector.setSheetNames(newVal);
      } else if ('cellStyleToCSSStyle' == name) {
        this.$connector.setCellStyleToCSSStyle(newVal);
      } else if ('rowIndexToStyleIndex' == name) {
        this.$connector.setRowIndexToStyleIndex(newVal);
      } else if ('columnIndexToStyleIndex' == name) {
        this.$connector.setColumnIndexToStyleIndex(newVal);
      } else if ('lockedColumnIndexes' == name) {
        this.$connector.setLockedColumnIndexes(newVal);
      } else if ('lockedRowIndexes' == name) {
        this.$connector.setLockedRowIndexes(newVal);
      } else if ('shiftedCellBorderStyles' == name) {
        this.$connector.setShiftedCellBorderStyles(newVal);
      } else if ('conditionalFormattingStyles' == name) {
        this.$connector.setConditionalFormattingStyles(newVal);
      } else if ('hiddenColumnIndexes' == name) {
        this.$connector.setHiddenColumnIndexes(newVal);
      } else if ('hiddenRowIndexes' == name) {
        this.$connector.setHiddenRowIndexes(newVal);
      } else if ('verticalScrollPositions' == name) {
        this.$connector.setVerticalScrollPositions(newVal);
      } else if ('horizontalScrollPositions' == name) {
        this.$connector.setHorizontalScrollPositions(newVal);
      } else if ('sheetProtected' == name) {
        this.$connector.setSheetProtected(newVal);
      } else if ('workbookProtected' == name) {
        this.$connector.setWorkbookProtected(newVal);
      } else if ('cellKeysToEditorIdMap' == name) {
        this.$connector.setCellKeysToEditorIdMap(newVal);
      } else if ('componentIDtoCellKeysMap' == name) {
        this.$connector.setComponentIDtoCellKeysMap(newVal);
      } else if ('hyperlinksTooltips' == name) {
        this.$connector.setHyperlinksTooltips(newVal);
      } else if ('cellComments' == name) {
        this.$connector.setCellComments(newVal);
      } else if ('cellCommentAuthors' == name) {
        this.$connector.setCellCommentAuthors(newVal);
      } else if ('visibleCellComments' == name) {
        this.$connector.setVisibleCellComments(newVal);
      } else if ('invalidFormulaCells' == name) {
        this.$connector.setInvalidFormulaCells(newVal);
      } else if ('hasActions' == name) {
        this.$connector.setHasActions(newVal);
      } else if ('overlays' == name) {
        this.$connector.setOverlays(newVal);
      } else if ('mergedRegions' == name) {
        this.$connector.setMergedRegions(newVal);
      } else if ('displayGridlines' == name) {
        this.$connector.setDisplayGridlines(newVal);
      } else if ('displayRowColHeadings' == name) {
        this.$connector.setDisplayRowColHeadings(newVal);
      } else if ('verticalSplitPosition' == name) {
        this.$connector.setVerticalSplitPosition(newVal);
      } else if ('horizontalSplitPosition' == name) {
        this.$connector.setHorizontalSplitPosition(newVal);
      } else if ('infoLabelValue' == name) {
        this.$connector.setInfoLabelValue(newVal);
      } else if ('workbookChangeToggle' == name) {
        this.$connector.setWorkbookChangeToggle(newVal);
      } else if ('invalidFormulaErrorMessage' == name) {
        this.$connector.setInvalidFormulaErrorMessage(newVal);
      } else if ('lockFormatColumns' == name) {
        this.$connector.setLockFormatColumns(newVal);
      } else if ('lockFormatRows' == name) {
        this.$connector.setLockFormatRows(newVal);
      } else if ('namedRanges' == name) {
        this.$connector.setNamedRanges(newVal);
      } else if ('id' == name) {
        this.$connector.setId(newVal);
      } else if ('class' == name) {
        this.$connector.setClass(newVal);
      } else if ('resources' == name) {
        this.$connector.setResources(this, newVal);
      } else if ('api' == name) {
      } else if ('theme' == name) {
      } else if ('showCustomEditorOnFocus' == name) {
        this.$connector.setShowCustomEditorOnFocus(newVal);
      } else {
        console.error('<vaadin-spreadsheet> unsupported property received from server: property=' + name);
      }
      propNames.push(name);
    });
    this.$connector.notifyStateChanges(propNames, this._firstUpdate);
    if (this._firstUpdate) {
      this.$connector.relayout();
      this._firstUpdate = false;
    }
  }

  // Flow can send RPC calls in the same task as the property writes that
  // configure the api, before Lit's microtask-scheduled `updated()` runs to
  // propagate those properties to the api. Calling `performUpdate()` flushes
  // the pending update synchronously so the api state is ready.
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
    this._flush();
    // The sheet widget's initial relayout is deferred via GWT's scheduler,
    // so `definedRowHeights` may not yet exist when this RPC arrives in the
    // same task as the initial property batch (e.g. opening a Dialog that
    // contains the Spreadsheet). Calling `setSelectedCellAndRange` before
    // it's populated would throw inside `$scrollAreaIntoView`. Re-queue the
    // call until the layout state is ready; only the latest pending call is
    // kept so rapid successive calls collapse to the final selection.
    if (!this.$connector.spreadsheetWidget.sheetWidget.definedRowHeights) {
      this._pendingSelection = [name, col, row, c1, c2, r1, r2, scroll];
      if (!this._pendingSelectionScheduled) {
        this._pendingSelectionScheduled = true;
        const applyPending = () => {
          if (!this.isConnected || !this._pendingSelection) {
            this._pendingSelectionScheduled = false;
            this._pendingSelection = null;
            return;
          }
          if (this.$connector.spreadsheetWidget.sheetWidget.definedRowHeights) {
            const args = this._pendingSelection;
            this._pendingSelection = null;
            this._pendingSelectionScheduled = false;
            this.$connector.setSelectedCellAndRange(...args);
            return;
          }
          setTimeout(applyPending, 10);
        };
        setTimeout(applyPending, 10);
      }
      return;
    }
    this._pendingSelection = null;
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
    this.$connector.onPopupButtonOpened(row, column, contentId, appId);
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

  /* SERVER RPC METHOD CALLBACKS */
  createCallbacks() {
    this.$connector.setGroupingCollapsedCallback((e) => {
      this.dispatchEvent(this.createEvent('groupingCollapsed', e));
    });

    this.$connector.setContextMenuClosedCallback((e) => {
      this.dispatchEvent(this.createEvent('contextMenuClosed', e));
    });

    this.$connector.setLevelHeaderClickedCallback((e) => {
      this.dispatchEvent(this.createEvent('levelHeaderClicked', e));
    });

    this.$connector.setOnSheetScrollCallback((e) => {
      this.dispatchEvent(this.createEvent('onSheetScroll', e));
    });

    this.$connector.setSheetAddressChangedCallback((e) => {
      this.dispatchEvent(this.createEvent('sheetAddressChanged', e));
    });

    this.$connector.setCellSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellSelected', e));
    });

    this.$connector.setCellRangeSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellRangeSelected', e));
    });

    this.$connector.setCellAddedToSelectionAndSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellAddedToSelectionAndSelected', e));
    });

    this.$connector.setCellsAddedToRangeSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('cellsAddedToRangeSelection', e));
    });

    this.$connector.setRowSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('rowSelected', e));
    });

    this.$connector.setRowAddedToRangeSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('rowAddedToRangeSelection', e));
    });

    this.$connector.setColumnSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('columnSelected', e));
    });

    this.$connector.setColumnAddedToSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('columnAddedToSelection', e));
    });

    this.$connector.setSelectionIncreasePaintedCallback((e) => {
      this.dispatchEvent(this.createEvent('selectionIncreasePainted', e));
    });

    this.$connector.setSelectionDecreasePaintedCallback((e) => {
      this.dispatchEvent(this.createEvent('selectionDecreasePainted', e));
    });

    this.$connector.setCellValueEditedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellValueEdited', e));
    });

    this.$connector.setSheetSelectedCallback((e) => {
      this.dispatchEvent(this.createEvent('sheetSelected', e));
    });

    this.$connector.setSheetRenamedCallback((e) => {
      this.dispatchEvent(this.createEvent('sheetRenamed', e));
    });

    this.$connector.setSheetCreatedCallback((e) => {
      this.dispatchEvent(this.createEvent('sheetCreated', e));
    });

    this.$connector.setCellRangePaintedCallback((e) => {
      this.dispatchEvent(this.createEvent('cellRangePainted', e));
    });

    this.$connector.setDeleteSelectedCellsCallback((e) => {
      this.dispatchEvent(this.createEvent('deleteSelectedCells', e));
    });

    this.$connector.setLinkCellClickedCallback((e) => {
      this.dispatchEvent(this.createEvent('linkCellClicked', e));
    });

    this.$connector.setRowsResizedCallback((e) => {
      this.dispatchEvent(this.createEvent('rowsResized', e));
    });

    this.$connector.setColumnResizedCallback((e) => {
      this.dispatchEvent(this.createEvent('columnResized', e));
    });

    this.$connector.setOnRowAutofitCallback((e) => {
      this.dispatchEvent(this.createEvent('onRowAutofit', e));
    });

    this.$connector.setOnColumnAutofitCallback((e) => {
      this.dispatchEvent(this.createEvent('onColumnAutofit', e));
    });

    this.$connector.setOnUndoCallback((e) => {
      this.dispatchEvent(this.createEvent('onUndo', e));
    });

    this.$connector.setOnRedoCallback((e) => {
      this.dispatchEvent(this.createEvent('onRedo', e));
    });

    this.$connector.setSetCellStyleWidthRatiosCallback((e) => {
      this.dispatchEvent(this.createEvent('setCellStyleWidthRatios', e));
    });

    this.$connector.setProtectedCellWriteAttemptedCallback((e) => {
      this.dispatchEvent(this.createEvent('protectedCellWriteAttempted', e));
    });

    this.$connector.setOnPasteCallback((e) => {
      this.dispatchEvent(this.createEvent('onPaste', e));
    });

    this.$connector.setClearSelectedCellsOnCutCallback((e) => {
      this.dispatchEvent(this.createEvent('clearSelectedCellsOnCut', e));
    });

    this.$connector.setUpdateCellCommentCallback((e) => {
      this.dispatchEvent(this.createEvent('updateCellComment', e));
    });

    this.$connector.setOnConnectorInitCallback((e) => {
      this.dispatchEvent(this.createEvent('onConnectorInit', e));
    });

    this.$connector.setContextMenuOpenOnSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('contextMenuOpenOnSelection', e));
    });

    this.$connector.setActionOnCurrentSelectionCallback((e) => {
      this.dispatchEvent(this.createEvent('actionOnCurrentSelection', e));
    });

    this.$connector.setRowHeaderContextMenuOpenCallback((e) => {
      this.dispatchEvent(this.createEvent('rowHeaderContextMenuOpen', e));
    });

    this.$connector.setActionOnRowHeaderCallback((e) => {
      this.dispatchEvent(this.createEvent('actionOnRowHeader', e));
    });

    this.$connector.setColumnHeaderContextMenuOpenCallback((e) => {
      this.dispatchEvent(this.createEvent('columnHeaderContextMenuOpen', e));
    });

    this.$connector.setActionOnColumnHeaderCallback((e) => {
      this.dispatchEvent(this.createEvent('actionOnColumnHeader', e));
    });

    this.$connector.setPopupButtonClickCallback((e) => {
      this.dispatchEvent(this.createEvent('popupButtonClick', e));
    });

    this.$connector.setPopupCloseCallback((e) => {
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
