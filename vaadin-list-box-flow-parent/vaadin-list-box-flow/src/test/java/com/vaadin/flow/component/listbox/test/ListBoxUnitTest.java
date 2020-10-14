package com.vaadin.flow.component.listbox.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;

public class ListBoxUnitTest {

    private static final String ITEM1 = "1";
    private static final String ITEM2 = "2";

    private ListBox<String> listBox;

    @Before
    public void init() {
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
    public void setItemEnabledProvider_itemDisabled() {
        listBox.setItemEnabledProvider(item -> item != ITEM2);
        assertDisabledItem(0, false);
        assertDisabledItem(1, true);

        listBox.setItems(ITEM2, ITEM1);
        assertDisabledItem(0, true);
        assertDisabledItem(1, false);
    }

    @Test
    public void setIdentifierProvider_shouldSelectCorrectItem_dueToIdentifier() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(Arrays.asList(first, second,
                third));

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
