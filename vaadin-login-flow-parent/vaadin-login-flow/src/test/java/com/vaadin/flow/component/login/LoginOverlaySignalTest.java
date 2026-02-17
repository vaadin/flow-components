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
package com.vaadin.flow.component.login;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class LoginOverlaySignalTest extends AbstractSignalsUnitTest {

    private LoginOverlay loginOverlay;
    private ValueSignal<Boolean> openedSignal;

    @Before
    public void setup() {
        loginOverlay = new LoginOverlay();
        openedSignal = new ValueSignal<>(false);
    }

    @After
    public void tearDown() {
        if (loginOverlay != null && loginOverlay.isAttached()) {
            loginOverlay.removeFromParent();
        }
    }

    @Test
    public void bindOpened_signalBound_openedSynchronizedWhenAttached() {
        loginOverlay.bindOpened(openedSignal);
        UI.getCurrent().add(loginOverlay);

        Assert.assertFalse(
                loginOverlay.getElement().getProperty("opened", false));

        openedSignal.set(true);
        Assert.assertTrue(
                loginOverlay.getElement().getProperty("opened", false));

        openedSignal.set(false);
        Assert.assertFalse(
                loginOverlay.getElement().getProperty("opened", false));
    }

    @Test
    public void bindOpened_signalBound_noEffectWhenDetached() {
        loginOverlay.bindOpened(openedSignal);
        // Not attached to UI

        boolean initial = loginOverlay.getElement().getProperty("opened",
                false);
        openedSignal.set(true);
        Assert.assertEquals(initial,
                loginOverlay.getElement().getProperty("opened", false));
    }

    @Test
    public void bindOpened_signalBound_detachAndReattach() {
        loginOverlay.bindOpened(openedSignal);
        UI.getCurrent().add(loginOverlay);
        Assert.assertFalse(
                loginOverlay.getElement().getProperty("opened", false));

        // Detach
        loginOverlay.removeFromParent();
        openedSignal.set(true);
        Assert.assertFalse(
                loginOverlay.getElement().getProperty("opened", false));

        // Reattach
        UI.getCurrent().add(loginOverlay);
        Assert.assertTrue(
                loginOverlay.getElement().getProperty("opened", false));

        openedSignal.set(false);
        Assert.assertFalse(
                loginOverlay.getElement().getProperty("opened", false));
    }

    @Test(expected = NullPointerException.class)
    public void bindOpened_nullSignal_throwsNPE() {
        loginOverlay.bindOpened(null);
    }

    @Test(expected = BindingActiveException.class)
    public void bindOpened_setOpenedWhileBound_throwsException() {
        loginOverlay.bindOpened(openedSignal);
        UI.getCurrent().add(loginOverlay);

        loginOverlay.setOpened(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindOpened_bindAgainWhileBound_throwsException() {
        loginOverlay.bindOpened(openedSignal);
        UI.getCurrent().add(loginOverlay);

        loginOverlay.bindOpened(new ValueSignal<>(true));
    }
}
