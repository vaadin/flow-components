package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class LayoutDefaultsTest {

    @Test
    public void testHorizontalLayout_byDefault_spacingIsOn() {
        Assert.assertTrue("Spacing should be on by default",
                new HorizontalLayout().isSpacing());
        Assert.assertFalse("Padding shouldn't be on by default",
                new HorizontalLayout().isPadding());
        Assert.assertFalse("Margin shouldn't be on by default",
                new HorizontalLayout().isMargin());
    }

    @Test
    public void testVerticalLayout_byDefault_spacingAndPaddingIsOn() {
        Assert.assertTrue("Padding should be on by default",
                new VerticalLayout().isPadding());
        Assert.assertTrue("Spacing should be on by default",
                new VerticalLayout().isSpacing());
        Assert.assertFalse("Margin shouldn't be on by default",
                new VerticalLayout().isMargin());
    }
}
