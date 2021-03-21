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
import { css_gwt, css_valo, Spreadsheet } from 'spreadsheet-export';

/**
 * An example element.
 *
 * @slot - This element has a slot
 * @csspart button - The button
 */
export class VaadinSpreadsheet extends LitElement {


  static get styles() {
    return css`    
    
      #mislot {
        border: 1px solid green;
        height: 200px;
      }
      
      slot {
        border: 1px solid green;
      }
    
      ${unsafeCSS(css_gwt)}

      ${unsafeCSS(css_valo)}
    `;
  }

  static get properties() {
    return {
      /**
       * The name to say "Hello" to.
       */
      name: {type: String},

      api: {type: Object},

      received: {type: String},
      /**
       * The number of times the button has been clicked.
       */
      count: {type: Number},



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

    reload: {type: Boolean},

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

  updateSharedState(newVal) {
    this.api.setState(newVal);
  }

  alert(newVal) {
    this.api.alert(newVal);
  }

  constructor() {
    super();
    this.name = 'World';
    this.count = 0;
    this.received = '';
  }

/*
  createRenderRoot() {
    // Do not use a shadow root
    return this;
  }
*/

  render() {
    return html`
      <h1>This is a web component</h1>
      <slot></slot>
    `;
  }

  _onClick() {
    this.count++;
    this.api.setState(this.name + ' - ' + this.count);
    console.log('Estado fijado a ' + this.name)
  }

  connectedCallback() {
    super.connectedCallback()
    console.log('connected')
  }

  addStyle(styleString) {
    const style = document.createElement('style');
    style.textContent = styleString;
    document.head.append(style);
  }

  updated(_changedProperties) {
    super.updated(_changedProperties);
    console.log(this.shadowRoot.querySelector('#mislot'));
    //console.log(this.querySelector('#mislot'));
    if (!this.api) {

      this.addStyle(css_gwt);
      this.addStyle(css_valo);

      const div = document.createElement('div');
      div.setAttribute('class', 'spreadsheetport');
      div.setAttribute('style', 'height: 300px;');
      this.append(div);

      this.api = new Spreadsheet(div);
      //this.api = new Spreadsheet(this.querySelector('#mislot'));
      this.api.registerClicked(e => {
        this.received = '' + e;
        let event = new CustomEvent('my-event', {
          detail: {
            message: '' + e
          }
        });
        this.dispatchEvent(event);
      }); //this.api.alert('webcomponent callback called for ' + e));
      this.api.alert('Hello from web component');
      console.log('updated')
    }
  }

  attributeChangedCallback(name, oldVal, newVal) {
    console.log('attribute change: ', name, newVal);
    if ('name' == name) {
      this.api.setState(newVal);
    }
    super.attributeChangedCallback(name, oldVal, newVal);
  }




  /*
  CLIENT SIDE RPC METHODS
   */

  updateBottomRightCellValues(cellData) { //ArrayList<CellData> cellData) {
    this.api.updateBottomRightCellValues(cellData);
  }

  updateTopLeftCellValues(cellData) { //ArrayList<CellData> cellData) {
    this.api.updateTopLeftCellValues(cellData);
  }

  updateTopRightCellValues(cellData) { //ArrayList<CellData> cellData) {
    this.api.updateTopRightCellValues(cellData);
  }

  updateBottomLeftCellValues(celldata) { //ArrayList<CellData> cellData) {
    this.api.updateBottomLeftCellValues(cellData);
  }

  updateFormulaBar(possibleName, col, row) { //String possibleName, int col, int row) {
    this.api.updateFormulaBar(possibleName, col, row);
  }

  invalidCellAddress() {
  this.api.invalidCellAddress();
  }

  showSelectedCell(name, col, row, cellValue, _function, locked, initialSelection) { // String name, int col, int row, String cellValue, boolean function, boolean locked, boolean initialSelection
    this.api.showSelectedCell(name, col, row, cellValue, _function, locked, initialSelection);
  }

  showActions(actionDetails) { //ArrayList<SpreadsheetActionDetails> actionDetails) {
    this.api.showActions(actionDetails);
  }

  setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll) { //String name, int col, int row, int c1, int c2, int r1, int r2, boolean scroll
    this.api.setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll);
  }

  cellsUpdated(updatedCellData) { //ArrayList<CellData> updatedCellData) {
    this.api.cellsUpdated(updatedCellData);
  }

  refreshCellStyles() {
    this.api.refreshCellStyles();
  }

  editCellComment(col, row) { // int col, int row
    this.api.editCellComment(col, row);
  }







  /*
SERVER RPC METHOD CALLBACKS
 */

