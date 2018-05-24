package com.vaadin.flow.component.datepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("date-picker-locale")
public class DatePickerLocaleIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void testPickerWithValueAndLocaleFromServerSideDifferentCtor() {
        WebElement localePicker = findElement(
                By.id("locale-picker-server-with-value"));
        WebElement displayText = findInShadowRoot(localePicker, By.id("input"))
                .get(0);

        Assert.assertEquals("Initial date is 2018/5/23", true, executeScript(
                "return arguments[0].value === '2018/5/23'", displayText));

        findElement(By.id("uk-locale")).click();
        Assert.assertEquals("UK locale date is 23/05/2018", true, executeScript(
                "return arguments[0].value === '23/05/2018'", displayText));
    }
}
