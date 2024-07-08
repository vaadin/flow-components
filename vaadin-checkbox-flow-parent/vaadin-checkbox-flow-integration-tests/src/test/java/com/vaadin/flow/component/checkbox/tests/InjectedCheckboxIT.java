/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-checkbox/injected-checkbox")
public class InjectedCheckboxIT extends AbstractComponentIT {

    @Test
    public void initialCheckboxValue() {
        open();

        String isChecked = $("inject-checkbox").first().$("vaadin-checkbox")
                .first().getAttribute("checked");
        Assert.assertEquals(Boolean.TRUE.toString(), isChecked);
    }
}
