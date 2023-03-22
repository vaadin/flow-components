package com.vaadin.flow.component.grid.customselectionmodel;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.grid.AbstractGridMultiSelectionModel;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.selection.SelectionEvent;
import org.junit.Test;

public class CustomSelectionModelTest {

    @Test
    public void customMultiSelectionModelGridInstantiation_doesNotThrow() {
        CustomGrid<String> customGrid = new CustomGrid<>();
    }

    /**
     * Sample class that extends Grid for setting a custom selection model
     * @param <T>
     */
    public static class CustomGrid<T> extends Grid<T> {

        public CustomGrid() {
            setSelectionModel(new CustomMultiSelectionModel<>(this), SelectionMode.MULTI);
        }

        class CustomMultiSelectionModel<T> extends AbstractGridMultiSelectionModel<T> {
            public CustomMultiSelectionModel(CustomGrid<T> grid) {
                super(grid);
            }

            @Override
            protected void fireSelectionEvent(SelectionEvent<Grid<T>, T> event) {
                ((CustomGrid) event.getSource()).fireSelectionEvent(event);
            }
            @Override
            protected void clientSelectAll() {
                getSelectionColumn().setSelectAllCheckboxState(true);
                getSelectionColumn().setSelectAllCheckboxIndeterminateState(false);
            }

        }

        void fireSelectionEvent(SelectionEvent<Grid<T>, T> event) {
            super.fireEvent((ComponentEvent<Grid<?>>) event);
        }

    }
}
