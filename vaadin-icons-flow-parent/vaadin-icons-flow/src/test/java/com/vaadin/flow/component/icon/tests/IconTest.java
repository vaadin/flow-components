/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.icon.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.shared.HasTooltip;

public class IconTest {
    @Test
    public void implementsHasTooltip() {
        Icon icon = new Icon();
        Assert.assertTrue(icon instanceof HasTooltip);
    }
}
