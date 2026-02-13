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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class IconSignalTest extends AbstractSignalsUnitTest {

    private Icon icon;
    private ValueSignal<VaadinIcon> iconSignal;

    @Before
    public void setup() {
        icon = new Icon();
        iconSignal = new ValueSignal<>(VaadinIcon.HOME);
    }

    @After
    public void tearDown() {
        if (icon != null && icon.isAttached()) {
            icon.removeFromParent();
        }
    }

    // ===== ICON BINDING TESTS =====

    @Test
    public void bindIcon_signalBound_iconSynchronizedWhenAttached() {
        icon.bindIcon(iconSignal);
        UI.getCurrent().add(icon);

        Assert.assertEquals("vaadin:home", icon.getIcon());

        iconSignal.set(VaadinIcon.SEARCH);
        Assert.assertEquals("vaadin:search", icon.getIcon());

        iconSignal.set(VaadinIcon.USER);
        Assert.assertEquals("vaadin:user", icon.getIcon());
    }

    @Test
    public void bindIcon_signalBound_noEffectWhenDetached() {
        icon.bindIcon(iconSignal);
        // Not attached to UI

        String initialIcon = icon.getIcon();
        iconSignal.set(VaadinIcon.SEARCH);
        Assert.assertEquals(initialIcon, icon.getIcon());
    }

    @Test
    public void bindIcon_signalBound_detachAndReattach() {
        icon.bindIcon(iconSignal);
        UI.getCurrent().add(icon);
        Assert.assertEquals("vaadin:home", icon.getIcon());

        // Detach
        icon.removeFromParent();
        iconSignal.set(VaadinIcon.SEARCH);
        Assert.assertEquals("vaadin:home", icon.getIcon());

        // Reattach
        UI.getCurrent().add(icon);
        Assert.assertEquals("vaadin:search", icon.getIcon());

        iconSignal.set(VaadinIcon.USER);
        Assert.assertEquals("vaadin:user", icon.getIcon());
    }

    @Test
    public void bindIcon_nullUnbindsSignal() {
        icon.bindIcon(iconSignal);
        UI.getCurrent().add(icon);
        Assert.assertEquals("vaadin:home", icon.getIcon());

        icon.bindIcon(null);
        iconSignal.set(VaadinIcon.SEARCH);
        Assert.assertEquals("vaadin:home", icon.getIcon());

        // Should be able to set manually after unbinding
        icon.setIcon(VaadinIcon.USER);
        Assert.assertEquals("vaadin:user", icon.getIcon());
    }

    @Test(expected = BindingActiveException.class)
    public void bindIcon_setIconWhileBound_throwsException() {
        icon.bindIcon(iconSignal);
        UI.getCurrent().add(icon);

        icon.setIcon(VaadinIcon.SEARCH);
    }

    @Test(expected = BindingActiveException.class)
    public void bindIcon_bindAgainWhileBound_throwsException() {
        icon.bindIcon(iconSignal);
        UI.getCurrent().add(icon);

        ValueSignal<VaadinIcon> anotherSignal = new ValueSignal<>(
                VaadinIcon.SEARCH);
        icon.bindIcon(anotherSignal);
    }

    @Test
    public void constructor_withSignal_bindsIconCorrectly() {
        icon = new Icon(iconSignal);
        UI.getCurrent().add(icon);

        Assert.assertEquals("vaadin:home", icon.getIcon());

        iconSignal.set(VaadinIcon.SEARCH);
        Assert.assertEquals("vaadin:search", icon.getIcon());
    }
}
