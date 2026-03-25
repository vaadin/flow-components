/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.FooterRow.FooterCell;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

class HeaderFooterTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private static final Predicate<Element> isColumn = element -> "vaadin-grid-column"
            .equals(element.getTag());
    private static final Predicate<Element> isColumnGroup = element -> "vaadin-grid-column-group"
            .equals(element.getTag());

    Grid<String> grid;
    Column<String> firstColumn;
    Column<String> secondColumn;
    Column<String> thirdColumn;

    @BeforeEach
    void setup() {
        grid = new Grid<>();
        addColumns();
    }

    private void addColumns() {
        firstColumn = grid.addColumn(str -> str);
        secondColumn = grid.addColumn(str -> str);
        thirdColumn = grid.addColumn(str -> str);
    }

    @Test
    void initGrid_noHeaderFooterRows() {
        Assertions.assertEquals(0, grid.getHeaderRows().size(),
                "Grid should not have header rows initially");
        Assertions.assertEquals(0, grid.getFooterRows().size(),
                "Grid should not have footer rows initially");
    }

    @Test
    void initGrid_noColumnGroups() {
        List<List<Element>> layers = getColumnLayers();
        Assertions.assertTrue(layers.size() == 1,
                "Grid should not have column groups initially");
    }

    @Test
    void setHeader_firstHeaderRowCreated() {
        firstColumn.setHeader("foo");
        Assertions.assertEquals(1, grid.getHeaderRows().size(),
                "There should be one HeaderRow after setting a header for a column");
        assertRowWrapsLayer(grid.getHeaderRows().get(0),
                getColumnLayersAndAssertCount(1).get(0));
    }

    @Test
    void setHeaderText() {
        firstColumn.setHeader("foo");

        Assertions.assertEquals("foo", firstColumn.getHeaderText());
        Assertions.assertEquals("foo",
                grid.getHeaderRows().get(0).getCell(firstColumn).getText());
    }

    @Test
    void setHeaderComponent() {
        TextField textField = new TextField();
        firstColumn.setHeader(textField);

        // Getter should return component
        Assertions.assertEquals(textField, firstColumn.getHeaderComponent());
        Assertions.assertEquals(textField, grid.getHeaderRows().get(0)
                .getCell(firstColumn).getComponent());
        // Should be added as virtual child
        assertIsVirtualChild(textField, firstColumn);
    }

    @Test
    void setHeaderText_clearsHeaderComponent() {
        TextField textField = new TextField();

        firstColumn.setHeader(textField);
        firstColumn.setHeader("foo");
        Assertions.assertNull(firstColumn.getHeaderComponent());

        firstColumn.setHeader(textField);
        firstColumn.setHeader((String) null);
        Assertions.assertNull(firstColumn.getHeaderComponent());
        // Component should be removed
        assertIsNotVirtualChild(textField);
    }

    @Test
    void setHeaderComponent_clearsHeaderText() {
        TextField textField = new TextField();

        firstColumn.setHeader("foo");
        firstColumn.setHeader(textField);
        Assertions.assertNull(firstColumn.getHeaderText());

        firstColumn.setHeader("foo");
        firstColumn.setHeader((Component) null);
        Assertions.assertNull(firstColumn.getHeaderText());
    }

    @Test
    void replaceHeaderComponent_replacesVirtualChild() {
        TextField firstField = new TextField();
        firstColumn.setHeader(firstField);

        TextField secondField = new TextField();
        firstColumn.setHeader(secondField);

        assertIsNotVirtualChild(firstField);
        assertIsVirtualChild(secondField, firstColumn);
    }

    @Test
    void moveHeaderContent() {
        // Move text
        firstColumn.setHeader("Header");
        firstColumn.moveHeaderContent(secondColumn);

        Assertions.assertNull(firstColumn.getHeaderText());
        Assertions.assertEquals("Header", secondColumn.getHeaderText());

        // Move component
        TextField firstField = new TextField();
        firstColumn.setHeader(firstField);
        firstColumn.moveHeaderContent(secondColumn);

        Assertions.assertNull(firstColumn.getHeaderComponent());
        Assertions.assertNull(secondColumn.getHeaderText());
        Assertions.assertEquals(firstField, secondColumn.getHeaderComponent());
        assertIsVirtualChild(firstField, secondColumn);

        // Replace component
        TextField secondField = new TextField();
        firstColumn.setHeader(secondField);
        firstColumn.moveHeaderContent(secondColumn);

        assertIsNotVirtualChild(firstField);
        assertIsVirtualChild(secondField, secondColumn);

        // Overwrite component with text
        firstColumn.setHeader("Header");
        firstColumn.moveHeaderContent(secondColumn);

        Assertions.assertNull(secondColumn.getHeaderComponent());
        assertIsNotVirtualChild(secondField);
    }

    @Test
    void setFooter_firstFooterRowCreated() {
        firstColumn.setFooter("foo");
        Assertions.assertEquals(1, grid.getFooterRows().size(),
                "There should be one FooterRow after setting a footer for a column");
        assertRowWrapsLayer(grid.getFooterRows().get(0),
                getColumnLayersAndAssertCount(1).get(0));
    }

    @Test
    void setFooterText() {
        firstColumn.setFooter("foo");

        Assertions.assertEquals("foo", firstColumn.getFooterText());
        Assertions.assertEquals("foo",
                grid.getFooterRows().get(0).getCell(firstColumn).getText());
    }

    @Test
    void setFooterComponent() {
        TextField textField = new TextField();
        firstColumn.setFooter(textField);

        // Getter should return component
        Assertions.assertEquals(textField, firstColumn.getFooterComponent());
        Assertions.assertEquals(textField, grid.getFooterRows().get(0)
                .getCell(firstColumn).getComponent());
        // Should be added as virtual child
        assertIsVirtualChild(textField, firstColumn);
    }

    @Test
    void setFooterText_clearsFooterComponent() {
        TextField textField = new TextField();

        firstColumn.setFooter(textField);
        firstColumn.setFooter("foo");
        Assertions.assertNull(firstColumn.getFooterComponent());

        firstColumn.setFooter(textField);
        firstColumn.setFooter((String) null);
        Assertions.assertNull(firstColumn.getFooterComponent());
        assertIsNotVirtualChild(textField);
    }

    @Test
    void setFooterComponent_clearsFooterText() {
        TextField textField = new TextField();

        firstColumn.setFooter("foo");
        firstColumn.setFooter(textField);
        Assertions.assertNull(firstColumn.getFooterText());

        firstColumn.setFooter("foo");
        firstColumn.setFooter((Component) null);
        Assertions.assertNull(firstColumn.getFooterText());
    }

    @Test
    void replaceFooterComponent_replacesVirtualChild() {
        TextField firstField = new TextField();
        firstColumn.setFooter(firstField);

        TextField secondField = new TextField();
        firstColumn.setFooter(secondField);

        assertIsNotVirtualChild(firstField);
        assertIsVirtualChild(secondField, firstColumn);
    }

    @Test
    void moveFooterContent() {
        // Move text
        firstColumn.setFooter("Footer");
        firstColumn.moveFooterContent(secondColumn);

        Assertions.assertNull(firstColumn.getFooterText());
        Assertions.assertEquals("Footer", secondColumn.getFooterText());

        // Move component
        TextField firstField = new TextField();
        firstColumn.setFooter(firstField);
        firstColumn.moveFooterContent(secondColumn);

        Assertions.assertNull(firstColumn.getFooterComponent());
        Assertions.assertNull(secondColumn.getFooterText());
        Assertions.assertEquals(firstField, secondColumn.getFooterComponent());
        assertIsVirtualChild(firstField, secondColumn);

        // Replace component
        TextField secondField = new TextField();
        firstColumn.setFooter(secondField);
        firstColumn.moveFooterContent(secondColumn);

        assertIsNotVirtualChild(firstField);
        assertIsVirtualChild(secondField, secondColumn);

        // Overwrite component with text
        firstColumn.setFooter("Header");
        firstColumn.moveFooterContent(secondColumn);

        Assertions.assertNull(secondColumn.getFooterComponent());
        assertIsNotVirtualChild(secondField);
    }

    @Test
    void appendHeaderRows_firstOnTop() {
        HeaderRow first = grid.appendHeaderRow();
        HeaderRow second = grid.appendHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(1));
        assertRowWrapsLayer(second, layers.get(0));
    }

    @Test
    void prependHeaderRows_firstOnBottom() {
        HeaderRow first = grid.prependHeaderRow();
        HeaderRow second = grid.prependHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(0));
        assertRowWrapsLayer(second, layers.get(1));
    }

    @Test
    void appendFooterRows_firstOnTop() {
        FooterRow first = grid.appendFooterRow();
        FooterRow second = grid.appendFooterRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(0));
        assertRowWrapsLayer(second, layers.get(1));
    }

    @Test
    void prependFooterRows_firstOnBottom() {
        FooterRow first = grid.prependFooterRow();
        FooterRow second = grid.prependFooterRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(1));
        assertRowWrapsLayer(second, layers.get(0));
    }

    @Test
    void addHeaderRows_addFooterRows_footersOnLowerLayer() {
        HeaderRow h1 = grid.prependHeaderRow();
        HeaderRow h2 = grid.prependHeaderRow();

        FooterRow f1 = grid.appendFooterRow();
        FooterRow f2 = grid.appendFooterRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);

        // bottom layer is used by both headers and footers
        assertRowWrapsLayer(h1, layers.get(0));
        assertRowWrapsLayer(f1, layers.get(0));

        assertRowWrapsLayer(f2, layers.get(1));
        assertRowWrapsLayer(h2, layers.get(2));
    }

    @Test
    void addFooterRows_addHeaderRows_headersOnLowerLayer() {
        FooterRow f1 = grid.appendFooterRow();
        FooterRow f2 = grid.appendFooterRow();

        HeaderRow h1 = grid.prependHeaderRow();
        HeaderRow h2 = grid.prependHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);

        // bottom layer is used by both headers and footers
        assertRowWrapsLayer(h1, layers.get(0));
        assertRowWrapsLayer(f1, layers.get(0));

        assertRowWrapsLayer(h2, layers.get(1));
        assertRowWrapsLayer(f2, layers.get(2));
    }

    @Test
    void addHeaderAndFooterRows_addColumns_rowsUpdatedToWrapCorrectElements() {
        grid = new Grid<>();

        HeaderRow h1 = grid.prependHeaderRow();
        HeaderRow h2 = grid.prependHeaderRow();

        FooterRow f1 = grid.appendFooterRow();
        FooterRow f2 = grid.appendFooterRow();

        addColumns();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);

        // bottom layer is used by both headers and footers
        assertRowWrapsLayer(h1, layers.get(0));
        assertRowWrapsLayer(f1, layers.get(0));

        assertRowWrapsLayer(f2, layers.get(1));
        assertRowWrapsLayer(h2, layers.get(2));
    }

    @Test
    void joinTwoFirstHeaderCells() {
        HeaderRow bottom = grid.prependHeaderRow();
        HeaderRow top = grid.prependHeaderRow();
        HeaderCell lastCell = top.getCells().get(2);
        HeaderCell joined = top.join(firstColumn, secondColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(bottom, layers.get(0));
        assertRowWrapsLayer(top, layers.get(1));

        Assertions.assertEquals(2, top.getCells().size(),
                "HeaderRow should have two cells after joining two of three cells");
        Assertions.assertEquals(joined, top.getCells().get(0),
                "The joined cell should be the first cell of the row after joining two first cells");
        Assertions.assertEquals(lastCell, top.getCells().get(1),
                "The last cell should not be affected after joining two first cells");
    }

    @Test
    void joinTwoLastHeaderCells() {
        HeaderRow bottom = grid.prependHeaderRow();
        HeaderRow top = grid.prependHeaderRow();
        HeaderCell firstCell = top.getCells().get(0);
        HeaderCell joined = top.join(secondColumn, thirdColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(bottom, layers.get(0));
        assertRowWrapsLayer(top, layers.get(1));

        Assertions.assertEquals(2, top.getCells().size(),
                "HeaderRow should have two cells after joining two of three cells");
        Assertions.assertEquals(joined, top.getCells().get(1),
                "The joined cell should be the last cell of the row after joining two last cells");
        Assertions.assertEquals(firstCell, top.getCells().get(0),
                "The first cell should not be affected after joining two last cells");
    }

    @Test
    void joinTwoFirstFooterCells() {
        FooterRow bottom = grid.prependFooterRow();
        FooterRow top = grid.prependFooterRow();
        FooterCell lastCell = bottom.getCells().get(2);
        FooterCell joined = bottom.join(firstColumn, secondColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(top, layers.get(0));
        assertRowWrapsLayer(bottom, layers.get(1));

        Assertions.assertEquals(2, bottom.getCells().size(),
                "FooterRow should have two cells after joining two of three cells");
        Assertions.assertEquals(joined, bottom.getCells().get(0),
                "The joined cell should be the first cell of the row after joining two first cells");
        Assertions.assertEquals(lastCell, bottom.getCells().get(1),
                "The last cell should not be affected after joining two first cells");
    }

    @Test
    void joinTwoLastFooterCells() {
        FooterRow bottom = grid.prependFooterRow();
        FooterRow top = grid.prependFooterRow();
        FooterCell firstCell = bottom.getCells().get(0);
        FooterCell joined = bottom.join(secondColumn, thirdColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(top, layers.get(0));
        assertRowWrapsLayer(bottom, layers.get(1));

        Assertions.assertEquals(2, bottom.getCells().size(),
                "FooterRow should have two cells after joining two of three cells");
        Assertions.assertEquals(joined, bottom.getCells().get(1),
                "The joined cell should be the last cell of the row after joining two last cells");
        Assertions.assertEquals(firstCell, bottom.getCells().get(0),
                "The first cell should not be affected after joining two last cells");
    }

    @Test
    void joinTwoFirstHeaderCellsOnLowerLayer_layerMovedToTop() {
        grid.appendFooterRow();
        grid.prependHeaderRow();

        FooterRow footer = grid.appendFooterRow();
        HeaderRow header = grid.prependHeaderRow();

        HeaderCell lastCell = header.getCells().get(2);

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(header, layers.get(1), 3);
        assertRowWrapsLayer(footer, layers.get(2), 3);

        header.join(firstColumn, secondColumn);

        layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(footer, layers.get(1), 3);
        assertRowWrapsLayer(header, layers.get(2), 2);
        Assertions.assertEquals(lastCell, header.getCells().get(1),
                "The last cell should not be affected after joining two first cells");
    }

    @Test
    void joinTwoLastHeaderCellsOnLowerLayer_layerMovedToTop() {
        grid.appendFooterRow();
        grid.prependHeaderRow();

        FooterRow footer = grid.appendFooterRow();
        HeaderRow header = grid.prependHeaderRow();

        HeaderCell firstCell = header.getCells().get(0);

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(header, layers.get(1), 3);
        assertRowWrapsLayer(footer, layers.get(2), 3);

        header.join(secondColumn, thirdColumn);

        layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(footer, layers.get(1), 3);
        assertRowWrapsLayer(header, layers.get(2), 2);
        Assertions.assertEquals(firstCell, header.getCells().get(0),
                "The first cell should not be affected after joining two last cells");
    }

    @Test
    void joinAllHeaderCellsOnLowerLayer_layerMovedToTop() {
        grid.appendFooterRow();
        grid.prependHeaderRow();

        FooterRow footer = grid.appendFooterRow();
        HeaderRow header = grid.prependHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(header, layers.get(1), 3);
        assertRowWrapsLayer(footer, layers.get(2), 3);

        header.join(firstColumn, secondColumn, thirdColumn);

        layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(footer, layers.get(1), 3);
        assertRowWrapsLayer(header, layers.get(2), 1);
    }

    @Test
    void joinAllFooterCellsOnLowerLayer_layerMovedToTop() {
        grid.appendFooterRow();
        grid.prependHeaderRow();

        HeaderRow header = grid.prependHeaderRow();
        FooterRow footer = grid.appendFooterRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(footer, layers.get(1), 3);
        assertRowWrapsLayer(header, layers.get(2), 3);

        footer.join(firstColumn, secondColumn, thirdColumn);

        layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(header, layers.get(1), 3);
        assertRowWrapsLayer(footer, layers.get(2), 1);
    }

    @Test
    void joinFooters_joinHeadersForSameColumns_headersNotMoved() {
        grid.appendFooterRow();
        grid.prependHeaderRow();

        HeaderRow header = grid.prependHeaderRow();
        FooterRow footer = grid.appendFooterRow();

        footer.join(firstColumn, secondColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(header, layers.get(1), 3);
        assertRowWrapsLayer(footer, layers.get(2), 2);

        header.join(firstColumn, secondColumn);

        layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(header, layers.get(1), 2);
        assertRowWrapsLayer(footer, layers.get(2), 2);
    }

    @Test
    void joinFooters_joinHeadersForMoreColumns_headersMovedToTop() {
        grid.appendFooterRow();
        grid.prependHeaderRow();

        HeaderRow header = grid.prependHeaderRow();
        FooterRow footer = grid.appendFooterRow();

        footer.join(firstColumn, secondColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(header, layers.get(1), 3);
        assertRowWrapsLayer(footer, layers.get(2), 2);

        header.join(firstColumn, secondColumn, thirdColumn);

        layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(footer, layers.get(1), 2);
        assertRowWrapsLayer(header, layers.get(2), 1);
    }

    @Test
    void joinFooters_joinHeadersForConflictingColumns_throws() {
        grid.appendFooterRow();
        grid.prependHeaderRow();

        HeaderRow header = grid.prependHeaderRow();
        FooterRow footer = grid.appendFooterRow();

        footer.join(firstColumn, secondColumn);

        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> header.join(secondColumn, thirdColumn));
        Assertions.assertTrue(
                ex.getMessage().contains("cells can not be joined"));
    }

    @Test
    void joinHeaders_prependRow_newRowHasJoinedCellAlso() {
        grid.prependHeaderRow();
        HeaderRow header = grid.prependHeaderRow();
        header.join(firstColumn, secondColumn);

        HeaderRow topHeader = grid.prependHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(header, layers.get(1), 2);
        assertRowWrapsLayer(topHeader, layers.get(2), 2);

        List<Column<?>> bottomChildColumns = topHeader.getCells().get(0)
                .getColumn().getBottomChildColumns();
        Assertions.assertEquals(2, bottomChildColumns.size(),
                "The cell prepended on top of a joined cell should be "
                        + "a parent for the same column elements");
        Assertions.assertTrue(bottomChildColumns.contains(firstColumn),
                "The child columns should contain firstColumn");
        Assertions.assertTrue(bottomChildColumns.contains(secondColumn),
                "The child columns should contain secondColumn");
    }

    @Test
    void addHeaderRow_joinHeaderCells_addFooterRow_joinFooterCells_repeat() {
        FooterRow footer0 = grid.appendFooterRow();
        HeaderRow header0 = grid.prependHeaderRow();

        HeaderRow header1 = grid.prependHeaderRow();
        header1.join(firstColumn, secondColumn);

        FooterRow footer1 = grid.appendFooterRow();
        footer1.join(firstColumn, secondColumn);

        HeaderRow header2 = grid.prependHeaderRow();
        header2.join(firstColumn, thirdColumn);

        FooterRow footer2 = grid.appendFooterRow();
        footer2.join(firstColumn, thirdColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(5);
        assertRowWrapsLayer(header0, layers.get(0), 3);
        assertRowWrapsLayer(footer0, layers.get(0), 3);

        assertRowWrapsLayer(footer1, layers.get(1), 2);
        assertRowWrapsLayer(header1, layers.get(2), 2);

        assertRowWrapsLayer(footer2, layers.get(3), 1);
        assertRowWrapsLayer(header2, layers.get(4), 1);
    }

    @Test
    void joinNonAdjacentHeaderCells_throws() {
        grid.prependHeaderRow();
        HeaderRow top = grid.prependHeaderRow();
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> top.join(firstColumn, thirdColumn));
        Assertions.assertTrue(ex.getMessage().contains("not adjacent"));
    }

    @Test
    void getHeaderRows_orderFromTopToBottom() {
        HeaderRow row1 = grid.prependHeaderRow();
        assertHeaderRowOrder(row1);

        HeaderRow row2 = grid.prependHeaderRow();
        assertHeaderRowOrder(row2, row1);

        HeaderRow row3 = grid.appendHeaderRow();
        assertHeaderRowOrder(row2, row1, row3);

        grid.appendFooterRow();
        grid.prependFooterRow();

        assertHeaderRowOrder(row2, row1, row3);
    }

    @Test
    void getCellByColumn_returnsCorrectCell() {
        AbstractRow<?> row = grid.appendFooterRow();
        assertGettingCellsByColumns(row);
        row = grid.prependFooterRow();
        assertGettingCellsByColumns(row);

        row = grid.appendHeaderRow();
        assertGettingCellsByColumns(row);
        row = grid.prependHeaderRow();
        assertGettingCellsByColumns(row);
    }

    private void assertGettingCellsByColumns(AbstractRow<?> row) {
        IntStream.range(0, grid.getColumns().size()).forEach(i -> {
            Assertions.assertSame(row.getCells().get(i),
                    row.getCell(grid.getColumns().get(i)),
                    "getCell(Column) returned unexpected cell");
        });
    }

    @Test
    void getJoinedCellByColumn_worksWithEachChildColumn() {
        grid.prependHeaderRow();
        HeaderRow topRow = grid.prependHeaderRow();
        HeaderCell joined = topRow.join(secondColumn, thirdColumn);

        Assertions.assertSame(joined, topRow.getCell(secondColumn),
                "Joined header cell was not found by its child column");
        Assertions.assertSame(joined, topRow.getCell(thirdColumn),
                "Joined header cell was not found by its child column");
        Assertions.assertSame(topRow.getCells().get(0),
                topRow.getCell(firstColumn),
                "getCell(Column) returned unexpected cell after joining other cells");
    }

    @Test
    void getCellByColumnNotBelongingToGrid_throws() {
        HeaderRow row = grid.prependHeaderRow();
        Column<?> mockColumn = new Column<>(new Grid<String>(), "",
                LitRenderer.of(""));

        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> row.getCell(mockColumn));
        Assertions.assertTrue(ex.getMessage().contains("Cannot find a cell"));
    }

    @Test
    void getFooterRows_orderFromTopToBottom() {
        FooterRow row1 = grid.prependFooterRow();
        assertFooterRowOrder(row1);

        FooterRow row2 = grid.prependFooterRow();
        assertFooterRowOrder(row2, row1);

        FooterRow row3 = grid.appendFooterRow();
        assertFooterRowOrder(row2, row1, row3);

        grid.appendHeaderRow();
        grid.prependHeaderRow();

        assertFooterRowOrder(row2, row1, row3);
    }

    @Test
    void addHeadersAndFooters_removeColumn_cellsAreRemoved() {
        HeaderRow header = grid.prependHeaderRow();
        FooterRow footer = grid.appendFooterRow();
        grid.removeColumn(secondColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(1);
        assertRowWrapsLayer(header, layers.get(0), 2);
        assertRowWrapsLayer(footer, layers.get(0), 2);

        grid.removeColumn(thirdColumn);
        layers = getColumnLayersAndAssertCount(1);
        assertRowWrapsLayer(header, layers.get(0), 1);
        assertRowWrapsLayer(footer, layers.get(0), 1);
    }

    @Test
    void addHeader_joinCells_removeColumn_joinedCellRemains() {
        HeaderRow bottomHeader = grid.prependHeaderRow();
        HeaderRow topHeader = grid.prependHeaderRow();

        HeaderCell joinedCell = topHeader.join(firstColumn, secondColumn);
        Assertions.assertEquals(2, topHeader.getCells().size(),
                "Top row should have two cells after joining two of three");

        grid.removeColumn(secondColumn);
        Assertions.assertEquals(2, topHeader.getCells().size(),
                "The joined header cell should remain when only one of the child columns is removed");

        Assertions.assertSame(joinedCell, topHeader.getCell(firstColumn),
                "The joined cell should still be linked to the remaining child column, "
                        + "after removing the other child column");

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(bottomHeader, layers.get(0));
        assertRowWrapsLayer(topHeader, layers.get(1));
    }

    // https://github.com/vaadin/vaadin-grid/issues/1928
    @Test
    void joinHeaders_joinAlreadyJoinedCells_shouldNotThrowException() {
        grid.appendHeaderRow();
        grid.prependHeaderRow().join(firstColumn, secondColumn);
        grid.prependHeaderRow().join(firstColumn, secondColumn, thirdColumn);
    }

    // https://github.com/vaadin/vaadin-grid/issues/1928#issuecomment-659545963
    @Test
    void joinHeaders_joinAllCells_shouldNotThrowException() {
        grid.appendHeaderRow();
        HeaderRow header = grid.prependHeaderRow();
        header.join(header.getCells());
    }

    @Test
    void gridHasPrependedHeaderRow_columnHasTextAlignment_prependedColumnHasSameTextAlignment() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);
        var headerRow = grid.prependHeaderRow();

        Assertions.assertEquals(ColumnTextAlign.CENTER,
                headerRow.getCell(firstColumn).getColumn().getTextAlign());
    }

    @Test
    void gridHasAppendedHeaderRow_columnHasTextAlignment_appendedColumnHasSameTextAlignment() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);
        grid.appendHeaderRow();

        var parentGroup = firstColumn.getParent().map(ColumnGroup.class::cast)
                .orElse(null);

        Assertions.assertNotNull(parentGroup);
        Assertions.assertEquals(ColumnTextAlign.CENTER,
                parentGroup.getTextAlign());
    }

    @Test
    void columnsWithTextAlign_gridHeaderWithJoinedColumns_textAlignShouldNotPropagate() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);
        secondColumn.setHeader("").setTextAlign(ColumnTextAlign.END);

        var headerRow = grid.prependHeaderRow();
        var joinedHeader = headerRow.join(firstColumn, secondColumn);

        Assertions.assertNotEquals(ColumnTextAlign.CENTER,
                joinedHeader.getColumn().getTextAlign());
    }

    @Test
    void columnWithTextAlign_headerRowsAdded_textAlignPropagated() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);

        var firstHeaderRow = grid.prependHeaderRow();
        var secondHeaderRow = grid.prependHeaderRow();

        Assertions.assertEquals(ColumnTextAlign.CENTER,
                firstHeaderRow.getCell(firstColumn).getColumn().getTextAlign());
        Assertions.assertEquals(ColumnTextAlign.CENTER, secondHeaderRow
                .getCell(firstColumn).getColumn().getTextAlign());
    }

    @Test
    void gridWithHeaderRows_newTextAlignSetOnColumn_headersUpdated() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);

        var prependHeaderRow = grid.prependHeaderRow();
        prependHeaderRow.getCell(firstColumn).setText("PREPEND ROW");
        var appendHeaderRow = grid.appendHeaderRow();
        appendHeaderRow.getCell(firstColumn).setText("APPEND ROW");

        var prependParentColumnGroup = prependHeaderRow.getCell(firstColumn)
                .getColumn();
        var appendParentColumnGroup = firstColumn.getParent()
                .map(ColumnGroup.class::cast).orElse(null);

        Assertions.assertNotNull(appendParentColumnGroup);
        Assertions.assertEquals(ColumnTextAlign.CENTER,
                prependParentColumnGroup.getTextAlign());
        Assertions.assertEquals(ColumnTextAlign.CENTER,
                appendParentColumnGroup.getTextAlign());

        firstColumn.setTextAlign(ColumnTextAlign.END);
        Assertions.assertEquals(ColumnTextAlign.END,
                prependParentColumnGroup.getTextAlign());
        Assertions.assertEquals(ColumnTextAlign.END,
                appendParentColumnGroup.getTextAlign());
    }

    @Test
    void gridWithJoinedHeaders_textAlignSetToChildColumn_textAlignShouldNotPropagate() {
        firstColumn.setHeader("");
        secondColumn.setHeader("");
        var headerRow = grid.prependHeaderRow();

        var joinedCell = headerRow.join(firstColumn, secondColumn);
        firstColumn.setTextAlign(ColumnTextAlign.CENTER);
        Assertions.assertNotEquals(ColumnTextAlign.CENTER,
                joinedCell.getColumn().getTextAlign());
    }

    @Test
    void gridHasPrependFooterRow_columnHasTextAlignment_prependedColumnHasSameTextAlignment() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);
        var footerRow = grid.prependFooterRow();

        var parentGroup = firstColumn.getParent().map(ColumnGroup.class::cast)
                .orElse(null);
        Assertions.assertNotNull(parentGroup);

        Assertions.assertEquals(ColumnTextAlign.CENTER,
                parentGroup.getTextAlign());
    }

    @Test
    void gridHasAppendFooterRow_columnHasTextAlignment_appendedColumnHasSameTextAlignment() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);
        var footerRow = grid.appendFooterRow();

        Assertions.assertEquals(ColumnTextAlign.CENTER,
                footerRow.getCell(firstColumn).getColumn().getTextAlign());
    }

    @Test
    void columnsWithTextAlign_gridFooterWithJoinedColumns_textAlignShouldNotPropagate() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);
        secondColumn.setFooter("").setTextAlign(ColumnTextAlign.END);
        var footer = grid.appendFooterRow();
        var joinedFooter = footer.join(firstColumn, secondColumn);

        Assertions.assertNotEquals(ColumnTextAlign.CENTER,
                joinedFooter.getColumn().getTextAlign());
    }

    @Test
    void columnWithTextAlign_footerRowsAdded_textAlignPropagated() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);

        grid.prependFooterRow();
        grid.prependFooterRow();

        var firstParentGroup = firstColumn.getParent()
                .map(ColumnGroup.class::cast).orElse(null);
        Assertions.assertNotNull(firstParentGroup);
        var secondParentGroup = firstParentGroup.getParent()
                .map(ColumnGroup.class::cast).orElse(null);
        Assertions.assertNotNull(secondParentGroup);

        Assertions.assertEquals(ColumnTextAlign.CENTER,
                firstParentGroup.getTextAlign());
        Assertions.assertEquals(ColumnTextAlign.CENTER,
                secondParentGroup.getTextAlign());
    }

    @Test
    void gridWithFooterRows_newTextAlignSetOnColumn_headersUpdated() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);

        var prependFooterRow = grid.prependFooterRow();
        prependFooterRow.getCell(firstColumn).setText("PREPEND ROW");
        var appendFooterRow = grid.appendFooterRow();
        appendFooterRow.getCell(firstColumn).setText("APPEND ROW");

        var appendFooterColumnGroup = appendFooterRow.getCell(firstColumn)
                .getColumn();
        var prependFooterColumnGroup = firstColumn.getParent()
                .map(ColumnGroup.class::cast).orElse(null);
        Assertions.assertNotNull(prependFooterColumnGroup);

        Assertions.assertEquals(ColumnTextAlign.CENTER,
                prependFooterColumnGroup.getTextAlign());
        Assertions.assertEquals(ColumnTextAlign.CENTER,
                appendFooterColumnGroup.getTextAlign());

        firstColumn.setTextAlign(ColumnTextAlign.END);
        Assertions.assertEquals(ColumnTextAlign.END,
                prependFooterColumnGroup.getTextAlign());
        Assertions.assertEquals(ColumnTextAlign.END,
                appendFooterColumnGroup.getTextAlign());
    }

    @Test
    void gridWithJoinedFooters_textAlignSetToChildColumn_textAlignShouldNotPropagate() {
        firstColumn.setFooter("");
        secondColumn.setFooter("");
        var footerRow = grid.appendFooterRow();

        var joinedCell = footerRow.join(firstColumn, secondColumn);
        firstColumn.setTextAlign(ColumnTextAlign.CENTER);
        Assertions.assertNotEquals(ColumnTextAlign.CENTER,
                joinedCell.getColumn().getTextAlign());
    }

    @Test
    void columnHasNoHeaderPartName() {
        Assertions.assertEquals(null, firstColumn.getHeaderPartName());
        Assertions.assertEquals(null,
                firstColumn.getElement().getProperty("headerPartName"));
    }

    @Test
    void setHeaderPartName_columnHasHeaderPartName() {
        firstColumn.setHeaderPartName("foo");
        Assertions.assertEquals("foo", firstColumn.getHeaderPartName());
        Assertions.assertEquals("foo",
                firstColumn.getElement().getProperty("headerPartName"));
    }

    @Test
    void columnHasNoFooterPartName() {
        Assertions.assertEquals(null, firstColumn.getFooterPartName());
        Assertions.assertEquals(null,
                firstColumn.getElement().getProperty("footerPartName"));
    }

    @Test
    void setFooterPartName_columnHasFooterPartName() {
        firstColumn.setFooterPartName("foo");
        Assertions.assertEquals("foo", firstColumn.getFooterPartName());
        Assertions.assertEquals("foo",
                firstColumn.getElement().getProperty("footerPartName"));
    }

    @Test
    void setHeaderPartName_setFooterPartName_isChainable() {
        firstColumn.setHeaderPartName("foo").setFrozen(true);
        firstColumn.setFooterPartName("foo").setFrozen(true);
    }

    @Test
    void columnGroupHasNoHeaderPartName() {
        grid.appendHeaderRow();
        var headerCell = grid.prependHeaderRow().join(firstColumn,
                secondColumn);
        Assertions.assertEquals(null, headerCell.getPartName());
        Assertions.assertEquals(null, headerCell.getColumn().getElement()
                .getProperty("headerPartName"));
    }

    @Test
    void setHeaderPartName_columnGroupHasHeaderPartName() {
        grid.appendHeaderRow();
        var headerCell = grid.prependHeaderRow().join(firstColumn,
                secondColumn);
        headerCell.setPartName("foo");
        Assertions.assertEquals("foo", headerCell.getPartName());
        Assertions.assertEquals("foo", headerCell.getColumn().getElement()
                .getProperty("headerPartName"));
    }

    @Test
    void columnGroupHasNoFooterPartName() {
        grid.appendFooterRow();
        var footerCell = grid.appendFooterRow().join(firstColumn, secondColumn);
        Assertions.assertEquals(null, footerCell.getPartName());
        Assertions.assertEquals(null, footerCell.getColumn().getElement()
                .getProperty("footerPartName"));
    }

    @Test
    void addHeaderRow_removeHeaderRow_headerRemoved() {
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.removeHeaderRow(headerRow);
        Assertions.assertEquals(0, grid.getHeaderRows().size());
    }

    @Test
    void addFooterRow_removeFooterRow_footerRemoved() {
        FooterRow footerRow = grid.appendFooterRow();
        grid.removeFooterRow(footerRow);
        Assertions.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    void addHeaderRow_removeHeaderRow_removeSameRow_throwsNoSuchElementException() {
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.removeHeaderRow(headerRow);
        Assertions.assertThrows(NoSuchElementException.class,
                () -> grid.removeHeaderRow(headerRow));
    }

    @Test
    void addFooterRow_removeFooterRow_removeSameRow_throwsNoSuchElementException() {
        FooterRow footerRow = grid.appendFooterRow();
        grid.removeFooterRow(footerRow);
        Assertions.assertThrows(NoSuchElementException.class,
                () -> grid.removeFooterRow(footerRow));
    }

    @Test
    void addHeaderRow_prependAnotherHeaderRow_removeDefaultHeaderRow_throwsUnsupportedOperationException() {
        HeaderRow defaultHeaderRow = grid.appendHeaderRow();
        grid.prependHeaderRow();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> grid.removeHeaderRow(defaultHeaderRow));
    }

    @Test
    void addHeaderRow_setSortable_appendAnotherHeaderRow_removeLastHeaderRow_firstHeaderRowIsSortable() {
        HeaderRow defaultHeaderRow = grid.appendHeaderRow();
        grid.getColumns().forEach(col -> col.setSortable(true));
        HeaderRow newHeaderRow = grid.appendHeaderRow();
        grid.removeHeaderRow(newHeaderRow);
        defaultHeaderRow.layer.getColumns().forEach(
                col -> Assertions.assertTrue(col.hasSortingIndicators()));
    }

    @Test
    void addTextHeaderRow_appendAnotherTextHeaderRow_removeAppendedHeaderRow_correctRowIsRemoved() {
        String textContent = "DEFAULT";
        HeaderRow defaultHeaderRow = grid.appendHeaderRow();
        defaultHeaderRow.getCells().forEach(cell -> cell.setText(textContent));
        HeaderRow newHeaderRow = grid.appendHeaderRow();
        newHeaderRow.getCells().forEach(cell -> cell.setText("NEW"));
        grid.removeHeaderRow(newHeaderRow);
        assertRowTextContent(textContent, grid.getHeaderRows().get(0));
    }

    @Test
    void addTextHeaderRow_appendComponentHeaderRow_removeAppendedHeaderRow_correctRowIsRemoved() {
        String textContent = "DEFAULT";
        grid.appendHeaderRow().getCells()
                .forEach(cell -> cell.setText(textContent));
        HeaderRow newHeaderRow = grid.appendHeaderRow();
        newHeaderRow.getCells()
                .forEach(cell -> cell.setComponent(new Span("NEW")));
        grid.removeHeaderRow(newHeaderRow);
        assertRowTextContent(textContent, grid.getHeaderRows().get(0));
    }

    @Test
    void addComponentHeaderRow_appendAnotherComponentHeaderRow_removeAppendedHeaderRow_correctRowIsRemoved() {
        HeaderRow defaultHeaderRow = grid.appendHeaderRow();
        List<? extends Component> defaultHeaderRowComponents = setComponentsToRow(
                defaultHeaderRow);
        HeaderRow newHeaderRow = grid.appendHeaderRow();
        newHeaderRow.getCells()
                .forEach(cell -> cell.setComponent(new Span("NEW")));
        grid.removeHeaderRow(newHeaderRow);
        assertRowComponents(defaultHeaderRowComponents,
                grid.getHeaderRows().get(0));

    }

    @Test
    void addComponentHeaderRow_appendTextHeaderRow_removeAppendedHeaderRow_correctRowIsRemoved() {
        HeaderRow defaultHeaderRow = grid.appendHeaderRow();
        List<? extends Component> defaultHeaderRowComponents = setComponentsToRow(
                defaultHeaderRow);
        HeaderRow newHeaderRow = grid.appendHeaderRow();
        newHeaderRow.getCells().forEach(cell -> cell.setText("NEW"));
        grid.removeHeaderRow(newHeaderRow);
        assertRowComponents(defaultHeaderRowComponents,
                grid.getHeaderRows().get(0));
    }

    @Test
    void addTextFooterRow_prependAnotherTextFooterRow_removePrependedFooterRow_correctRowIsRemoved() {
        String textContent = "DEFAULT";
        grid.appendFooterRow().getCells()
                .forEach(cell -> cell.setText(textContent));
        FooterRow newFooterRow = grid.prependFooterRow();
        newFooterRow.getCells().forEach(cell -> cell.setText("NEW"));
        grid.removeFooterRow(newFooterRow);
        assertRowTextContent(textContent, grid.getFooterRows().get(0));
    }

    @Test
    void addTextFooterRow_prependComponentFooterRow_removePrependedFooterRow_correctRowIsRemoved() {
        String textContent = "DEFAULT";
        grid.appendFooterRow().getCells()
                .forEach(cell -> cell.setText(textContent));
        FooterRow newFooterRow = grid.prependFooterRow();
        newFooterRow.getCells()
                .forEach(cell -> cell.setComponent(new Span("NEW")));
        grid.removeFooterRow(newFooterRow);
        assertRowTextContent(textContent, grid.getFooterRows().get(0));
    }

    @Test
    void addComponentFooterRow_prependAnotherComponentFooterRow_removePrependedFooterRow_correctRowIsRemoved() {
        FooterRow footerRow = grid.appendFooterRow();
        List<? extends Component> footerRowComponents = setComponentsToRow(
                footerRow);
        FooterRow newFooterRow = grid.prependFooterRow();
        newFooterRow.getCells()
                .forEach(cell -> cell.setComponent(new Span("NEW")));
        grid.removeFooterRow(newFooterRow);
        assertRowComponents(footerRowComponents, grid.getFooterRows().get(0));
    }

    @Test
    void addComponentFooterRow_prependTextFooterRow_removePrependedFooterRow_correctRowIsRemoved() {
        FooterRow footerRow = grid.appendFooterRow();
        List<? extends Component> footerRowComponents = setComponentsToRow(
                footerRow);
        FooterRow newFooterRow = grid.prependFooterRow();
        newFooterRow.getCells().forEach(cell -> cell.setText("NEW"));
        grid.removeFooterRow(newFooterRow);
        assertRowComponents(footerRowComponents, grid.getFooterRows().get(0));
    }

    @Test
    void addHeaderRowsAlternatingPrependAndAppend_removeEachRowExceptDefault_rowsRemoved() {
        grid.appendHeaderRow();
        List<HeaderRow> headerRows = addMixedHeaderRows(6);
        headerRows.forEach(row -> grid.removeHeaderRow(row));
        Assertions.assertEquals(1, grid.getHeaderRows().size());
    }

    @Test
    void addFooterRowsAlternatingPrependAndAppend_removeEachRow_rowsRemoved() {
        List<FooterRow> footerRows = addMixedFooterRows(6);
        footerRows.forEach(row -> grid.removeFooterRow(row));
        Assertions.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    void addHeaderRowsAlternatingPrependAndAppend_removeEachRowInReverseOrder_rowsRemoved() {
        List<HeaderRow> headerRows = addMixedHeaderRows(6);
        Collections.reverse(headerRows);
        headerRows.forEach(row -> grid.removeHeaderRow(row));
        Assertions.assertEquals(0, grid.getHeaderRows().size());
    }

    @Test
    void addFooterRowsAlternatingPrependAndAppend_removeEachRowInReverseOrder_rowsRemoved() {
        List<FooterRow> footerRows = addMixedFooterRows(6);
        Collections.reverse(footerRows);
        footerRows.forEach(row -> grid.removeFooterRow(row));
        Assertions.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    void addHeaderRowsAlternatingPrependAndAppend_removeAllHeaderRows_rowsRemoved() {
        addMixedHeaderRows(6);
        grid.removeAllHeaderRows();
        Assertions.assertEquals(0, grid.getHeaderRows().size());
    }

    @Test
    void addFooterRowsAlternatingPrependAndAppend_removeAllFooterRows_rowsRemoved() {
        addMixedFooterRows(6);
        grid.removeAllFooterRows();
        Assertions.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    void addHeaderAndFooterRows_removeAllFooterRows_onlyFooterRowsRemoved() {
        List<HeaderRow> headerRows = addMixedHeaderRows(2);
        addMixedFooterRows(2);
        grid.removeAllFooterRows();
        Assertions.assertEquals(headerRows.size(), grid.getHeaderRows().size());
        Assertions.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    void addHeaderAndFooterRows_removeAllHeaderRows_onlyHeaderRowsRemoved() {
        List<FooterRow> footerRows = addMixedFooterRows(2);
        addMixedHeaderRows(2);
        grid.removeAllHeaderRows();
        Assertions.assertEquals(footerRows.size(), grid.getFooterRows().size());
        Assertions.assertEquals(0, grid.getHeaderRows().size());
    }

    @Test
    void addHeaderAndFooterRowsInMixedOrder_removeAllHeaderAndFooters_headersAndFootersRemoved() {
        List<HeaderRow> headerRows = addMixedHeaderRows(2);
        List<FooterRow> footerRows = addMixedFooterRows(2);
        headerRows.addAll(addMixedHeaderRows(2));
        footerRows.addAll(addMixedFooterRows(2));
        grid.removeAllHeaderRows();
        grid.removeAllFooterRows();
        Assertions.assertEquals(0, grid.getHeaderRows().size());
        Assertions.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    void addHeaderRow_removeHeaderRow_addHeaderRow_headerAdded() {
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.removeHeaderRow(headerRow);
        grid.appendHeaderRow();
        Assertions.assertEquals(1, grid.getHeaderRows().size());
    }

    @Test
    void addFooterRow_removeFooterRow_addFooterRow_footerAdded() {
        FooterRow footerRow = grid.appendFooterRow();
        grid.removeFooterRow(footerRow);
        grid.appendFooterRow();
        Assertions.assertEquals(1, grid.getFooterRows().size());
    }

    @Test
    void addFooterRow_appendFooterRowWithJoinedCells_removeFirstFooterRow_throwsUnsupportedOperationException() {
        var first = grid.appendFooterRow();
        var second = grid.appendFooterRow();
        var columns = grid.getColumns();
        second.join(columns.get(0), columns.get(1));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> grid.removeFooterRow(first));
    }

    @Test
    void addHeaderRow_removeHeaderRow_setCellValueForRemovedRow_throwsIllegalArgumentException() {
        var row = grid.appendHeaderRow();
        grid.removeHeaderRow(row);
        assertCannotSetValueOnRemovedRow(row);
    }

    @Test
    void addHeaderRow_prependAnotherHeaderRow_removeSecondHeaderRow_setCellValueForRemovedRow_throwsIllegalArgumentException() {
        grid.appendHeaderRow();
        var second = grid.prependHeaderRow();
        grid.removeHeaderRow(second);
        assertCannotSetValueOnRemovedRow(second);
    }

    @Test
    void addHeaderRow_appendAnotherHeaderRow_removeSecondHeaderRow_setCellValueForRemovedRow_throwsIllegalArgumentException() {
        grid.appendHeaderRow();
        var second = grid.appendHeaderRow();
        grid.removeHeaderRow(second);
        assertCannotSetValueOnRemovedRow(second);
    }

    @Test
    void addFooterRow_removeFooterRow_setCellValueForRemovedRow_throwsIllegalArgumentException() {
        var row = grid.appendFooterRow();
        grid.removeFooterRow(row);
        assertCannotSetValueOnRemovedRow(row);
    }

    @Test
    void addFooterRow_prependAnotherFooterRow_removeFirstFooterRow_setCellValueForRemovedRow_throwsIllegalArgumentException() {
        var first = grid.appendFooterRow();
        grid.prependFooterRow();
        grid.removeFooterRow(first);
        assertCannotSetValueOnRemovedRow(first);
    }

    @Test
    void addFooterRow_prependAnotherFooterRow_removeSecondFooterRow_setCellValueForRemovedRow_throwsIllegalArgumentException() {
        grid.appendFooterRow();
        var second = grid.prependFooterRow();
        grid.removeFooterRow(second);
        assertCannotSetValueOnRemovedRow(second);
    }

    @Test
    void addFooterRow_appendAnotherFooterRow_removeFirstFooterRow_setCellValueForRemovedRow_throwsIllegalArgumentException() {
        var first = grid.appendFooterRow();
        grid.appendFooterRow();
        grid.removeFooterRow(first);
        assertCannotSetValueOnRemovedRow(first);
    }

    @Test
    void addFooterRow_appendAnotherFooterRow_removeSecondFooterRow_setCellValueForRemovedRow_throwsIllegalArgumentException() {
        grid.appendFooterRow();
        var second = grid.appendFooterRow();
        grid.removeFooterRow(second);
        assertCannotSetValueOnRemovedRow(second);
    }

    @Test
    void setFooterPartName_columnGroupHasFooterPartName() {
        grid.appendFooterRow();
        var footerCell = grid.appendFooterRow().join(firstColumn, secondColumn);
        footerCell.setPartName("foo");
        Assertions.assertEquals("foo", footerCell.getPartName());
        Assertions.assertEquals("foo", footerCell.getColumn().getElement()
                .getProperty("footerPartName"));
    }

    private void assertHeaderRowOrder(HeaderRow... rows) {
        Assertions.assertEquals(rows.length, grid.getHeaderRows().size(),
                "Grid returned unexpected amount of header rows");
        IntStream.range(0, rows.length).forEach(i -> {
            Assertions.assertSame(rows[i], grid.getHeaderRows().get(i),
                    "Grid did not return expected header rows in order from top to bottom");
        });
    }

    private void assertFooterRowOrder(FooterRow... rows) {
        Assertions.assertEquals(rows.length, grid.getFooterRows().size(),
                "Grid returned unexpected amount of footer rows");
        IntStream.range(0, rows.length).forEach(i -> {
            Assertions.assertSame(rows[i], grid.getFooterRows().get(i),
                    "Grid did not return expected footer rows in order from top to bottom");
        });
    }

    private void assertRowWrapsLayer(AbstractRow<?> row, List<Element> layer,
            int expectedCellCount) {
        Assertions.assertEquals(expectedCellCount, row.getCells().size(),
                "The row contains unexpected amount of cells");
        assertRowWrapsLayer(row, layer);
    }

    private void assertRowWrapsLayer(AbstractRow<?> row, List<Element> layer) {
        List<Element> cellWrappedElements = row.getCells().stream()
                .map(cell -> cell.getColumn().getElement())
                .collect(Collectors.toList());

        Assertions.assertEquals(layer.size(), cellWrappedElements.size(),
                "The row contains unexpected amount of column elements");

        IntStream.range(0, layer.size()).forEach(i -> {
            Assertions.assertEquals(layer.get(i), cellWrappedElements.get(i),
                    "The row is not referring to expected column elements");
        });
    }

    private List<List<Element>> getColumnLayersAndAssertCount(
            int expectedAmountOfLayers) {
        List<List<Element>> layers = getColumnLayers();
        assertLayerCount(layers, expectedAmountOfLayers);
        return layers;
    }

    private void assertLayerCount(List<List<Element>> layers,
            int expectedCount) {
        Assertions.assertEquals(expectedCount, layers.size(),
                "Unexpected amount of column layers");
    }

    /**
     * Gets all the layers of column-elements in the column-hierarchy of the
     * Grid. The order is from in-most to out-most. So the first layer consists
     * of the vaadin-grid-column elements, and the second layer consists of
     * vaadin-grid-column-group elements that are their parents, and so on.
     *
     * @see ColumnLayer
     */
    private List<List<Element>> getColumnLayers() {
        List<List<Element>> layers = new ArrayList<List<Element>>();
        List<Element> children = grid.getElement().getChildren()
                .collect(Collectors.toList());
        while (children.stream().anyMatch(isColumnGroup)) {
            if (!children.stream().allMatch(isColumnGroup)) {
                throw new IllegalStateException(
                        "All column-children on the same hierarchy level "
                                + "should be either vaadin-grid-columns or "
                                + "vaadin-grid-column-groups. "
                                + "All of the tags on this layer are:\n"
                                + children.stream().map(Element::getTag)
                                        .reduce("", (a, b) -> (a + " " + b)));
            }
            layers.add(children);
            children = children.stream()
                    .flatMap(element -> element.getChildren())
                    .collect(Collectors.toList());
        }
        if (children.stream().anyMatch(isColumn)) {
            if (!children.stream().allMatch(isColumn)) {
                throw new IllegalStateException(
                        "All column-children on the same hierarchy level "
                                + "should be either vaadin-grid-columns or "
                                + "vaadin-grid-column-groups. "
                                + "All of the tags on this layer are:\n"
                                + children.stream().map(Element::getTag)
                                        .reduce("", (a, b) -> (a + " " + b)));
            }
            layers.add(children);
        } else if (layers.size() > 0) {
            throw new IllegalStateException(
                    "If there are vaadin-grid-column-groups, there should "
                            + "also be vaadin-grid-columns inside them");
        }
        // reverse to have the same order as in the implementation code:
        // from inner-most to out-most
        Collections.reverse(layers);
        return layers;
    }

    private void assertIsVirtualChild(Component child,
            Component expectedParent) {
        Assertions.assertTrue(child.getParent().isPresent());
        Assertions.assertSame(child.getParent().get(), expectedParent);
        Assertions.assertTrue(child.getElement().isVirtualChild());
    }

    private void assertIsNotVirtualChild(Component component) {
        Assertions.assertFalse(component.getParent().isPresent());
        Assertions.assertFalse(component.getElement().isVirtualChild());
    }

    private List<FooterRow> addMixedFooterRows(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> index % 2 == 0 ? grid.appendFooterRow()
                        : grid.prependFooterRow())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<HeaderRow> addMixedHeaderRows(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> index % 2 == 0 ? grid.appendHeaderRow()
                        : grid.prependHeaderRow())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void assertRowTextContent(String expectedTextContent,
            AbstractRow<? extends AbstractRow.AbstractCell> row) {
        List<? extends AbstractRow.AbstractCell> cells = row.getCells();
        Assertions.assertEquals(grid.getColumns().size(), cells.size());
        cells.forEach(cell -> Assertions.assertEquals(expectedTextContent,
                cell.getText()));
    }

    private void assertRowComponents(
            List<? extends Component> expectedRowComponents,
            AbstractRow<? extends AbstractRow.AbstractCell> row) {
        List<? extends AbstractRow.AbstractCell> cells = row.getCells();
        Assertions.assertEquals(grid.getColumns().size(), cells.size());
        for (int i = 0; i < expectedRowComponents.size(); i++) {
            Assertions.assertEquals(expectedRowComponents.get(i),
                    cells.get(i).getComponent());
        }
    }

    private List<? extends Component> setComponentsToRow(
            AbstractRow<? extends AbstractRow.AbstractCell> row) {
        List<Span> rowComponents = IntStream.range(0, grid.getColumns().size())
                .mapToObj(Integer::toString).map(Span::new).toList();
        for (int i = 0; i < rowComponents.size(); i++) {
            row.getCells().get(i).setComponent(rowComponents.get(i));
        }
        return rowComponents;
    }

    private void assertCannotSetValueOnRemovedRow(
            AbstractRow<? extends AbstractRow.AbstractCell> row) {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> row.getCell(grid.getColumns().get(0)).setText("TEXT"));
    }
}
