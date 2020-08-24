package com.vaadin.flow.component.grid.dataview;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;

public class GridLazyDataViewTest {

    private GridLazyDataView<String> dataView;
    private Grid<String> grid;
    private DataCommunicatorTest.MockUI ui;

    @Before
    public void setup() {
        BackEndDataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> {
                    query.getOffset();
                    query.getLimit();
                    return Stream.of("foo", "bar", "baz");
                }, query -> 3);

        grid = new Grid<>();
        ui = new DataCommunicatorTest.MockUI();
        ui.add(grid);

        dataView = grid.setItems(dataProvider);
    }

    @Test
    public void setItemCountCallback_switchFromUndefinedSize_definedSize() {
        Assert.assertTrue(grid.getDataCommunicator().isDefinedSize());

        dataView.setItemCountUnknown();
        Assert.assertFalse(grid.getDataCommunicator().isDefinedSize());

        dataView.setItemCountCallback(query -> 5);
        Assert.assertTrue(grid.getDataCommunicator().isDefinedSize());
    }

    @Test
    public void setItemCountCallback_setAnotherCountCallback_itemCountChanged() {
        final AtomicInteger itemCount = new AtomicInteger(0);
        dataView.addItemCountChangeListener(
                event -> itemCount.set(event.getItemCount()));
        grid.getDataCommunicator().setRequestedRange(0, 50);

        dataView.setItemCountCallback(query -> 2);

        fakeClientCommunication();

        Assert.assertEquals("Invalid item count reported", 2, itemCount.get());
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
