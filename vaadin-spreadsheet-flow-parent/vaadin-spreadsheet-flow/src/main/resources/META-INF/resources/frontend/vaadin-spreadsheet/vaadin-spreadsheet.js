/**
 * @license
 * Copyright (c) 2019 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */

import {LitElement, html, css, unsafeCSS} from 'lit-element';
import { Spreadsheet } from './spreadsheet-export.js';
import css_valo from './spreadsheet-styles-valo.css';

/**
 * An example element.
 *
 * @slot - This element has a slot
 * @csspart button - The button
 */
export class VaadinSpreadsheet extends LitElement {

  static get properties() {
    return {
      api: {type: Object},

      /* SHARED STATE */
      dirty: {type: Number},

      width: {type: String},

      height: {type: String},

      id: {type: String},

      class: {type: String},

      resources: {type: String},

      popupbuttons: {type: String},

      rowBufferSize: {type: Number},

      columnBufferSize: {type: Number},

      rows: {type: Number},

      cols: {type: Number},

      //public void setColGroupingData(List<GroupingData> colGroupingData) {
      colGroupingData: {type: Object},

      //public void setRowGroupingData(List<GroupingData> rowGroupingData) {
      rowGroupingData: {type: Object},

      colGroupingMax: {type: Number},

      rowGroupingMax: {type: Number},

      colGroupingInversed: {type: Boolean},

      rowGroupingInversed: {type: Boolean},

      defRowH: {type: Number},

      defColW: {type: Number},

      //public void setRowH(float[] rowH) {
      rowH: {type: Object},

      //public void setColW(int[] colW) {
      colW: {type: Object},

      reload: {type: Number},

      sheetIndex: {type: Number},

      //public void setSheetNames(String[] sheetNames) {
      sheetNames: {type: Object},

      //public void setCellStyleToCSSStyle(HashMap<Integer, String> cellStyleToCSSStyle) {
      cellStyleToCSSStyle: {type: Object},

      //public void setRowIndexToStyleIndex(HashMap<Integer, Integer> rowIndexToStyleIndex) {
      rowIndexToStyleIndex: {type: Object},

      //public void setColumnIndexToStyleIndex(HashMap<Integer, Integer> columnIndexToStyleIndex) {
      columnIndexToStyleIndex: {type: Object},

      //public void setLockedColumnIndexes(Set<Integer> lockedColumnIndexes) {
      lockedColumnIndexes: {type: Object},

      //public void setLockedRowIndexes(Set<Integer> lockedRowIndexes) {
      lockedRowIndexes: {type: Object},

      //public void setShiftedCellBorderStyles(ArrayList<String> shiftedCellBorderStyles) {
      shiftedCellBorderStyles: {type: Object},

      //public void setConditionalFormattingStyles(HashMap<Integer, String> conditionalFormattingStyles) {
      conditionalFormattingStyles: {type: Object},

      //public void setHiddenColumnIndexes(ArrayList<Integer> hiddenColumnIndexes) {
      hiddenColumnIndexes: {type: Object},

      //public void setHiddenRowIndexes(ArrayList<Integer> hiddenRowIndexes) {
      hiddenRowIndexes: {type: Object},

      //public void setVerticalScrollPositions(int[] verticalScrollPositions) {
      verticalScrollPositions: {type: Object},

      //public void setHorizontalScrollPositions(int[] horizontalScrollPositions) {
      horizontalScrollPositions: {type: Object},

      sheetProtected: {type: Boolean},

      workbookProtected: {type: Boolean},

      //public void setCellKeysToEditorIdMap(HashMap<String, String> cellKeysToEditorIdMap) {
      cellKeysToEditorIdMap: {type: Object},

      //public void setComponentIDtoCellKeysMap(HashMap<String, String> componentIDtoCellKeysMap) {
      componentIDtoCellKeysMap: {type: Object},

      //public void setHyperlinksTooltips(HashMap<String, String> hyperlinksTooltips) {
      hyperlinksTooltips: {type: Object},

      //public void setCellComments(HashMap<String, String> cellComments) {
      cellComments: {type: Object},

      //public void setCellCommentAuthors(HashMap<String, String> cellCommentAuthors) {
      cellCommentAuthors: {type: Object},

      //public void setVisibleCellComments(ArrayList<String> visibleCellComments) {
      visibleCellComments: {type: Object},

      //public void setInvalidFormulaCells(Set<String> invalidFormulaCells) {
      invalidFormulaCells: {type: Object},

      hasActions: {type: Boolean},

      //public void setOverlays(HashMap<String, OverlayInfo> overlays) {
      overlays: {type: Object},

      //public void setMergedRegions(ArrayList<MergedRegion> mergedRegions) {
      mergedRegions: {type: Object},

      displayGridlines: {type: Boolean},

      displayRowColHeadings: {type: Boolean},

      verticalSplitPosition: {type: Number},

      horizontalSplitPosition: {type: Number},

      infoLabelValue: {type: String},

      workbookChangeToggle: {type: Boolean},

      invalidFormulaErrorMessage: {type: String},

      lockFormatColumns: {type: Boolean},

      lockFormatRows: {type: Boolean},

      //public void setNamedRanges(List<String> namedRanges) {
      namedRanges: {type: String},
    };
  }

