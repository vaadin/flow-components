/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.tests.MockUIRule;

public class ExportingTest {

    @Rule
    public MockUIRule mockUIRule = new MockUIRule();

    @Before
    public void enableUrlSchemeValidation() {
        mockUIRule.enableUrlSchemeValidation();
    }

    @Test
    public void setUrl_safeScheme_urlSet() {
        Exporting exporting = new Exporting();
        exporting.setUrl("https://export.highcharts.com");

        Assert.assertEquals("https://export.highcharts.com",
                exporting.getUrl());
    }

    @Test
    public void setUrl_unsafeScheme_throws() {
        Exporting exporting = new Exporting();

        Assert.assertThrows(IllegalArgumentException.class,
                () -> exporting.setUrl("javascript:alert(1)"));
    }

    @Test
    public void setUnsafeUrl_unsafeScheme_urlSet() {
        Exporting exporting = new Exporting();
        exporting.setUnsafeUrl("javascript:alert(1)");

        Assert.assertEquals("javascript:alert(1)", exporting.getUrl());
    }

    @Test
    public void setLibURL_safeScheme_libURLSet() {
        Exporting exporting = new Exporting();
        exporting.setLibURL("https://code.highcharts.com/lib");

        Assert.assertEquals("https://code.highcharts.com/lib",
                exporting.getLibURL());
    }

    @Test
    public void setLibURL_unsafeScheme_throws() {
        Exporting exporting = new Exporting();

        Assert.assertThrows(IllegalArgumentException.class,
                () -> exporting.setLibURL("javascript:alert(1)"));
    }

    @Test
    public void setUnsafeLibURL_unsafeScheme_libURLSet() {
        Exporting exporting = new Exporting();
        exporting.setUnsafeLibURL("javascript:alert(1)");

        Assert.assertEquals("javascript:alert(1)", exporting.getLibURL());
    }
}
