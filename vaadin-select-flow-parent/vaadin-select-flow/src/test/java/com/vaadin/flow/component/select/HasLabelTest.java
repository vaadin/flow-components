package com.vaadin.flow.component.select;

import com.vaadin.flow.component.HasLabel;
import org.junit.Assert;
import org.junit.Test;

public class HasLabelTest {

    @Test
    public void select() {
        Select<String> c = new Select<>();
        Assert.assertTrue(c instanceof HasLabel);
    }

}
