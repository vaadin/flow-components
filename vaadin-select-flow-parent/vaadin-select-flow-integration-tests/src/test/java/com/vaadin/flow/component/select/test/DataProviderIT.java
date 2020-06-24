package com.vaadin.flow.component.select.test;

import java.util.List;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;

@TestPath("")
public class DataProviderIT extends AbstractSelectIT {
    @Override
    protected int getInitialNumberOfItems() {
        return 20;
    }

    @Test
    public void testDataProvider_initialItems_allItemsRendered() {
        List<SelectElement.ItemElement> items = selectElement.getItems();
        Assert.assertEquals("invalid number of items", getInitialNumberOfItems(), items.size());

        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement itemElement = items.get(i);
            Assert.assertEquals("invalid key", i + 1 + "", itemElement.getPropertyString("value"));
            Assert.assertEquals("invalid text", "Item-" + i, itemElement.getText());
        }
    }

    @Test
    public void testDataProvider_newDataProvider_itemsRefreshed() {
        page.clickResetNItems(5);

        List<SelectElement.ItemElement> items = selectElement.getItems();
        Assert.assertEquals("invalid number of items", 5, items.size());
        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement itemElement = items.get(i);
            Assert.assertEquals("invalid key", (i + 20 + 1) + "", itemElement.getPropertyString("value"));
            Assert.assertEquals("invalid text", "Item-" + (i + 20), itemElement.getText());
        }

        page.clickResetNItems(2);

        items = selectElement.getItems();
        Assert.assertEquals("invalid number of items", 2, items.size());
        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement itemElement = items.get(i);
            Assert.assertEquals("invalid key", (i + 20 + 5 + 1) + "", itemElement.getPropertyString("value"));
            Assert.assertEquals("invalid text", "Item-" + (i + 20 + 5), itemElement.getText());
        }
    }

    @Test
    public void testDataProvider_emptyDataProvider_noItems() {
        open();

        // FIXME trying to open an empty select will throw inside the web component
        // List<SelectElement.ItemElement> items = selectElement.getItems();
        // Assert.assertEquals("invalid number of items", 0, items.size());

        page.clickResetNItems(5);

        List<SelectElement.ItemElement> items = selectElement.getItems();
        Assert.assertEquals("invalid number of items", 5, items.size());

        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement itemElement = items.get(i);
            Assert.assertEquals("invalid key", (i + 1) + "", itemElement.getPropertyString("value"));
            Assert.assertEquals("invalid text", "Item-" + i, itemElement.getText());
        }

        page.clickResetNItems(0);

        items = selectElement.getItems();
        Assert.assertEquals("invalid number of items", 0, items.size());
    }

    @Test
    public void testDataProvider_testRefreshAllItems_itemsShouldBeUpdated() {
        page.clickRefreshAll();

        List<SelectElement.ItemElement> items = selectElement.getItems();
        Assert.assertEquals("invalid number of items", getInitialNumberOfItems(), items.size());

        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement itemElement = items.get(i);
            // full reset happened so new keys for items
            Assert.assertEquals("invalid key", i + 1 + 20 + "", itemElement.getPropertyString("value"));
            Assert.assertEquals("invalid text", "Item-" + i + "-UPDATED", itemElement.getText());
        }

    }

    @Test
    public void testDataProvider_testRefreshItem_itemIsUpdatedValuePropertyNot() {
        page.clickRefreshItem(0);

        SelectElement.ItemElement itemElement = selectElement.getItems().get(0);
        Assert.assertEquals("invalid key", 1 + "", itemElement.getPropertyString("value"));
        Assert.assertEquals("invalid text", "Item-0-UPDATED", itemElement.getText());

        page.clickRefreshItem(2);

        itemElement = selectElement.getItems().get(2);
        Assert.assertEquals("invalid key", 3 + "", itemElement.getPropertyString("value"));
        Assert.assertEquals("invalid text", "Item-2-UPDATED", itemElement.getText());

        page.clickRefreshItem(2);

        itemElement = selectElement.getItems().get(2);
        Assert.assertEquals("invalid key", 3 + "", itemElement.getPropertyString("value"));
        Assert.assertEquals("invalid text", "Item-2-UPDATED-UPDATED", itemElement.getText());

        page.clickRefreshItem(10);

        itemElement = selectElement.getItems().get(10);
        Assert.assertEquals("invalid key", 11 + "", itemElement.getPropertyString("value"));
        Assert.assertEquals("invalid text", "Item-10-UPDATED", itemElement.getText());
    }

    @Test
    public void testDataProvider_testRefreshSelectedItem_itemIsUpdatedCorrectly() {
        SelectElement.ItemElement itemElement = selectElement.getItems().get(2);
        Assert.assertEquals("invalid key", 3 + "", itemElement.getPropertyString("value"));
        Assert.assertEquals("invalid text", "Item-2", itemElement.getText());

        selectElement.selectItemByIndex(2);

        verify.valueChangeEvent("Item-2", "null", true, 0);
        verify.selectedItem("Item-2");

        page.clickRefreshItem(2);

        Assert.assertEquals("invalid key", 3 + "", itemElement.getPropertyString("value"));
        Assert.assertEquals("invalid text", "Item-2-UPDATED", itemElement.getText());
        verify.selectedItem("Item-2-UPDATED");
    }
}
