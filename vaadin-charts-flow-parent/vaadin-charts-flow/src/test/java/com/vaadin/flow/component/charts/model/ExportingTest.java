/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinService;

public class ExportingTest {

    private static MockedStatic<VaadinService> vaadinServiceMock;

    @BeforeClass
    public static void enableUrlSchemeValidation() {
        // URL scheme validation is disabled by default in this branch, so
        // configure a strict set of safe schemes to exercise the validation.
        DeploymentConfiguration config = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(config.getUrlSafeSchemes())
                .thenReturn(Set.of("http", "https", "mailto", "tel", "ftp"));
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(service.getDeploymentConfiguration()).thenReturn(config);
        vaadinServiceMock = Mockito.mockStatic(VaadinService.class);
        vaadinServiceMock.when(VaadinService::getCurrent).thenReturn(service);
    }

    @AfterClass
    public static void cleanup() {
        vaadinServiceMock.close();
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
