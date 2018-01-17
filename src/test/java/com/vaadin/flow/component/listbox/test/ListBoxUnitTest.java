package com.vaadin.flow.component.listbox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.listbox.ListBox;

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

    private void assertDisabledItem(int index, boolean disabled) {
        Assert.assertEquals(disabled, listBox.getElement().getChild(index)
                .getProperty("disabled", false));
    }

}
