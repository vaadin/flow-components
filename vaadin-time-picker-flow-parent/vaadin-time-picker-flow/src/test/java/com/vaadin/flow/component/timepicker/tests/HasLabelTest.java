package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.timepicker.TimePicker;
import org.junit.Assert;
import org.junit.Test;

public class HasLabelTest {

    @Test
    public void tab() {
        TimePicker c = new TimePicker();
        Assert.assertTrue(c instanceof HasLabel);
    }

}
