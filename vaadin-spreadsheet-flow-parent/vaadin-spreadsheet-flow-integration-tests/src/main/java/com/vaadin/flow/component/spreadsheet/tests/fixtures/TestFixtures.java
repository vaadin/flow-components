/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

/**
 * Test fixtures for server-side Spreadsheet manipulation
 *
 */
public enum TestFixtures {
    FirstColumnWidth(FirstColumnWidthFixture.class),
    PopupButton(PopupButtonFixture.class),
    TablePopupButton(TablePopupButtonFixture.class),
    SpreadsheetTable(SpreadsheetTableFixture.class),
    Comments(CommentFixture.class),
    AddOrRemoveComment(AddOrRemoveCommentFixture.class),
    Formats(FormatsFixture.class),
    DisableChartOverlays(DisableChartsFixture.class),
    StyleMergeReigions(StyleMergeReigions.class),
    RemoveFixture(RemoveFixture.class),
    DefaultStyleUnlocked(DefaultStyleUnlockedFixture.class),
    HideSecondRow(HideSecondRowFixture.class),
    HideSecondColumn(HideSecondColumnFixture.class),
    LargeSpreadsheet(LargeSpreadsheetFixture.class),
    ColumnToggle(ColumnToggleFixture.class),
    RowToggle(RowToggleFixture.class),
    DeletionHandler(DeletionHandlerFixture.class),
    Selection(SelectionFixture.class),
    MergeCells(CellMergeFixture.class),
    ValueChangeHandler(ValueHandlerFixture.class),
    Rename(RenameFixture.class),
    CreateSheet(SheetsFixture.class),
    CustomEditor(SimpleCustomEditorFixture.class),
    CustomEditorRow(CustomEditorSharedFixture.class),
    CustomEditorShared(CustomEditorSharedFixture.class),
    Styles(StylesFixture.class),
    LockCell(LockCellFixture.class),
    CustomComponent(CustomComponentFixture.class),
    Action(ActionFixture.class),
    InsertRow(InsertRowFixture.class),
    DeleteRow(DeleteRowFixture.class),
    RowHeaderDoubleClick(RowHeaderDoubleClickFixture.class);

    public final SpreadsheetFixtureFactory factory;

    TestFixtures(SpreadsheetFixtureFactory factory) {
        this.factory = factory;
    }

    TestFixtures(Class<? extends SpreadsheetFixture> fixtureClass) {
        this(new ClassFixtureFactory(fixtureClass));
    }
}