  constructor() {
    super();
  }

  static get styles() {
    return css`
      :host {
        display: block;
        height: 100%;
        min-height: 400px;
      }
    `
  };

  render() {
    return html`
      <slot></slot>
    `;
  }

  connectedCallback() {
    super.connectedCallback()
    this.observer && this.observer.observe(this);
    // Restore styles in the case widget is reattached, it happens e.g in client router
    this.styles && this.styles.forEach(e => document.head.appendChild(e));
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.observer && this.observer.unobserve(this);
    // Remove styles added to the head by the Widget
    this.styles = document.head.querySelectorAll(`style[id^="spreadsheet-${this.id}"]`);
    this.styles.forEach(e => document.head.removeChild(e));
  }

  updated(_changedProperties) {
    super.updated(_changedProperties);
    let initial = false;
    if (!this.api) {
      this.injectStyle('css_valo', css_valo);

      let overlays = document.getElementById('spreadsheet-overlays');
      if (!overlays) {
        overlays = document.createElement('div');
        overlays.id = 'spreadsheet-overlays';
        document.body.appendChild(overlays);        
      }

      this.api = new Spreadsheet(this);
      this.createCallbacks();

      this.observer = new ResizeObserver(e => this.api.resize());
      initial = true;
    }
    let propNames = [];
    let dirty = false;
    _changedProperties.forEach((oldValue, name) => {
      let newVal = this[name];
      if ('dirty' == name) {
        dirty = true;
      } else if ('rowBufferSize' == name) {
        this.api.setRowBufferSize(newVal);
      } else if ('columnBufferSize' == name) {
        this.api.setColumnBufferSize(newVal);
      } else if ('rows' == name) {
        this.api.setRows(newVal);
      } else if ('cols' == name) {
        this.api.setCols(newVal);
      } else if ('colGroupingData' == name) {
        this.api.setColGroupingData(newVal);
      } else if ('rowGroupingData' == name) {
        this.api.setRowGroupingData(newVal);
      } else if ('colGroupingMax' == name) {
        this.api.setColGroupingMax(newVal);
      } else if ('rowGroupingMax' == name) {
        this.api.setRowGroupingMax(newVal);
      } else if ('colGroupingInversed' == name) {
        this.api.setColGroupingInversed(newVal);
      } else if ('rowGroupingInversed' == name) {
        this.api.setRowGroupingInversed(newVal);
      } else if ('defRowH' == name) {
        this.api.setDefRowH(newVal);
      } else if ('defColW' == name) {
        this.api.setDefColW(newVal);
      } else if ('rowH' == name) {
        this.api.setRowH(newVal);
      } else if ('colW' == name) {
        this.api.setColW(newVal);
      } else if ('reload' == name) {
        this.api.setReload(true);
      } else if ('sheetIndex' == name) {
        this.api.setSheetIndex(newVal);
      } else if ('sheetNames' == name) {
        this.api.setSheetNames(newVal);
      } else if ('cellStyleToCSSStyle' == name) {
        this.api.setCellStyleToCSSStyle(newVal);
      } else if ('rowIndexToStyleIndex' == name) {
        this.api.setRowIndexToStyleIndex(newVal);
      } else if ('columnIndexToStyleIndex' == name) {
        this.api.setColumnIndexToStyleIndex(newVal);
      } else if ('lockedColumnIndexes' == name) {
        this.api.setLockedColumnIndexes(newVal);
      } else if ('lockedRowIndexes' == name) {
        this.api.setLockedRowIndexes(newVal);
      } else if ('shiftedCellBorderStyles' == name) {
        this.api.setShiftedCellBorderStyles(newVal);
      } else if ('conditionalFormattingStyles' == name) {
        this.api.setConditionalFormattingStyles(newVal);
      } else if ('hiddenColumnIndexes' == name) {
        this.api.setHiddenColumnIndexes(newVal);
      } else if ('hiddenRowIndexes' == name) {
        this.api.setHiddenRowIndexes(newVal);
      } else if ('verticalScrollPositions' == name) {
        this.api.setVerticalScrollPositions(newVal);
      } else if ('horizontalScrollPositions' == name) {
        this.api.setHorizontalScrollPositions(newVal);
      } else if ('sheetProtected' == name) {
        this.api.setSheetProtected(newVal);
      } else if ('workbookProtected' == name) {
        this.api.setWorkbookProtected(newVal);
      } else if ('cellKeysToEditorIdMap' == name) {
        this.api.setCellKeysToEditorIdMap(newVal);
      } else if ('componentIDtoCellKeysMap' == name) {
        this.api.setComponentIDtoCellKeysMap(newVal);
      } else if ('hyperlinksTooltips' == name) {
        this.api.setHyperlinksTooltips(newVal);
      } else if ('cellComments' == name) {
        this.api.setCellComments(newVal);
      } else if ('cellCommentAuthors' == name) {
        this.api.setCellCommentAuthors(newVal);
      } else if ('visibleCellComments' == name) {
        this.api.setVisibleCellComments(newVal);
      } else if ('invalidFormulaCells' == name) {
        this.api.setInvalidFormulaCells(newVal);
      } else if ('hasActions' == name) {
        this.api.setHasActions(newVal);
      } else if ('overlays' == name) {
        this.api.setOverlays(newVal);
      } else if ('mergedRegions' == name) {
        this.api.setMergedRegions(newVal);
      } else if ('displayGridlines' == name) {
        this.api.setDisplayGridlines(newVal);
      } else if ('displayRowColHeadings' == name) {
        this.api.setDisplayRowColHeadings(newVal);
      } else if ('verticalSplitPosition' == name) {
        this.api.setVerticalSplitPosition(newVal);
      } else if ('horizontalSplitPosition' == name) {
        this.api.setHorizontalSplitPosition(newVal);
      } else if ('infoLabelValue' == name) {
        this.api.setInfoLabelValue(newVal);
      } else if ('workbookChangeToggle' == name) {
        this.api.setWorkbookChangeToggle(newVal);
      } else if ('invalidFormulaErrorMessage' == name) {
        this.api.setInvalidFormulaErrorMessage(newVal);
      } else if ('lockFormatColumns' == name) {
        this.api.setLockFormatColumns(newVal);
      } else if ('lockFormatRows' == name) {
        this.api.setLockFormatRows(newVal);
      } else if ('namedRanges' == name) {
        this.api.setNamedRanges(newVal);
      } else if ('width' == name) {
        this.api.setWidth(newVal);
      } else if ('height' == name) {
        this.api.setHeight(newVal);
      } else if ('id' == name) {
        this.api.setId(newVal);
      } else if ('class' == name) {
        this.api.setClass(newVal);
      } else if ('resources' == name) {
        this.api.setResources(this, newVal);
      } else if ('popupbuttons' == name) {
        this.api.setPopups(newVal);
      } else if ('api' == name) {
      } else {
        console.error('<vaadin-spreadsheet> unsupported property received from server: property=' + name);
      }
      propNames.push(name);
    });
    this.api.notifyStateChanges(propNames, initial);
    if (initial) {
      this.api.relayout();
    }
  }

