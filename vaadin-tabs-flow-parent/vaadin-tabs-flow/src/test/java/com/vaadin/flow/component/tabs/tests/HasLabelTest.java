package com.vaadin.flow.component.tabs.tests;

import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.tabs.Tab;
import org.junit.Assert;
import org.junit.Test;

public class HasLabelTest {

    @Test
    public void tab() {
        Tab c = new Tab();
        Assert.assertTrue(c instanceof HasLabel);
    }

}
