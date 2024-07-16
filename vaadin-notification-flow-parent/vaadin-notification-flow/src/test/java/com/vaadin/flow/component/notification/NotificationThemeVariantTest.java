/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.notification;

import org.junit.Assert;
import org.junit.Test;

public class NotificationThemeVariantTest {

    private Notification notification = new Notification();

    @Test
    public void addAndRemoveVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        assertThemeAttribute("error");
        notification.removeThemeVariants(NotificationVariant.LUMO_ERROR);
        assertThemeAttribute(null);
    }

    private void assertThemeAttribute(String expected) {
        String theme = notification.getElement().getAttribute("theme");
        Assert.assertEquals("Unexpected theme attribute on notification",
                expected, theme);
    }
}
