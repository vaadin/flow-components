/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.dashboard.testbench.DashboardElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link DashboardI18nPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-dashboard/i18n")
public class DashboardI18nIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void dashboardHasCorrectDefaultI18nKeys() {
        for (DashboardI18nPage.I18nEntry i18NEntry : DashboardI18nPage.I18nEntry
                .values()) {
            Assert.assertNotNull(getI18nValue(i18NEntry));
        }
    }

    @Test
    public void setCustomI18n_i18nIsUpdated() {
        clickElementWithJs("set-custom-i18n");
        for (DashboardI18nPage.I18nEntry i18NEntry : DashboardI18nPage.I18nEntry
                .values()) {
            String expectedI18nValue = i18NEntry.getCustomValue();
            Assert.assertEquals(expectedI18nValue, getI18nValue(i18NEntry));
        }
    }

    private String getI18nValue(DashboardI18nPage.I18nEntry i18NEntry) {
        return dashboardElement.getPropertyString("i18n", i18NEntry.getKey());
    }
}