  setGroupingCollapsedCallback(callback) {
    this.api.setGroupingCollapsedCallback(callback);
  }

  setLevelHeaderClickedCallback(callback) {
    this.api.setLevelHeaderClickedCallback(callback);
  }

  setOnSheetScrollCallback(callback) {
    this.api.setOnSheetScrollCallback(callback);
  }

  setSheetAddressChangedCallback(callback) {
    this.api.setSheetAddressChangedCallback(callback);
  }

  setCellSelectedCallback(callback) {
    this.api.setCellSelectedCallback(callback);
  }

  setCellRangeSelectedCallback(callback) {
    this.api.setCellRangeSelectedCallback(callback);
  }

  setCellAddedToSelectionAndSelectedCallback(callback) {
    this.api.setCellAddedToSelectionAndSelectedCallback(callback);
  }

  setCellsAddedToRangeSelectionCallback(callback) {
    this.api.setCellsAddedToRangeSelectionCallback(callback);
  }

  setRowSelectedCallback(callback) {
    this.api.setRowSelectedCallback(callback);
  }

  setRowAddedToRangeSelectionCallback(callback) {
    this.api.setRowAddedToRangeSelectionCallback(callback);
  }

  setColumnSelectedCallback(callback) {
    this.api.setColumnSelectedCallback(callback);
  }

  setColumnAddedToSelectionCallback(callback) {
    this.api.setColumnAddedToSelectionCallback(callback);
  }

  setSelectionIncreasePaintedCallback(callback) {
    this.api.setSelectionIncreasePaintedCallback(callback);
  }


  setSelectionDecreasePaintedCallback(callback) {
    this.api.setSelectionDecreasePaintedCallback(callback);
  }

  setCellValueEditedCallback(callback) {
    this.api.setCellValueEditedCallback(callback);
  }

  setSheetSelectedCallback(callback) {
    this.api.setSheetSelectedCallback(callback);
  }

  setSheetRenamedCallback(callback) {
    this.api.setSheetRenamedCallback(callback);
  }

  setSheetCreatedCallback(callback) {
    this.api.setSheetCreatedCallback(callback);
  }

  setCellRangePaintedCallback(callback) {
    this.api.setCellRangePaintedCallback(callback);
  }

  setDeleteSelectedCellsCallback(callback) {
    this.api.setDeleteSelectedCellsCallback(callback);
  }

  setLinkCellClickedCallback(callback) {
    this.api.setLinkCellClickedCallback(callback);
  }

  setRowsResizedCallback(callback) {
    this.api.setRowsResizedCallback(callback);
  }

  setColumnResizedCallback(callback) {
    this.api.setColumnResizedCallback(callback);
  }

  setOnRowAutofitCallback(callback) {
    this.api.setOnRowAutofitCallback(callback);
  }

  setOnColumnAutofitCallback(callback) {
    this.api.setOnColumnAutofitCallback(callback);
  }

  setOnUndoCallback(callback) {
    this.api.setOnUndoCallback(callback);
  }

  setOnRedoCallback(callback) {
    this.api.setOnRedoCallback(callback);
  }

  setSetCellStyleWidthRatiosCallback(callback) {
    this.api.setSetCellStyleWidthRatiosCallback(callback);
  }

  setProtectedCellWriteAttemptedCallback(callback) {
    this.api.setProtectedCellWriteAttemptedCallback(callback);
  }

  setOnPasteCallback(callback) {
    this.api.setOnPasteCallback(callback);
  }

  setClearSelectedCellsOnCutCallback(callback) {
    this.api.setClearSelectedCellsOnCutCallback(callback);
  }

  setUpdateCellCommentCallback(callback) {
    this.api.setUpdateCellCommentCallback(callback);
  }

  setOnConnectorInitCallback(callback) {
    this.api.setOnConnectorInitCallback(callback);
  }

  setContextMenuOpenOnSelectionCallback(callback) {
    this.api.setContextMenuOpenOnSelectionCallback(callback);
  }

  setActionOnCurrentSelectionCallback(callback) {
    this.api.setActionOnCurrentSelectionCallback(callback);
  }

  setRowHeaderContextMenuOpenCallback(callback) {
    this.api.setRowHeaderContextMenuOpenCallback(callback);
  }

  setActionOnRowHeaderCallback(callback) {
    this.api.setActionOnRowHeaderCallback(callback);
  }

  setColumnHeaderContextMenuOpenCallback(callback) {
    this.api.setColumnHeaderContextMenuOpenCallback(callback);
  }

  setActionOnColumnHeaderCallback(callback) {
    this.api.setActionOnColumnHeaderCallback(callback);
  }

}

window.customElements.define('vaadin-spreadsheet', VaadinSpreadsheet);
