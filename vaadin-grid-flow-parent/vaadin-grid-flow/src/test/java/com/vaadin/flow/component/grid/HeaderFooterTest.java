/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LitRenderer;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.FooterRow.FooterCell;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.dom.Element;

public class HeaderFooterTest {

    private static final Predicate<Element> isColumn = element -> "vaadin-grid-column"
            .equals(element.getTag());
    private static final Predicate<Element> isColumnGroup = element -> "vaadin-grid-column-group"
            .equals(element.getTag());

    Grid<String> grid;
    Column<String> firstColumn;
    Column<String> secondColumn;
    Column<String> thirdColumn;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        grid = new Grid<>();
        addColumns();

        UI.setCurrent(new UI());
    }

    private void addColumns() {
        firstColumn = grid.addColumn(str -> str);
        secondColumn = grid.addColumn(str -> str);
        thirdColumn = grid.addColumn(str -> str);
    }

    @Test
    public void initGrid_noHeaderFooterRows() {
        Assert.assertEquals("Grid should not have header rows initially", 0,
                grid.getHeaderRows().size());
        Assert.assertEquals("Grid should not have footer rows initially", 0,
                grid.getFooterRows().size());
    }

    @Test
    public void initGrid_noColumnGroups() {
        List<List<Element>> layers = getColumnLayers();
        Assert.assertTrue("Grid should not have column groups initially",
                layers.size() == 1);
    }

    @Test
    public void setHeader_firstHeaderRowCreated() {
        firstColumn.setHeader("foo");
        Assert.assertEquals(
                "There should be one HeaderRow after setting a header for a column",
                1, grid.getHeaderRows().size());
        assertRowWrapsLayer(grid.getHeaderRows().get(0),
                getColumnLayersAndAssertCount(1).get(0));
    }

    @Test
    public void setHeaderText() {
        firstColumn.setHeader("foo");

        Assert.assertEquals("foo", firstColumn.getHeaderText());
        Assert.assertEquals("foo",
                grid.getHeaderRows().get(0).getCell(firstColumn).getText());
    }

    @Test
    public void setHeaderComponent() {
        TextField textField = new TextField();
        firstColumn.setHeader(textField);

        // Getter should return component
        Assert.assertEquals(textField, firstColumn.getHeaderComponent());
        Assert.assertEquals(textField, grid.getHeaderRows().get(0)
                .getCell(firstColumn).getComponent());
        // Should be added as virtual child
        assertIsVirtualChild(textField, firstColumn);
    }

    @Test
    public void setHeaderText_clearsHeaderComponent() {
        TextField textField = new TextField();

        firstColumn.setHeader(textField);
        firstColumn.setHeader("foo");
        Assert.assertNull(firstColumn.getHeaderComponent());

        firstColumn.setHeader(textField);
        firstColumn.setHeader((String) null);
        Assert.assertNull(firstColumn.getHeaderComponent());
        // Component should be removed
        assertIsNotVirtualChild(textField);
    }

    @Test
    public void setHeaderComponent_clearsHeaderText() {
        TextField textField = new TextField();

        firstColumn.setHeader("foo");
        firstColumn.setHeader(textField);
        Assert.assertNull(firstColumn.getHeaderText());

        firstColumn.setHeader("foo");
        firstColumn.setHeader((Component) null);
        Assert.assertNull(firstColumn.getHeaderText());
    }

    @Test
    public void replaceHeaderComponent_replacesVirtualChild() {
        TextField firstField = new TextField();
        firstColumn.setHeader(firstField);

        TextField secondField = new TextField();
        firstColumn.setHeader(secondField);

        assertIsNotVirtualChild(firstField);
        assertIsVirtualChild(secondField, firstColumn);
    }

    @Test
    public void moveHeaderContent() {
        // Move text
        firstColumn.setHeader("Header");
        firstColumn.moveHeaderContent(secondColumn);

        Assert.assertNull(firstColumn.getHeaderText());
        Assert.assertEquals("Header", secondColumn.getHeaderText());

        // Move component
        TextField firstField = new TextField();
        firstColumn.setHeader(firstField);
        firstColumn.moveHeaderContent(secondColumn);

        Assert.assertNull(firstColumn.getHeaderComponent());
        Assert.assertNull(secondColumn.getHeaderText());
        Assert.assertEquals(firstField, secondColumn.getHeaderComponent());
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

        Assert.assertNull(secondColumn.getHeaderComponent());
        assertIsNotVirtualChild(secondField);
    }

    @Test
    public void setFooter_firstFooterRowCreated() {
        firstColumn.setFooter("foo");
        Assert.assertEquals(
                "There should be one FooterRow after setting a footer for a column",
                1, grid.getFooterRows().size());
        assertRowWrapsLayer(grid.getFooterRows().get(0),
                getColumnLayersAndAssertCount(1).get(0));
    }

    @Test
    public void setFooterText() {
        firstColumn.setFooter("foo");

        Assert.assertEquals("foo", firstColumn.getFooterText());
        Assert.assertEquals("foo",
                grid.getFooterRows().get(0).getCell(firstColumn).getText());
    }

    @Test
    public void setFooterComponent() {
        TextField textField = new TextField();
        firstColumn.setFooter(textField);

        // Getter should return component
        Assert.assertEquals(textField, firstColumn.getFooterComponent());
        Assert.assertEquals(textField, grid.getFooterRows().get(0)
                .getCell(firstColumn).getComponent());
        // Should be added as virtual child
        assertIsVirtualChild(textField, firstColumn);
    }

    @Test
    public void setFooterText_clearsFooterComponent() {
        TextField textField = new TextField();

        firstColumn.setFooter(textField);
        firstColumn.setFooter("foo");
        Assert.assertNull(firstColumn.getFooterComponent());

        firstColumn.setFooter(textField);
        firstColumn.setFooter((String) null);
        Assert.assertNull(firstColumn.getFooterComponent());
        assertIsNotVirtualChild(textField);
    }

    @Test
    public void setFooterComponent_clearsFooterText() {
        TextField textField = new TextField();

        firstColumn.setFooter("foo");
        firstColumn.setFooter(textField);
        Assert.assertNull(firstColumn.getFooterText());

        firstColumn.setFooter("foo");
        firstColumn.setFooter((Component) null);
        Assert.assertNull(firstColumn.getFooterText());
    }

    @Test
    public void replaceFooterComponent_replacesVirtualChild() {
        TextField firstField = new TextField();
        firstColumn.setFooter(firstField);

        TextField secondField = new TextField();
        firstColumn.setFooter(secondField);

        assertIsNotVirtualChild(firstField);
        assertIsVirtualChild(secondField, firstColumn);
    }

    @Test
    public void moveFooterContent() {
        // Move text
        firstColumn.setFooter("Footer");
        firstColumn.moveFooterContent(secondColumn);

        Assert.assertNull(firstColumn.getFooterText());
        Assert.assertEquals("Footer", secondColumn.getFooterText());

        // Move component
        TextField firstField = new TextField();
        firstColumn.setFooter(firstField);
        firstColumn.moveFooterContent(secondColumn);

        Assert.assertNull(firstColumn.getFooterComponent());
        Assert.assertNull(secondColumn.getFooterText());
        Assert.assertEquals(firstField, secondColumn.getFooterComponent());
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

        Assert.assertNull(secondColumn.getFooterComponent());
        assertIsNotVirtualChild(secondField);
    }

    @Test
    public void appendHeaderRows_firstOnTop() {
        HeaderRow first = grid.appendHeaderRow();
        HeaderRow second = grid.appendHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(1));
        assertRowWrapsLayer(second, layers.get(0));
    }

    @Test
    public void prependHeaderRows_firstOnBottom() {
        HeaderRow first = grid.prependHeaderRow();
        HeaderRow second = grid.prependHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(0));
        assertRowWrapsLayer(second, layers.get(1));
    }

    @Test
    public void appendFooterRows_firstOnTop() {
        FooterRow first = grid.appendFooterRow();
        FooterRow second = grid.appendFooterRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(0));
        assertRowWrapsLayer(second, layers.get(1));
    }

    @Test
    public void prependFooterRows_firstOnBottom() {
        FooterRow first = grid.prependFooterRow();
        FooterRow second = grid.prependFooterRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(1));
        assertRowWrapsLayer(second, layers.get(0));
    }

    @Test
    public void addHeaderRows_addFooterRows_footersOnLowerLayer() {
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
    public void addFooterRows_addHeaderRows_headersOnLowerLayer() {
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
    public void addHeaderAndFooterRows_addColumns_rowsUpdatedToWrapCorrectElements() {
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
    public void joinTwoFirstHeaderCells() {
        HeaderRow bottom = grid.prependHeaderRow();
        HeaderRow top = grid.prependHeaderRow();
        HeaderCell lastCell = top.getCells().get(2);
        HeaderCell joined = top.join(firstColumn, secondColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(bottom, layers.get(0));
        assertRowWrapsLayer(top, layers.get(1));

        Assert.assertEquals(
                "HeaderRow should have two cells after joining two of three cells",
                2, top.getCells().size());
        Assert.assertEquals(
                "The joined cell should be the first cell of the row after joining two first cells",
                joined, top.getCells().get(0));
        Assert.assertEquals(
                "The last cell should not be affected after joining two first cells",
                lastCell, top.getCells().get(1));
    }

    @Test
    public void joinTwoLastHeaderCells() {
        HeaderRow bottom = grid.prependHeaderRow();
        HeaderRow top = grid.prependHeaderRow();
        HeaderCell firstCell = top.getCells().get(0);
        HeaderCell joined = top.join(secondColumn, thirdColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(bottom, layers.get(0));
        assertRowWrapsLayer(top, layers.get(1));

        Assert.assertEquals(
                "HeaderRow should have two cells after joining two of three cells",
                2, top.getCells().size());
        Assert.assertEquals(
                "The joined cell should be the last cell of the row after joining two last cells",
                joined, top.getCells().get(1));
        Assert.assertEquals(
                "The first cell should not be affected after joining two last cells",
                firstCell, top.getCells().get(0));
    }

    @Test
    public void joinTwoFirstFooterCells() {
        FooterRow bottom = grid.prependFooterRow();
        FooterRow top = grid.prependFooterRow();
        FooterCell lastCell = bottom.getCells().get(2);
        FooterCell joined = bottom.join(firstColumn, secondColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(top, layers.get(0));
        assertRowWrapsLayer(bottom, layers.get(1));

        Assert.assertEquals(
                "FooterRow should have two cells after joining two of three cells",
                2, bottom.getCells().size());
        Assert.assertEquals(
                "The joined cell should be the first cell of the row after joining two first cells",
                joined, bottom.getCells().get(0));
        Assert.assertEquals(
                "The last cell should not be affected after joining two first cells",
                lastCell, bottom.getCells().get(1));
    }

    @Test
    public void joinTwoLastFooterCells() {
        FooterRow bottom = grid.prependFooterRow();
        FooterRow top = grid.prependFooterRow();
        FooterCell firstCell = bottom.getCells().get(0);
        FooterCell joined = bottom.join(secondColumn, thirdColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(top, layers.get(0));
        assertRowWrapsLayer(bottom, layers.get(1));

        Assert.assertEquals(
                "FooterRow should have two cells after joining two of three cells",
                2, bottom.getCells().size());
        Assert.assertEquals(
                "The joined cell should be the last cell of the row after joining two last cells",
                joined, bottom.getCells().get(1));
        Assert.assertEquals(
                "The first cell should not be affected after joining two last cells",
                firstCell, bottom.getCells().get(0));
    }

    @Test
    public void joinTwoFirstHeaderCellsOnLowerLayer_layerMovedToTop() {
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
        Assert.assertEquals(
                "The last cell should not be affected after joining two first cells",
                lastCell, header.getCells().get(1));
    }

    @Test
    public void joinTwoLastHeaderCellsOnLowerLayer_layerMovedToTop() {
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
        Assert.assertEquals(
                "The first cell should not be affected after joining two last cells",
                firstCell, header.getCells().get(0));
    }

    @Test
    public void joinAllHeaderCellsOnLowerLayer_layerMovedToTop() {
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
    public void joinAllFooterCellsOnLowerLayer_layerMovedToTop() {
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
    public void joinFooters_joinHeadersForSameColumns_headersNotMoved() {
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
    public void joinFooters_joinHeadersForMoreColumns_headersMovedToTop() {
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
    public void joinFooters_joinHeadersForConflictingColumns_throws() {
        grid.appendFooterRow();
        grid.prependHeaderRow();

        HeaderRow header = grid.prependHeaderRow();
        FooterRow footer = grid.appendFooterRow();

        footer.join(firstColumn, secondColumn);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("cells can not be joined");

        header.join(secondColumn, thirdColumn);
    }

    @Test
    public void joinHeaders_prependRow_newRowHasJoinedCellAlso() {
        grid.prependHeaderRow();
        HeaderRow header = grid.prependHeaderRow();
        header.join(firstColumn, secondColumn);

        HeaderRow topHeader = grid.prependHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);
        assertRowWrapsLayer(header, layers.get(1), 2);
        assertRowWrapsLayer(topHeader, layers.get(2), 2);

        List<Column<?>> bottomChildColumns = topHeader.getCells().get(0)
                .getColumn().getBottomChildColumns();
        Assert.assertEquals(
                "The cell prepended on top of a joined cell should be "
                        + "a parent for the same column elements",
                2, bottomChildColumns.size());
        Assert.assertThat(
                "The cell prepended on top of a joined cell should be "
                        + "a parent for the same column elements",
                bottomChildColumns,
                CoreMatchers.hasItems(firstColumn, secondColumn));
    }

    @Test
    public void addHeaderRow_joinHeaderCells_addFooterRow_joinFooterCells_repeat() {
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
    public void joinNonAdjacentHeaderCells_throws() {
        grid.prependHeaderRow();
        HeaderRow top = grid.prependHeaderRow();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("not adjacent");
        top.join(firstColumn, thirdColumn);
    }

    @Test
    public void getHeaderRows_orderFromTopToBottom() {
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
    public void getCellByColumn_returnsCorrectCell() {
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
            Assert.assertSame("getCell(Column) returned unexpected cell",
                    row.getCells().get(i),
                    row.getCell(grid.getColumns().get(i)));
        });
    }

    @Test
    public void getJoinedCellByColumn_worksWithEachChildColumn() {
        grid.prependHeaderRow();
        HeaderRow topRow = grid.prependHeaderRow();
        HeaderCell joined = topRow.join(secondColumn, thirdColumn);

        Assert.assertSame(
                "Joined header cell was not found by its child column", joined,
                topRow.getCell(secondColumn));
        Assert.assertSame(
                "Joined header cell was not found by its child column", joined,
                topRow.getCell(thirdColumn));
        Assert.assertSame(
                "getCell(Column) returned unexpected cell after joining other cells",
                topRow.getCells().get(0), topRow.getCell(firstColumn));
    }

    @Test
    public void getCellByColumnNotBelongingToGrid_throws() {
        HeaderRow row = grid.prependHeaderRow();
        Column<?> mockColumn = new Column<>(new Grid<String>(), "",
                LitRenderer.of(""));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot find a cell");
        row.getCell(mockColumn);
    }

    @Test
    public void getFooterRows_orderFromTopToBottom() {
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
    public void addHeadersAndFooters_removeColumn_cellsAreRemoved() {
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
    public void addHeader_joinCells_removeColumn_joinedCellRemains() {
        HeaderRow bottomHeader = grid.prependHeaderRow();
        HeaderRow topHeader = grid.prependHeaderRow();

        HeaderCell joinedCell = topHeader.join(firstColumn, secondColumn);
        Assert.assertEquals(
                "Top row should have two cells after joining two of three", 2,
                topHeader.getCells().size());

        grid.removeColumn(secondColumn);
        Assert.assertEquals(
                "The joined header cell should remain when only one of the child columns is removed",
                2, topHeader.getCells().size());

        Assert.assertSame(
                "The joined cell should still be linked to the remaining child column, "
                        + "after removing the other child column",
                joinedCell, topHeader.getCell(firstColumn));

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(bottomHeader, layers.get(0));
        assertRowWrapsLayer(topHeader, layers.get(1));
    }

    // https://github.com/vaadin/vaadin-grid/issues/1928
    @Test
    public void joinHeaders_joinAlreadyJoinedCells_shouldNotThrowException() {
        grid.appendHeaderRow();
        grid.prependHeaderRow().join(firstColumn, secondColumn);
        grid.prependHeaderRow().join(firstColumn, secondColumn, thirdColumn);
    }

    // https://github.com/vaadin/vaadin-grid/issues/1928#issuecomment-659545963
    @Test
    public void joinHeaders_joinAllCells_shouldNotThrowException() {
        grid.appendHeaderRow();
        HeaderRow header = grid.prependHeaderRow();
        header.join(header.getCells());
    }

    @Test
    public void gridHasPrependedHeaderRow_columnHasTextAlignment_prependedColumnHasSameTextAlignment() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);
        var headerRow = grid.prependHeaderRow();

        Assert.assertEquals(ColumnTextAlign.CENTER,
                headerRow.getCell(firstColumn).getColumn().getTextAlign());
    }

    @Test
    public void gridHasAppendedHeaderRow_columnHasTextAlignment_appendedColumnHasSameTextAlignment() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);
        grid.appendHeaderRow();

        var parentGroup = firstColumn.getParent().map(ColumnGroup.class::cast)
                .orElse(null);

        Assert.assertNotNull(parentGroup);
        Assert.assertEquals(ColumnTextAlign.CENTER, parentGroup.getTextAlign());
    }

    @Test
    public void columnsWithTextAlign_gridHeaderWithJoinedColumns_textAlignShouldNotPropagate() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);
        secondColumn.setHeader("").setTextAlign(ColumnTextAlign.END);

        var headerRow = grid.prependHeaderRow();
        var joinedHeader = headerRow.join(firstColumn, secondColumn);

        Assert.assertNotEquals(ColumnTextAlign.CENTER,
                joinedHeader.getColumn().getTextAlign());
    }

    @Test
    public void columnWithTextAlign_headerRowsAdded_textAlignPropagated() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);

        var firstHeaderRow = grid.prependHeaderRow();
        var secondHeaderRow = grid.prependHeaderRow();

        Assert.assertEquals(ColumnTextAlign.CENTER,
                firstHeaderRow.getCell(firstColumn).getColumn().getTextAlign());
        Assert.assertEquals(ColumnTextAlign.CENTER, secondHeaderRow
                .getCell(firstColumn).getColumn().getTextAlign());
    }

    @Test
    public void gridWithHeaderRows_newTextAlignSetOnColumn_headersUpdated() {
        firstColumn.setHeader("").setTextAlign(ColumnTextAlign.CENTER);

        var prependHeaderRow = grid.prependHeaderRow();
        prependHeaderRow.getCell(firstColumn).setText("PREPEND ROW");
        var appendHeaderRow = grid.appendHeaderRow();
        appendHeaderRow.getCell(firstColumn).setText("APPEND ROW");

        var prependParentColumnGroup = prependHeaderRow.getCell(firstColumn)
                .getColumn();
        var appendParentColumnGroup = firstColumn.getParent()
                .map(ColumnGroup.class::cast).orElse(null);

        Assert.assertNotNull(appendParentColumnGroup);
        Assert.assertEquals(ColumnTextAlign.CENTER,
                prependParentColumnGroup.getTextAlign());
        Assert.assertEquals(ColumnTextAlign.CENTER,
                appendParentColumnGroup.getTextAlign());

        firstColumn.setTextAlign(ColumnTextAlign.END);
        Assert.assertEquals(ColumnTextAlign.END,
                prependParentColumnGroup.getTextAlign());
        Assert.assertEquals(ColumnTextAlign.END,
                appendParentColumnGroup.getTextAlign());
    }

    @Test
    public void gridWithJoinedHeaders_textAlignSetToChildColumn_textAlignShouldNotPropagate() {
        firstColumn.setHeader("");
        secondColumn.setHeader("");
        var headerRow = grid.prependHeaderRow();

        var joinedCell = headerRow.join(firstColumn, secondColumn);
        firstColumn.setTextAlign(ColumnTextAlign.CENTER);
        Assert.assertNotEquals(ColumnTextAlign.CENTER,
                joinedCell.getColumn().getTextAlign());
    }

    @Test
    public void gridHasPrependFooterRow_columnHasTextAlignment_prependedColumnHasSameTextAlignment() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);
        var footerRow = grid.prependFooterRow();

        var parentGroup = firstColumn.getParent().map(ColumnGroup.class::cast)
                .orElse(null);
        Assert.assertNotNull(parentGroup);

        Assert.assertEquals(ColumnTextAlign.CENTER, parentGroup.getTextAlign());
    }

    @Test
    public void gridHasAppendFooterRow_columnHasTextAlignment_appendedColumnHasSameTextAlignment() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);
        var footerRow = grid.appendFooterRow();

        Assert.assertEquals(ColumnTextAlign.CENTER,
                footerRow.getCell(firstColumn).getColumn().getTextAlign());
    }

    @Test
    public void columnsWithTextAlign_gridFooterWithJoinedColumns_textAlignShouldNotPropagate() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);
        secondColumn.setFooter("").setTextAlign(ColumnTextAlign.END);
        var footer = grid.appendFooterRow();
        var joinedFooter = footer.join(firstColumn, secondColumn);

        Assert.assertNotEquals(ColumnTextAlign.CENTER,
                joinedFooter.getColumn().getTextAlign());
    }

    @Test
    public void columnWithTextAlign_footerRowsAdded_textAlignPropagated() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);

        grid.prependFooterRow();
        grid.prependFooterRow();

        var firstParentGroup = firstColumn.getParent()
                .map(ColumnGroup.class::cast).orElse(null);
        Assert.assertNotNull(firstParentGroup);
        var secondParentGroup = firstParentGroup.getParent()
                .map(ColumnGroup.class::cast).orElse(null);
        Assert.assertNotNull(secondParentGroup);

        Assert.assertEquals(ColumnTextAlign.CENTER,
                firstParentGroup.getTextAlign());
        Assert.assertEquals(ColumnTextAlign.CENTER,
                secondParentGroup.getTextAlign());
    }

    @Test
    public void gridWithFooterRows_newTextAlignSetOnColumn_headersUpdated() {
        firstColumn.setFooter("").setTextAlign(ColumnTextAlign.CENTER);

        var prependFooterRow = grid.prependFooterRow();
        prependFooterRow.getCell(firstColumn).setText("PREPEND ROW");
        var appendFooterRow = grid.appendFooterRow();
        appendFooterRow.getCell(firstColumn).setText("APPEND ROW");

        var appendFooterColumnGroup = appendFooterRow.getCell(firstColumn)
                .getColumn();
        var prependFooterColumnGroup = firstColumn.getParent()
                .map(ColumnGroup.class::cast).orElse(null);
        Assert.assertNotNull(prependFooterColumnGroup);

        Assert.assertEquals(ColumnTextAlign.CENTER,
                prependFooterColumnGroup.getTextAlign());
        Assert.assertEquals(ColumnTextAlign.CENTER,
                appendFooterColumnGroup.getTextAlign());

        firstColumn.setTextAlign(ColumnTextAlign.END);
        Assert.assertEquals(ColumnTextAlign.END,
                prependFooterColumnGroup.getTextAlign());
        Assert.assertEquals(ColumnTextAlign.END,
                appendFooterColumnGroup.getTextAlign());
    }

    @Test
    public void gridWithJoinedFooters_textAlignSetToChildColumn_textAlignShouldNotPropagate() {
        firstColumn.setFooter("");
        secondColumn.setFooter("");
        var footerRow = grid.appendFooterRow();

        var joinedCell = footerRow.join(firstColumn, secondColumn);
        firstColumn.setTextAlign(ColumnTextAlign.CENTER);
        Assert.assertNotEquals(ColumnTextAlign.CENTER,
                joinedCell.getColumn().getTextAlign());
    }

    @Test
    public void columnHasNoHeaderPartName() {
        Assert.assertEquals(null, firstColumn.getHeaderPartName());
        Assert.assertEquals(null,
                firstColumn.getElement().getProperty("headerPartName"));
    }

    @Test
    public void setHeaderPartName_columnHasHeaderPartName() {
        firstColumn.setHeaderPartName("foo");
        Assert.assertEquals("foo", firstColumn.getHeaderPartName());
        Assert.assertEquals("foo",
                firstColumn.getElement().getProperty("headerPartName"));
    }

    @Test
    public void columnHasNoFooterPartName() {
        Assert.assertEquals(null, firstColumn.getFooterPartName());
        Assert.assertEquals(null,
                firstColumn.getElement().getProperty("footerPartName"));
    }

    @Test
    public void setFooterPartName_columnHasFooterPartName() {
        firstColumn.setFooterPartName("foo");
        Assert.assertEquals("foo", firstColumn.getFooterPartName());
        Assert.assertEquals("foo",
                firstColumn.getElement().getProperty("footerPartName"));
    }

    @Test
    public void setHeaderPartName_setFooterPartName_isChainable() {
        firstColumn.setHeaderPartName("foo").setFrozen(true);
        firstColumn.setFooterPartName("foo").setFrozen(true);
    }

    @Test
    public void columnGroupHasNoHeaderPartName() {
        grid.appendHeaderRow();
        var headerCell = grid.prependHeaderRow().join(firstColumn,
                secondColumn);
        Assert.assertEquals(null, headerCell.getPartName());
        Assert.assertEquals(null, headerCell.getColumn().getElement()
                .getProperty("headerPartName"));
    }

    @Test
    public void setHeaderPartName_columnGroupHasHeaderPartName() {
        grid.appendHeaderRow();
        var headerCell = grid.prependHeaderRow().join(firstColumn,
                secondColumn);
        headerCell.setPartName("foo");
        Assert.assertEquals("foo", headerCell.getPartName());
        Assert.assertEquals("foo", headerCell.getColumn().getElement()
                .getProperty("headerPartName"));
    }

    @Test
    public void columnGroupHasNoFooterPartName() {
        grid.appendFooterRow();
        var footerCell = grid.appendFooterRow().join(firstColumn, secondColumn);
        Assert.assertEquals(null, footerCell.getPartName());
        Assert.assertEquals(null, footerCell.getColumn().getElement()
                .getProperty("footerPartName"));
    }

    @Test
    public void addHeaderRow_removeHeaderRow_headerRemoved() {
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.removeHeaderRow(headerRow);
        Assert.assertEquals(0, grid.getHeaderRows().size());
    }

    @Test
    public void addFooterRow_removeFooterRow_footerRemoved() {
        FooterRow footerRow = grid.appendFooterRow();
        grid.removeFooterRow(footerRow);
        Assert.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    public void addHeaderRow_removeHeaderRow_removeSameRow_throwsNoSuchElementException() {
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.removeHeaderRow(headerRow);
        Assert.assertThrows(NoSuchElementException.class,
                () -> grid.removeHeaderRow(headerRow));
    }

    @Test
    public void addFooterRow_removeFooterRow_removeSameRow_throwsNoSuchElementException() {
        FooterRow footerRow = grid.appendFooterRow();
        grid.removeFooterRow(footerRow);
        Assert.assertThrows(NoSuchElementException.class,
                () -> grid.removeFooterRow(footerRow));
    }

    @Test
    public void addHeaderRow_prependAnotherHeaderRow_removeDefaultHeaderRow_throwsUnsupportedOperationException() {
        HeaderRow defaultHeaderRow = grid.appendHeaderRow();
        grid.prependHeaderRow();
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> grid.removeHeaderRow(defaultHeaderRow));
    }

    @Test
    public void addTextHeaderRow_appendAnotherTextHeaderRow_removeAppendedHeaderRow_correctRowIsRemoved() {
        int columnCount = grid.getColumns().size();
        HeaderRow defaultHeaderRow = grid.appendHeaderRow();
        defaultHeaderRow.getCells().forEach(cell -> cell.setText("DEFAULT"));
        HeaderRow newHeaderRow = grid.appendHeaderRow();
        newHeaderRow.getCells().forEach(cell -> cell.setText("NEW"));
        grid.removeHeaderRow(newHeaderRow);
        List<HeaderCell> headerCells = grid.getHeaderRows().get(0).getCells();
        Assert.assertEquals(columnCount, headerCells.size());
        headerCells.forEach(
                cell -> Assert.assertEquals("DEFAULT", cell.getText()));
    }

    @Test
    public void addComponentHeaderRow_appendAnotherComponentHeaderRow_removeAppendedHeaderRow_correctRowIsRemoved() {
        int columnCount = grid.getColumns().size();
        HeaderRow defaultHeaderRow = grid.appendHeaderRow();
        List<NativeLabel> defaultHeaderRowComponents = IntStream
                .range(0, columnCount).mapToObj(Integer::toString)
                .map(NativeLabel::new).toList();
        for (int i = 0; i < columnCount; i++) {
            defaultHeaderRow.getCells().get(i)
                    .setComponent(defaultHeaderRowComponents.get(i));
        }
        HeaderRow newHeaderRow = grid.appendHeaderRow();
        newHeaderRow.getCells()
                .forEach(cell -> cell.setComponent(new NativeLabel("NEW")));
        grid.removeHeaderRow(newHeaderRow);
        List<HeaderCell> headerCells = grid.getHeaderRows().get(0).getCells();
        Assert.assertEquals(columnCount, headerCells.size());
        for (int i = 0; i < columnCount; i++) {
            Assert.assertEquals(defaultHeaderRowComponents.get(i),
                    headerCells.get(i).getComponent());
        }
    }

    @Test
    public void addHeaderRowsAlternatingPrependAndAppend_removeEachRowExceptDefault_rowsRemoved() {
        grid.appendHeaderRow();
        List<HeaderRow> headerRows = addMixedHeaderRows(6);
        headerRows.forEach(row -> grid.removeHeaderRow(row));
        Assert.assertEquals(1, grid.getHeaderRows().size());
    }

    @Test
    public void addFooterRowsAlternatingPrependAndAppend_removeEachRow_rowsRemoved() {
        List<FooterRow> footerRows = addMixedFooterRows(6);
        footerRows.forEach(row -> grid.removeFooterRow(row));
        Assert.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    public void addHeaderRowsAlternatingPrependAndAppend_removeEachRowInReverseOrder_rowsRemoved() {
        List<HeaderRow> headerRows = addMixedHeaderRows(6);
        Collections.reverse(headerRows);
        headerRows.forEach(row -> grid.removeHeaderRow(row));
        Assert.assertEquals(0, grid.getHeaderRows().size());
    }

    @Test
    public void addFooterRowsAlternatingPrependAndAppend_removeEachRowInReverseOrder_rowsRemoved() {
        List<FooterRow> footerRows = addMixedFooterRows(6);
        Collections.reverse(footerRows);
        footerRows.forEach(row -> grid.removeFooterRow(row));
        Assert.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    public void addHeaderRowsAlternatingPrependAndAppend_removeAllHeaderRows_rowsRemoved() {
        addMixedHeaderRows(6);
        grid.removeAllHeaderRows();
        Assert.assertEquals(0, grid.getHeaderRows().size());
    }

    @Test
    public void addFooterRowsAlternatingPrependAndAppend_removeAllFooterRows_rowsRemoved() {
        addMixedFooterRows(6);
        grid.removeAllFooterRows();
        Assert.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    public void addHeaderAndFooterRows_removeAllFooterRows_onlyFooterRowsRemoved() {
        List<HeaderRow> headerRows = addMixedHeaderRows(2);
        addMixedFooterRows(2);
        grid.removeAllFooterRows();
        Assert.assertEquals(headerRows.size(), grid.getHeaderRows().size());
        Assert.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    public void addHeaderAndFooterRows_removeAllHeaderRows_onlyHeaderRowsRemoved() {
        List<FooterRow> footerRows = addMixedFooterRows(2);
        addMixedHeaderRows(2);
        grid.removeAllHeaderRows();
        Assert.assertEquals(footerRows.size(), grid.getFooterRows().size());
        Assert.assertEquals(0, grid.getHeaderRows().size());
    }

    @Test
    public void addHeaderAndFooterRowsInMixedOrder_removeAllHeaderAndFooters_headersAndFootersRemoved() {
        List<HeaderRow> headerRows = addMixedHeaderRows(2);
        List<FooterRow> footerRows = addMixedFooterRows(2);
        headerRows.addAll(addMixedHeaderRows(2));
        footerRows.addAll(addMixedFooterRows(2));
        grid.removeAllHeaderRows();
        grid.removeAllFooterRows();
        Assert.assertEquals(0, grid.getHeaderRows().size());
        Assert.assertEquals(0, grid.getFooterRows().size());
    }

    @Test
    public void addHeaderRow_removeHeaderRow_addHeaderRow_headerAdded() {
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.removeHeaderRow(headerRow);
        grid.appendHeaderRow();
        Assert.assertEquals(1, grid.getHeaderRows().size());
    }

    @Test
    public void addFooterRow_removeFooterRow_addFooterRow_footerAdded() {
        FooterRow footerRow = grid.appendFooterRow();
        grid.removeFooterRow(footerRow);
        grid.appendFooterRow();
        Assert.assertEquals(1, grid.getFooterRows().size());
    }

    @Test
    public void setFooterPartName_columnGroupHasFooterPartName() {
        grid.appendFooterRow();
        var footerCell = grid.appendFooterRow().join(firstColumn, secondColumn);
        footerCell.setPartName("foo");
        Assert.assertEquals("foo", footerCell.getPartName());
        Assert.assertEquals("foo", footerCell.getColumn().getElement()
                .getProperty("footerPartName"));
    }

    private void assertHeaderRowOrder(HeaderRow... rows) {
        Assert.assertEquals("Grid returned unexpected amount of header rows",
                rows.length, grid.getHeaderRows().size());
        IntStream.range(0, rows.length).forEach(i -> {
            Assert.assertSame(
                    "Grid did not return expected header rows in order from top to bottom",
                    rows[i], grid.getHeaderRows().get(i));
        });
    }

    private void assertFooterRowOrder(FooterRow... rows) {
        Assert.assertEquals("Grid returned unexpected amount of footer rows",
                rows.length, grid.getFooterRows().size());
        IntStream.range(0, rows.length).forEach(i -> {
            Assert.assertSame(
                    "Grid did not return expected footer rows in order from top to bottom",
                    rows[i], grid.getFooterRows().get(i));
        });
    }

    private void assertRowWrapsLayer(AbstractRow<?> row, List<Element> layer,
            int expectedCellCount) {
        Assert.assertEquals("The row contains unexpected amount of cells",
                expectedCellCount, row.getCells().size());
        assertRowWrapsLayer(row, layer);
    }

    private void assertRowWrapsLayer(AbstractRow<?> row, List<Element> layer) {
        List<Element> cellWrappedElements = row.getCells().stream()
                .map(cell -> cell.getColumn().getElement())
                .collect(Collectors.toList());

        Assert.assertEquals(
                "The row contains unexpected amount of column elements",
                layer.size(), cellWrappedElements.size());

        IntStream.range(0, layer.size()).forEach(i -> {
            Assert.assertEquals(
                    "The row is not referring to expected column elements",
                    layer.get(i), cellWrappedElements.get(i));
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
        Assert.assertEquals("Unexpected amount of column layers", expectedCount,
                layers.size());
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
        Assert.assertTrue(child.getParent().isPresent());
        Assert.assertSame(child.getParent().get(), expectedParent);
        Assert.assertTrue(child.getElement().isVirtualChild());
    }

    private void assertIsNotVirtualChild(Component component) {
        Assert.assertFalse(component.getParent().isPresent());
        Assert.assertFalse(component.getElement().isVirtualChild());
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
}
