/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.icon.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class IconSignalTest extends AbstractSignalsTest {

    private Icon icon;
    private ValueSignal<VaadinIcon> iconSignal;

    @BeforeEach
    void setup() {
        icon = new Icon();
        iconSignal = new ValueSignal<>(VaadinIcon.HOME);
    }

    @AfterEach
    void tearDown() {
        if (icon != null && icon.isAttached()) {
            icon.removeFromParent();
        }
    }

    // ===== ICON BINDING TESTS =====

    @Test
    void bindIcon_signalBound_iconSynchronizedWhenAttached() {
        icon.bindIcon(iconSignal);
        UI.getCurrent().add(icon);

        Assertions.assertEquals("vaadin:home", icon.getIcon());

        iconSignal.set(VaadinIcon.SEARCH);
        Assertions.assertEquals("vaadin:search", icon.getIcon());

        iconSignal.set(VaadinIcon.USER);
        Assertions.assertEquals("vaadin:user", icon.getIcon());
    }

    @Test
    void bindIcon_signalBound_noEffectWhenDetached() {
        icon.bindIcon(iconSignal);
        // Not attached to UI

        String initialIcon = icon.getIcon();
        iconSignal.set(VaadinIcon.SEARCH);
        Assertions.assertEquals(initialIcon, icon.getIcon());
    }

    @Test
    void bindIcon_signalBound_detachAndReattach() {
        icon.bindIcon(iconSignal);
        UI.getCurrent().add(icon);
        Assertions.assertEquals("vaadin:home", icon.getIcon());

        // Detach
        icon.removeFromParent();
        iconSignal.set(VaadinIcon.SEARCH);
        Assertions.assertEquals("vaadin:home", icon.getIcon());

        // Reattach
        UI.getCurrent().add(icon);
        Assertions.assertEquals("vaadin:search", icon.getIcon());

        iconSignal.set(VaadinIcon.USER);
        Assertions.assertEquals("vaadin:user", icon.getIcon());
    }

    @Test
    void bindIcon_setIconWhileBound_throwsException() {
        icon.bindIcon(iconSignal);
        UI.getCurrent().add(icon);

        Assertions.assertThrows(BindingActiveException.class,
                () -> icon.setIcon(VaadinIcon.SEARCH));
    }

    @Test
    void bindIcon_bindAgainWhileBound_throwsException() {
        icon.bindIcon(iconSignal);
        UI.getCurrent().add(icon);

        ValueSignal<VaadinIcon> anotherSignal = new ValueSignal<>(
                VaadinIcon.SEARCH);
        Assertions.assertThrows(BindingActiveException.class,
                () -> icon.bindIcon(anotherSignal));
    }

    @Test
    void constructor_withSignal_bindsIconCorrectly() {
        icon = new Icon(iconSignal);
        UI.getCurrent().add(icon);

        Assertions.assertEquals("vaadin:home", icon.getIcon());

        iconSignal.set(VaadinIcon.SEARCH);
        Assertions.assertEquals("vaadin:search", icon.getIcon());
    }
}
