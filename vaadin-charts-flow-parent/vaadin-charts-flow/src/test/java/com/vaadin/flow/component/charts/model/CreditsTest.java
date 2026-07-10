/**
 * Copyright 2000-2025 Vaadin Ltd.
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

public class CreditsTest {

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
    public void setHref_safeScheme_hrefSet() {
        Credits credits = new Credits();
        credits.setHref("https://vaadin.com");

        Assert.assertEquals("https://vaadin.com", credits.getHref());
    }

    @Test
    public void setHref_unsafeScheme_throws() {
        Credits credits = new Credits();

        Assert.assertThrows(IllegalArgumentException.class,
                () -> credits.setHref("javascript:alert(1)"));
    }

    @Test
    public void setUnsafeHref_unsafeScheme_hrefSet() {
        Credits credits = new Credits();
        credits.setUnsafeHref("javascript:alert(1)");

        Assert.assertEquals("javascript:alert(1)", credits.getHref());
    }
}
