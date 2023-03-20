
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-checkbox/refresh-on-value-change")
public class RefreshOnValueChangeIT extends AbstractComponentIT {

    @Test
    public void subsequentValueChangesDontAffectElementCount() {
        open();
        Assert.assertEquals(2, $("vaadin-checkbox").all().size());

        $("vaadin-checkbox").first().click();

        Assert.assertEquals(2, $("vaadin-checkbox").all().size());
    }
}
