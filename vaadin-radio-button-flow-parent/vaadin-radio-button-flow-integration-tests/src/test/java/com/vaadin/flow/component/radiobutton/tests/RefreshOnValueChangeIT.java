
package com.vaadin.flow.component.radiobutton.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

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
