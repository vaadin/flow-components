/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.notification.tests;

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-notification/nested")
public class NestedNotificationIT extends AbstractComponentIT {

    @Test
    public void openNotification_shouldNotThrow() {
        open();

        findElement(By.id("open")).click();

        checkLogsForErrors();
    }

}
