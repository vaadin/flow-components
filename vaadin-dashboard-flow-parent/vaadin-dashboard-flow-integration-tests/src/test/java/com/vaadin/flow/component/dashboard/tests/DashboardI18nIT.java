/**
 * Copyright 2000-2024 Vaadin Ltd.
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
    public void itemsHaveCorrectDefaultI18N() {
        assertI18nValues(true);
    }

    @Test
    public void setCustomI18n_i18nIsUpdated() {
        clickElementWithJs("set-custom-i18n");
        assertI18nValues(false);
    }

    private void assertI18nValues(boolean isDefault) {
        for (DashboardI18nPage.I18nEntry i18NEntry : DashboardI18nPage.I18nEntry
                .values()) {
            String expectedValue = isDefault ? i18NEntry.getDefaultValue()
                    : i18NEntry.getCustomValue();
            Assert.assertEquals(expectedValue, dashboardElement
                    .getPropertyString("i18n", i18NEntry.getKey()));
        }
    }
}
