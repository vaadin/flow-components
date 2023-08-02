package com.vaadin.flow.component.grid;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for Grid DataProvider change event
 *
 * @author Vaadin Ltd.
 */
public class GridDataProviderChangeTest {

    transient List<String> dummyData = Arrays.asList("item1", "item2", "item3");

    @Test
    public void setItemsInConstructor_noChangeEvent() {
        Grid<String> grid = new Grid<>(dummyData);

        grid.addDataProviderChangeListener(event -> {
            Assert.fail(
                    "DataProvider change should not be triggered from server");
        });
    }

    @Test
    public void setItems_triggerChangeEvent() {
        Grid<String> grid = new Grid<>();

        AtomicInteger listenerCalled = new AtomicInteger();
        grid.addDataProviderChangeListener(
                event -> listenerCalled.incrementAndGet());

        grid.setItems(dummyData);
        Assert.assertEquals(1, listenerCalled.get());

        grid.setItems(dummyData);
        Assert.assertEquals(2, listenerCalled.get());
    }
}
