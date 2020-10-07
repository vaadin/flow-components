package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.renderer.IconRenderer;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class GridColumnOrderTest {
    private Grid<String> grid;
    private Grid.Column<String> firstColumn;
    private Grid.Column<String> secondColumn;
    private Grid.Column<String> thirdColumn;
    private Grid.Column<String> fourthColumn;

    @Before
    public void init() {
        grid = new Grid<>();
        firstColumn = grid.addColumn(str -> str).setKey("firstColumn");
        secondColumn = grid.addColumn(str -> str).setKey("secondColumn");
        thirdColumn = grid.addColumn(str -> str).setKey("thirdColumn");
        IconRenderer<String> renderer = new IconRenderer<>(generator -> new Label(":D"));
        fourthColumn = grid.addColumn(renderer).setKey("fourthColumn");
    }


    @Test
    public void setColumnOrder_doesNothingOnEmptyInput() {
        grid.removeAllColumns();
        grid.setColumnOrder();
        assertArrayEquals(new Object[0], grid.getColumns().toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setColumnOrder_failsWhenColumnIsPresentTwoTimes() {
        grid.setColumnOrder(firstColumn, secondColumn, firstColumn, thirdColumn, fourthColumn);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setColumnOrder_failsOnColumnFromAnotherGrid() {
        grid.setColumnOrder(firstColumn, secondColumn, thirdColumn, fourthColumn, new Grid<String>().addColumn(s -> null));
    }

    @Test
    public void setColumnOrder_simpleCase() {
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertArrayEquals(new Object[]{fourthColumn, thirdColumn, secondColumn, firstColumn}, grid.getColumns().toArray());
    }

    @Test
    public void setColumnOrder_reorderPlusRemoval() {
        grid.removeColumns(secondColumn, thirdColumn);
        grid.setColumnOrder(fourthColumn, firstColumn);
        assertArrayEquals(new Object[]{fourthColumn, firstColumn}, grid.getColumns().toArray());
    }

    @Test
    public void setColumnOrder_doesNothingOnCurrentColumnOrdering() {
        grid.setColumnOrder(grid.getColumns());
        assertArrayEquals(new Object[]{firstColumn, secondColumn, thirdColumn, fourthColumn}, grid.getColumns().toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setColumnOrder_failsOnMissingColumns() {
        grid.setColumnOrder(firstColumn, secondColumn);
    }

    @Test
    public void setColumnOrder_simpleCaseWithHeader() {
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        // verify that the Grid wrapped <grid-column> elements in <grid-column-group> elements
        assertEquals(4, grid.getChildren().filter(it -> it instanceof ColumnGroup).count());
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertArrayEquals(new Object[]{fourthColumn, thirdColumn, secondColumn, firstColumn}, grid.getColumns().toArray());
    }

    @Test
    public void setColumnOrder_simpleCaseWithTenHeadersAndFooters() {
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        grid.appendFooterRow();
        grid.appendFooterRow();
        grid.appendFooterRow();
        grid.appendFooterRow();
        grid.appendFooterRow();
        grid.appendFooterRow();
        grid.appendFooterRow();
        grid.appendFooterRow();
        grid.appendFooterRow();
        grid.appendFooterRow();
        // verify that the Grid wrapped <grid-column> elements in <grid-column-group> elements
        assertEquals(4, grid.getChildren().filter(it -> it instanceof ColumnGroup).count());
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertArrayEquals(new Object[]{fourthColumn, thirdColumn, secondColumn, firstColumn}, grid.getColumns().toArray());
    }

    @Test
    public void setColumnOrder_headerFooterMultiSelect() {
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        // verify that the Grid wrapped <grid-column> elements in <grid-column-group> elements
        assertEquals(4, grid.getChildren().filter(it -> it instanceof ColumnGroup).count());
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertArrayEquals(new Object[]{fourthColumn, thirdColumn, secondColumn, firstColumn}, grid.getColumns().toArray());
    }

    @Test
    public void setColumnOrder_headerFooterSingleSelect() {
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.appendHeaderRow();
        grid.appendHeaderRow();
        // verify that the Grid wrapped <grid-column> elements in <grid-column-group> elements
        assertEquals(4, grid.getChildren().filter(it -> it instanceof ColumnGroup).count());
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertArrayEquals(new Object[]{fourthColumn, thirdColumn, secondColumn, firstColumn}, grid.getColumns().toArray());
    }

    @Test
    public void setColumnOrder_simpleCaseWithFooter() {
        grid.appendFooterRow();
        grid.appendFooterRow();
        // verify that the Grid wrapped <grid-column> elements in <grid-column-group> elements
        assertEquals(4, grid.getChildren().filter(it -> it instanceof ColumnGroup).count());
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertArrayEquals(new Object[]{fourthColumn, thirdColumn, secondColumn, firstColumn}, grid.getColumns().toArray());
    }

    @Test
    public void setColumnOrder_firesColumnReorderEvent() {
        final AtomicReference<ColumnReorderEvent> event = new AtomicReference<>();
        grid.addColumnReorderListener(event::set);
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertNotNull(event.get());
        assertArrayEquals(new Object[]{fourthColumn, thirdColumn, secondColumn, firstColumn}, event.get().getColumns().toArray());
        assertFalse(event.get().isFromClient());
        assertSame(grid, event.get().getSource());
    }

    @Test
    public void joiningHeadersWorkAfterReordering1() {
        grid.appendHeaderRow();
        final HeaderRow header = grid.prependHeaderRow();
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertEquals("(fourthColumn), (thirdColumn), (secondColumn), (firstColumn)", dumpColumnHierarchyFromDOM());
        header.join(fourthColumn, thirdColumn);
        assertEquals("(fourthColumn, thirdColumn), (secondColumn), (firstColumn)", dumpColumnHierarchyFromDOM());
    }

    @Test
    public void joiningHeadersWorkAfterReordering2() {
        grid.appendHeaderRow();
        final HeaderRow header = grid.prependHeaderRow();
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertEquals("(fourthColumn), (thirdColumn), (secondColumn), (firstColumn)", dumpColumnHierarchyFromDOM());
        header.join(secondColumn, firstColumn);
        assertEquals("(fourthColumn), (thirdColumn), (secondColumn, firstColumn)", dumpColumnHierarchyFromDOM());
    }

    @Test
    public void joiningHeadersWorkAfterReordering3() {
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertEquals("fourthColumn, thirdColumn, secondColumn, firstColumn", dumpColumnHierarchyFromDOM());
        grid.appendHeaderRow();
        final HeaderRow header = grid.prependHeaderRow();
        header.join(secondColumn, firstColumn);
        assertEquals("(fourthColumn), (thirdColumn), (secondColumn, firstColumn)", dumpColumnHierarchyFromDOM());
    }

    @Test
    public void joiningHeadersWorkAfterReordering4() {
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertEquals("fourthColumn, thirdColumn, secondColumn, firstColumn", dumpColumnHierarchyFromDOM());
        grid.appendHeaderRow();
        final HeaderRow header = grid.prependHeaderRow();
        header.join(secondColumn, thirdColumn);
        assertEquals("(fourthColumn), (thirdColumn, secondColumn), (firstColumn)", dumpColumnHierarchyFromDOM());
    }

    @Test
    public void setColumnOrder_simpleJoinedHeader() {
        grid.appendHeaderRow();
        grid.prependHeaderRow().join(firstColumn, secondColumn);
        grid.setColumnOrder(firstColumn, secondColumn, thirdColumn, fourthColumn);
        assertArrayEquals(new Object[]{firstColumn, secondColumn, thirdColumn, fourthColumn}, grid.getColumns().toArray());
    }

    @Test
    public void setColumnOrder_simpleJoinedFooter() {
        grid.appendFooterRow();
        grid.appendFooterRow().join(firstColumn, secondColumn);
        grid.setColumnOrder(firstColumn, secondColumn, thirdColumn, fourthColumn);
        assertArrayEquals(new Object[]{firstColumn, secondColumn, thirdColumn, fourthColumn}, grid.getColumns().toArray());
    }

    @Test
    public void setColumnOrder_joinedHeader2() {
        grid.appendHeaderRow();
        grid.prependHeaderRow().join(firstColumn, secondColumn);
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertArrayEquals(new Object[]{fourthColumn, thirdColumn, secondColumn, firstColumn}, grid.getColumns().toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setColumnOrder_impossibleOrderingWithJoinedHeaders() {
        grid.appendHeaderRow();
        grid.prependHeaderRow().join(firstColumn, secondColumn);
        grid.setColumnOrder(fourthColumn, secondColumn, thirdColumn, firstColumn);
    }

    @Test
    public void setColumnOrder_joinedHeader3() {
        grid.appendHeaderRow();
        grid.prependHeaderRow().join(secondColumn, thirdColumn);
        grid.setColumnOrder(fourthColumn, thirdColumn, secondColumn, firstColumn);
        assertArrayEquals(new Object[]{fourthColumn, thirdColumn, secondColumn, firstColumn}, grid.getColumns().toArray());
        grid.setColumnOrder(thirdColumn, secondColumn, firstColumn, fourthColumn);
        assertArrayEquals(new Object[]{thirdColumn, secondColumn, firstColumn, fourthColumn}, grid.getColumns().toArray());
        grid.setColumnOrder(firstColumn, fourthColumn, secondColumn, thirdColumn);
        assertArrayEquals(new Object[]{firstColumn, fourthColumn, secondColumn, thirdColumn}, grid.getColumns().toArray());
    }

    private String dumpColumnHierarchyFromDOM() {
        return dumpColumnHierarchyFromDOM(grid);
    }

    private String dumpColumnHierarchyFromDOM(Component component) {
        return component.getChildren().map(child -> {
            if (child instanceof Grid.Column) {
                return ((Grid.Column) child).getKey();
            } else {
                return "(" + dumpColumnHierarchyFromDOM(child) + ")";
            }
        }).collect(Collectors.joining(", "));
    }
}
