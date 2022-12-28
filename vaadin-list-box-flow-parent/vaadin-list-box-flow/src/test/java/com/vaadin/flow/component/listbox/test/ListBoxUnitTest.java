package com.vaadin.flow.component.listbox.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.tests.DataProviderListenersTest;

public class ListBoxUnitTest {

    private static final String ITEM1 = "1";
    private static final String ITEM2 = "2";

    private ListBox<String> listBox;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        listBox = new ListBox<>();
        listBox.setItems(ITEM1, ITEM2);
    }

    @Test
    public void getValue_returnsNull() {
        Assert.assertNull(listBox.getValue());
    }

    @Test
    public void setValue_getValue_returnsValue() {
        listBox.setValue(ITEM1);
        Assert.assertEquals(ITEM1, listBox.getValue());
    }

    @Test
    public void setValue_changeItemSet_getValue_returnsNull() {
        listBox.setValue(ITEM1);
        listBox.setItems("a");
        Assert.assertNull(listBox.getValue());
    }

    @Test
    public void selectItem_changeDataProvider_selectionIsReset() {
        AtomicReference<String> capture = new AtomicReference<>();
        listBox.addValueChangeListener(event -> capture.set(event.getValue()));

        listBox.setValue(ITEM1);

        Assert.assertEquals(ITEM1, capture.get());
        Assert.assertEquals(ITEM1, listBox.getValue());

        listBox.setItems("Foo", "Bar");

        Assert.assertEquals(null, listBox.getValue());
        Assert.assertEquals(null, capture.get());
    }

    @Test
    public void selectItem_updateDataSource_refreshAll_selectionIsRetained() {
        List<String> items = new ArrayList<>();
        items.add("Foo");
        items.add("Bar");

        listBox.setItems(items);

        AtomicReference<String> capture = new AtomicReference<>();
        listBox.addValueChangeListener(event -> capture.set(event.getValue()));

        listBox.setValue("Foo");

        Assert.assertEquals("Foo", capture.get());
        Assert.assertEquals("Foo", listBox.getValue());

        items.add("Baz");
        items.remove(1);
        listBox.getListDataView().refreshAll();

        Assert.assertEquals("Foo", capture.get());
        Assert.assertEquals("Foo", listBox.getValue());
    }

    @Test
    public void selectItem_removeItemFromDataSource_refreshAll_selectionIsReset() {
        List<String> items = new ArrayList<>();
        items.add("Foo");
        items.add("Bar");

        listBox.setItems(items);

        AtomicReference<String> capture = new AtomicReference<>();
        listBox.addValueChangeListener(event -> capture.set(event.getValue()));

        listBox.setValue("Foo");

        Assert.assertEquals("Foo", capture.get());
        Assert.assertEquals("Foo", listBox.getValue());

        items.remove(0);
        listBox.getListDataView().refreshAll();

        Assert.assertEquals(null, listBox.getValue());
        Assert.assertEquals(null, capture.get());
    }

    @Test
    public void selectItem_setItemLabelGenerator_selectionIsRetained() {
        AtomicReference<String> capture = new AtomicReference<>();
        listBox.addValueChangeListener(event -> capture.set(event.getValue()));

        listBox.setValue(ITEM1);

        Assert.assertEquals(ITEM1, capture.get());
        Assert.assertEquals(ITEM1, listBox.getValue());

        listBox.setItemLabelGenerator(item -> item + " (Updated)");

        Assert.assertEquals(ITEM1, capture.get());
        Assert.assertEquals(ITEM1, listBox.getValue());
    }

    @Test
    public void setItemEnabledProvider_itemDisabled() {
        listBox.setItemEnabledProvider(item -> item != ITEM2);
        assertDisabledItem(0, false);
        assertDisabledItem(1, true);

        listBox.setItems(ITEM2, ITEM1);
        assertDisabledItem(0, true);
        assertDisabledItem(1, false);
    }

    @Test
    public void setIdentifierProvider_setItemWithIdentifierOnly_shouldSelectCorrectItem() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> listBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = listBox.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        listBox.setValue(new CustomItem(1L));

        Assert.assertNotNull(listBox.getValue());
        Assert.assertEquals(listBox.getValue().getName(), "First");

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        listBox.setValue(new CustomItem(2L));

        Assert.assertNotNull(listBox.getValue());
        Assert.assertEquals(Long.valueOf(2L), listBox.getValue().getId());
    }

    @Test
    public void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> listBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = listBox.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        listBox.setValue(new CustomItem(1L));

        Assert.assertNotNull(listBox.getValue());
        Assert.assertEquals(listBox.getValue().getName(), "First");

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item with an Id and the name that can be wrongly equals to
        // another items, should verify that <equals> method is not in use:
        listBox.setValue(new CustomItem(3L, "Second"));

        Assert.assertNotNull(listBox.getValue());
        Assert.assertEquals(Long.valueOf(3L), listBox.getValue().getId());
    }

    @Test
    public void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> listBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = listBox.setItems(items);

        listBox.setValue(new CustomItem(null, "Second"));

        Assert.assertNotNull(listBox.getValue());
        Assert.assertEquals(Long.valueOf(2L), listBox.getValue().getId());
    }

    @Test
    public void setIdentifierProviderOnId_setItemWithNullId_shouldThrowException() {

        thrown.expect(NullPointerException.class);

        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> listBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = listBox.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        listBox.setValue(new CustomItem(null, "First"));
    }

    @Test
    public void dataProviderListeners_listBoxAttachedAndDetached_oldDataProviderListenerRemoved() {
        DataProviderListenersTest
                .checkOldListenersRemovedOnComponentAttachAndDetach(
                        new ListBox<>(), 1, 1, new int[] { 0, 1 },
                        new DataCommunicatorTest.MockUI());
    }

    @Test
    public void implementsHasTooltip() {
        Assert.assertTrue(listBox instanceof HasTooltip);
    }

    private void assertDisabledItem(int index, boolean disabled) {
        if (disabled) {
            Assert.assertNotNull(listBox.getElement().getChild(index)
                    .getAttribute("disabled"));
        } else {
            Assert.assertNull(listBox.getElement().getChild(index)
                    .getAttribute("disabled"));
        }
    }
}
