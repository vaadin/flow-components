/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * Smoke tests for the skeleton `Spreadsheet` class. These don't
 * exercise rendering behavior (that's Phase 3.2+); they pin the
 * contract that the class is constructable + every setter accepts its
 * documented type + the read accessors return what was set.
 *
 * The real value is COMPILE-TIME: every test below requires the class
 * to satisfy `SpreadsheetClientApi`. If a method is renamed / removed
 * / wrong-typed, this file fails to compile, surfacing the
 * contract drift immediately.
 */

describe('Spreadsheet skeleton — constructor + identity', () => {
    it('is constructable with a host element', () => {
        const host = document.createElement('div');
        const sheet = new Spreadsheet(host);

        expect(sheet).to.be.instanceOf(Spreadsheet);
    });

    it('sets host width + height via setters', () => {
        const host = document.createElement('div');
        const sheet = new Spreadsheet(host);

        sheet.setWidth('500px');
        sheet.setHeight('400px');

        expect(host.style.width).to.equal('500px');
        expect(host.style.height).to.equal('400px');
    });
});

describe('Spreadsheet skeleton — property mirrors', () => {
    let sheet: Spreadsheet;

    beforeEach(() => {
        sheet = new Spreadsheet(document.createElement('div'));
    });

    it('stores + returns rows + cols', () => {
        sheet.setRows(100);
        sheet.setCols(20);

        expect(sheet.getRows()).to.equal(100);
        expect(sheet.getCols()).to.equal(20);
    });

    it('stores + returns sheet navigation state', () => {
        sheet.setSheetIndex(2);
        sheet.setSheetNames(['Sheet1', 'Sheet2', 'Sheet3']);

        expect(sheet.getSheetIndex()).to.equal(2);
        expect(sheet.getSheetNames()).to.deep.equal([
            'Sheet1',
            'Sheet2',
            'Sheet3',
        ]);
    });

    it('stores + returns merged regions', () => {
        const regions = [
            { id: 1, col1: 1, col2: 3, row1: 1, row2: 1 },
            { id: 2, col1: 5, col2: 7, row1: 2, row2: 4 },
        ];

        sheet.setMergedRegions(regions);

        expect(sheet.getMergedRegions()).to.deep.equal(regions);
    });

    it('stores + returns sheet-protected state', () => {
        expect(sheet.isSheetProtected()).to.be.false;

        sheet.setSheetProtected(true);

        expect(sheet.isSheetProtected()).to.be.true;
    });
});

describe('Spreadsheet skeleton — RPC stubs are callable without throwing', () => {
    let sheet: Spreadsheet;

    beforeEach(() => {
        sheet = new Spreadsheet(document.createElement('div'));
    });

    it('the 12 explicit RPC methods are all callable', () => {
        expect(() => sheet.updateBottomRightCellValues([])).to.not.throw();
        expect(() => sheet.updateTopLeftCellValues([])).to.not.throw();
        expect(() => sheet.updateTopRightCellValues([])).to.not.throw();
        expect(() => sheet.updateBottomLeftCellValues([])).to.not.throw();
        expect(() => sheet.updateFormulaBar('Sheet1!A1', 1, 1)).to.not.throw();
        expect(() => sheet.invalidCellAddress()).to.not.throw();
        expect(() =>
            sheet.showSelectedCell('A1', 1, 1, 'val', false, false, true)
        ).to.not.throw();
        expect(() => sheet.showActions([])).to.not.throw();
        expect(() =>
            sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false)
        ).to.not.throw();
        expect(() => sheet.cellsUpdated([])).to.not.throw();
        expect(() => sheet.refreshCellStyles()).to.not.throw();
        expect(() => sheet.editCellComment(1, 1)).to.not.throw();
    });

    it('lifecycle methods are callable', () => {
        expect(() => sheet.relayout()).to.not.throw();
        expect(() => sheet.resize()).to.not.throw();
        expect(() => sheet.notifyStateChanges([], true)).to.not.throw();
    });

    it('popup-button methods are callable', () => {
        expect(() =>
            sheet.onPopupButtonOpened(1, 1, 'content', 'app')
        ).to.not.throw();
        expect(() => sheet.closePopup(1, 1)).to.not.throw();
        expect(() => sheet.addPopupButton('{}')).to.not.throw();
        expect(() => sheet.removePopupButton('{}')).to.not.throw();
    });
});

describe('Spreadsheet skeleton — initial selection state', () => {
    it('starts with A1 (single-cell) selected', () => {
        const sheet = new Spreadsheet(document.createElement('div'));

        const sel = sheet.getSelection();

        expect(sel.activeCell).to.deep.equal({ col: 1, row: 1 });
        expect(sel.activeRange).to.deep.equal({ c1: 1, c2: 1, r1: 1, r2: 1 });
        expect(sel.auxRanges).to.deep.equal([]);
    });
});

