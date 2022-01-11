/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
