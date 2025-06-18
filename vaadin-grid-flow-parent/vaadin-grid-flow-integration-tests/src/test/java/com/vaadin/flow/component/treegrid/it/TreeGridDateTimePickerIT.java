/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/tree-grid-date-time-picker")
public class TreeGridDateTimePickerIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void shouldHaveI18nAppliedToRoot() {
        Assert.assertEquals("13.06.2000", $(DateTimePickerElement.class)
                .id("id-Row-1").getDatePresentation());
    }

    @Test
    public void shouldHaveI18nAppliedToChild() {
        Assert.assertEquals("13.06.2000", $(DateTimePickerElement.class)
                .id("id-Child-1").getDatePresentation());
    }
}