describe('Spreadsheet skeleton — freeze + display state (slice 126)', () => {
    let sheet: Spreadsheet;

    beforeEach(() => {
        sheet = new Spreadsheet(document.createElement('div'));
    });

    it('freeze split positions are all callable + storable', () => {
        expect(() => sheet.setVerticalSplitPosition(3)).to.not.throw();
        expect(() => sheet.setHorizontalSplitPosition(2)).to.not.throw();
    });

    it('display flags are all callable', () => {
        expect(() => sheet.setDisplayGridlines(false)).to.not.throw();
        expect(() => sheet.setDisplayRowColHeadings(false)).to.not.throw();
        expect(() => sheet.setLockFormatColumns(true)).to.not.throw();
        expect(() => sheet.setLockFormatRows(true)).to.not.throw();
        expect(() => sheet.setHasActions(true)).to.not.throw();
        expect(() => sheet.setWorkbookChangeToggle(true)).to.not.throw();
        expect(() => sheet.setWorkbookProtected(true)).to.not.throw();
    });

    it('display content setters are all callable', () => {
        expect(() => sheet.setInfoLabelValue('Sheet1: A1')).to.not.throw();
        expect(() => sheet.setHyperlinksTooltips({ 'col1 row1': 'see http://...' }))
            .to.not.throw();
        expect(() => sheet.setId('spreadsheet-1')).to.not.throw();
        expect(() => sheet.setClass('spreadsheet themed')).to.not.throw();
    });
});

describe('Spreadsheet skeleton — callback registry (slice 126)', () => {
    let sheet: Spreadsheet;

    beforeEach(() => {
        sheet = new Spreadsheet(document.createElement('div'));
    });

    it('representative selection-event callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setCellSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setCellRangeSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setRowSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setColumnSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setSelectionIncreasePaintedCallback(noop)).to.not.throw();
        expect(() => sheet.setSelectionDecreasePaintedCallback(noop)).to.not.throw();
        expect(() =>
            sheet.setCellAddedToSelectionAndSelectedCallback(noop)
        ).to.not.throw();
        expect(() =>
            sheet.setCellsAddedToRangeSelectionCallback(noop)
        ).to.not.throw();
        expect(() =>
            sheet.setRowAddedToRangeSelectionCallback(noop)
        ).to.not.throw();
        expect(() =>
            sheet.setColumnAddedToSelectionCallback(noop)
        ).to.not.throw();
    });

    it('sheet-lifecycle + edit + scroll callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setSheetSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setSheetRenamedCallback(noop)).to.not.throw();
        expect(() => sheet.setSheetCreatedCallback(noop)).to.not.throw();
        expect(() => sheet.setCellValueEditedCallback(noop)).to.not.throw();
        expect(() => sheet.setOnSheetScrollCallback(noop)).to.not.throw();
        expect(() => sheet.setSheetAddressChangedCallback(noop)).to.not.throw();
        expect(() => sheet.setCellRangePaintedCallback(noop)).to.not.throw();
        expect(() => sheet.setDeleteSelectedCellsCallback(noop)).to.not.throw();
    });

    it('action + context-menu callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setContextMenuClosedCallback(noop)).to.not.throw();
        expect(() => sheet.setContextMenuOpenOnSelectionCallback(noop)).to.not.throw();
        expect(() => sheet.setRowHeaderContextMenuOpenCallback(noop)).to.not.throw();
        expect(() =>
            sheet.setColumnHeaderContextMenuOpenCallback(noop)
        ).to.not.throw();
        expect(() => sheet.setActionOnCurrentSelectionCallback(noop)).to.not.throw();
        expect(() => sheet.setActionOnRowHeaderCallback(noop)).to.not.throw();
        expect(() => sheet.setActionOnColumnHeaderCallback(noop)).to.not.throw();
    });

    it('resize + autofit + undo/redo + grouping callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setRowsResizedCallback(noop)).to.not.throw();
        expect(() => sheet.setColumnResizedCallback(noop)).to.not.throw();
        expect(() => sheet.setOnRowAutofitCallback(noop)).to.not.throw();
        expect(() => sheet.setOnColumnAutofitCallback(noop)).to.not.throw();
        expect(() => sheet.setOnUndoCallback(noop)).to.not.throw();
        expect(() => sheet.setOnRedoCallback(noop)).to.not.throw();
        expect(() => sheet.setGroupingCollapsedCallback(noop)).to.not.throw();
        expect(() => sheet.setLevelHeaderClickedCallback(noop)).to.not.throw();
    });

    it('cut/paste + protected-write + comment + connector callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setOnPasteCallback(noop)).to.not.throw();
        expect(() => sheet.setClearSelectedCellsOnCutCallback(noop)).to.not.throw();
        expect(() => sheet.setProtectedCellWriteAttemptedCallback(noop)).to.not.throw();
        expect(() => sheet.setUpdateCellCommentCallback(noop)).to.not.throw();
        expect(() => sheet.setOnConnectorInitCallback(noop)).to.not.throw();
        expect(() => sheet.setLinkCellClickedCallback(noop)).to.not.throw();
        expect(() => sheet.setSetCellStyleWidthRatiosCallback(noop)).to.not.throw();
        expect(() => sheet.setPopupButtonClickCallback(noop)).to.not.throw();
        expect(() => sheet.setPopupCloseCallback(noop)).to.not.throw();
    });
});

describe('Spreadsheet skeleton — legacy spreadsheetWidget escape hatch (slice 126)', () => {
    it('exposes spreadsheetWidget as undefined (not throw on read)', () => {
        const sheet = new Spreadsheet(document.createElement('div'));
        // The GWT-era wrapper reads `api.spreadsheetWidget.sheetWidget.
        // definedRowHeights` to time first render. The new impl returns
        // undefined; the wrapper's existence check handles this safely.
        expect(sheet.spreadsheetWidget).to.be.undefined;
    });
});