  /* CLIENT SIDE RPC METHODS */
  updateBottomRightCellValues(cellData) { //ArrayList<CellData> cellData) {
    this.api.updateBottomRightCellValues(cellData);
  }

  updateTopLeftCellValues(cellData) { //ArrayList<CellData> cellData) {
    this.api.updateTopLeftCellValues(cellData);
  }

  updateTopRightCellValues(cellData) { //ArrayList<CellData> cellData) {
    this.api.updateTopRightCellValues(cellData);
  }

  updateBottomLeftCellValues(cellData) { //ArrayList<CellData> cellData) {
    this.api.updateBottomLeftCellValues(cellData);
  }

  updateFormulaBar(possibleName, col, row) { //String possibleName, int col, int row) {
    this.api.updateFormulaBar(possibleName, col, row);
  }

  invalidCellAddress() {
    this.api.invalidCellAddress();
  }

  showSelectedCell(name, col, row, cellValue, formula, locked, initialSelection) { // String name, int col, int row, String cellValue, boolean function, boolean locked, boolean initialSelection
    this.api.showSelectedCell(name, col, row, cellValue, formula, locked, initialSelection);
  }

  showActions(actionDetails) { //ArrayList<SpreadsheetActionDetails> actionDetails) {
    this.api.showActions(actionDetails);
  }

  setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll) { //String name, int col, int row, int c1, int c2, int r1, int r2, boolean scroll
    this.api.setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll);
  }

  cellsUpdated(updatedCellData) { //ArrayList<CellData> updatedCellData) {
    if (this.api) this.api.cellsUpdated(updatedCellData);
  }

  refreshCellStyles() {
    if (this.api) this.api.refreshCellStyles();
  }

  editCellComment(col, row) { // int col, int row
    this.api.editCellComment(col, row);
  }

  /* SERVER RPC METHOD CALLBACKS */
  createCallbacks() {
    this.api.setGroupingCollapsedCallback(e => {
      this.dispatchEvent(this.createEvent('groupingCollapsed', e));
    });

    this.api.setLevelHeaderClickedCallback(e => {
      this.dispatchEvent(this.createEvent('levelHeaderClicked', e));
    });

    this.api.setOnSheetScrollCallback(e => {
      this.dispatchEvent(this.createEvent('onSheetScroll', e));
    });

    this.api.setSheetAddressChangedCallback(e => {
      this.dispatchEvent(this.createEvent('sheetAddressChanged', e));
    });

    this.api.setCellSelectedCallback(e => {
      this.dispatchEvent(this.createEvent('cellSelected', e));
    });

    this.api.setCellRangeSelectedCallback(e => {
      this.dispatchEvent(this.createEvent('cellRangeSelected', e));
    });

    this.api.setCellAddedToSelectionAndSelectedCallback(e => {
      this.dispatchEvent(this.createEvent('cellAddedToSelectionAndSelected', e));
    });

    this.api.setCellsAddedToRangeSelectionCallback(e => {
      this.dispatchEvent(this.createEvent('cellsAddedToRangeSelection', e));
    });

    this.api.setRowSelectedCallback(e => {
      this.dispatchEvent(this.createEvent('rowSelected', e));
    });

    this.api.setRowAddedToRangeSelectionCallback(e => {
      this.dispatchEvent(this.createEvent('rowAddedToRangeSelection', e));
    });

    this.api.setColumnSelectedCallback(e => {
      this.dispatchEvent(this.createEvent('columnSelected', e));
    });

    this.api.setColumnAddedToSelectionCallback(e => {
      this.dispatchEvent(this.createEvent('columnAddedToSelection', e));
    });

    this.api.setSelectionIncreasePaintedCallback(e => {
      this.dispatchEvent(this.createEvent('selectionIncreasePainted', e));
    });

    this.api.setSelectionDecreasePaintedCallback(e => {
      this.dispatchEvent(this.createEvent('selectionDecreasePainted', e));
    });

    this.api.setCellValueEditedCallback(e => {
      this.dispatchEvent(this.createEvent('cellValueEdited', e));
    });

    this.api.setSheetSelectedCallback(e => {
      this.dispatchEvent(this.createEvent('sheetSelected', e));
    });

    this.api.setSheetRenamedCallback(e => {
      this.dispatchEvent(this.createEvent('sheetRenamed', e));
    });

    this.api.setSheetCreatedCallback(e => {
      this.dispatchEvent(this.createEvent('sheetCreated', e));
    });

    this.api.setCellRangePaintedCallback(e => {
      this.dispatchEvent(this.createEvent('cellRangePainted', e));
    });

    this.api.setDeleteSelectedCellsCallback(e => {
      this.dispatchEvent(this.createEvent('deleteSelectedCells', e));
    });

    this.api.setLinkCellClickedCallback(e => {
      this.dispatchEvent(this.createEvent('linkCellClicked', e));
    });

    this.api.setRowsResizedCallback(e => {
      this.dispatchEvent(this.createEvent('rowsResized', e));
    });

    this.api.setColumnResizedCallback(e => {
      this.dispatchEvent(this.createEvent('columnResized', e));
    });

    this.api.setOnRowAutofitCallback(e => {
      this.dispatchEvent(this.createEvent('onRowAutofit', e));
    });

    this.api.setOnColumnAutofitCallback(e => {
      this.dispatchEvent(this.createEvent('onColumnAutofit', e));
    });

    this.api.setOnUndoCallback(e => {
      this.dispatchEvent(this.createEvent('onUndo', e));
    });

    this.api.setOnRedoCallback(e => {
      this.dispatchEvent(this.createEvent('onRedo', e));
    });

    this.api.setSetCellStyleWidthRatiosCallback(e => {
      this.dispatchEvent(this.createEvent('setCellStyleWidthRatios', e));
    });

    this.api.setProtectedCellWriteAttemptedCallback(e => {
      this.dispatchEvent(this.createEvent('protectedCellWriteAttempted', e));
    });

    this.api.setOnPasteCallback(e => {
      this.dispatchEvent(this.createEvent('onPaste', e));
    });

    this.api.setClearSelectedCellsOnCutCallback(e => {
      this.dispatchEvent(this.createEvent('clearSelectedCellsOnCut', e));
    });

    this.api.setUpdateCellCommentCallback(e => {
      this.dispatchEvent(this.createEvent('updateCellComment', e));
    });

    this.api.setOnConnectorInitCallback(e => {
      this.dispatchEvent(this.createEvent('onConnectorInit', e));
    });

    this.api.setContextMenuOpenOnSelectionCallback(e => {
      this.dispatchEvent(this.createEvent('contextMenuOpenOnSelection', e));
    });

    this.api.setActionOnCurrentSelectionCallback(e => {
      this.dispatchEvent(this.createEvent('actionOnCurrentSelection', e));
    });

    this.api.setRowHeaderContextMenuOpenCallback(e => {
      this.dispatchEvent(this.createEvent('rowHeaderContextMenuOpen', e));
    });

    this.api.setActionOnRowHeaderCallback(e => {
      this.dispatchEvent(this.createEvent('actionOnRowHeader', e));
    });

    this.api.setColumnHeaderContextMenuOpenCallback(e => {
      this.dispatchEvent(this.createEvent('columnHeaderContextMenuOpen', e));
    });

    this.api.setActionOnColumnHeaderCallback(e => {
      this.dispatchEvent(this.createEvent('actionOnColumnHeader', e));
    });

    this.dispatchEvent(this.createEvent('onConnectorInit'), []);
  }

  createEvent(type, data) {
    return new CustomEvent('spreadsheet-event', {
      detail: {type, data}
    });
  }

  injectStyle(id, style) {
    let elm = document.getElementById(id);
    if (!elm) {
      elm = document.createElement('style');
      elm.id = id;
      document.head.append(elm);
    }
    elm.textContent = style;
  }
}

window.customElements.define('vaadin-spreadsheet', VaadinSpreadsheet);
