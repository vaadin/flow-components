/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.select;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasPlaceholder;

public class HasPlaceholderTest {

    @Test
    public void select() {
        Select<String> c = new Select<>();
        Assert.assertTrue(c instanceof HasPlaceholder);
    }

}
