
package com.vaadin.flow.component.datepicker;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-date-picker/injected-datepicker")
public class InjectedDatePickerI18nIT extends AbstractComponentIT {

    @Test
    public void checkInitialI18n() {
        open();

        $("injected-datepicker-i18n").first().$("vaadin-date-picker").first()
                .$("input").first().click();

        TestBenchElement cancelButton = $("vaadin-date-picker-overlay").first()
                .$("div").id("content").$("vaadin-date-picker-overlay-content")
                .first().$("vaadin-button").id("cancelButton");

        Assert.assertEquals("peruuta", cancelButton.getText());
    }
}
