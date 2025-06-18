/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-radio-button/refresh-on-value-change")
public class RefreshOnValueChangeIT extends AbstractComponentIT {

    @Test
    public void subsequentValueChangesDontAffectElementCount() {
        open();
        Assert.assertEquals(2, $("vaadin-radio-button").all().size());

        $("vaadin-radio-button").first().click();

        Assert.assertEquals(2, $("vaadin-radio-button").all().size());
    }
}
