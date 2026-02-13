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
import com.vaadin.flow.component.icon.FontIcon;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class FontIconSignalTest extends AbstractSignalsUnitTest {

    private FontIcon fontIcon;
    private ValueSignal<String> ligatureSignal;
    private ValueSignal<String> charCodeSignal;

    @Before
    public void setup() {
        fontIcon = new FontIcon();
        ligatureSignal = new ValueSignal<>("home");
        charCodeSignal = new ValueSignal<>("e001");
    }

    @After
    public void tearDown() {
        if (fontIcon != null && fontIcon.isAttached()) {
            fontIcon.removeFromParent();
        }
    }

    // ===== LIGATURE BINDING TESTS =====

    @Test
    public void bindLigature_signalBound_ligatureSynchronizedWhenAttached() {
        fontIcon.bindLigature(ligatureSignal);
        UI.getCurrent().add(fontIcon);

        Assert.assertEquals("home", fontIcon.getLigature());

        ligatureSignal.set("settings");
        Assert.assertEquals("settings", fontIcon.getLigature());

        ligatureSignal.set("search");
        Assert.assertEquals("search", fontIcon.getLigature());
    }

    @Test
    public void bindLigature_signalBound_noEffectWhenDetached() {
        fontIcon.bindLigature(ligatureSignal);
        // Not attached to UI

        String initialLigature = fontIcon.getLigature();
        ligatureSignal.set("settings");
        Assert.assertEquals(initialLigature, fontIcon.getLigature());
    }

    @Test
    public void bindLigature_signalBound_detachAndReattach() {
        fontIcon.bindLigature(ligatureSignal);
        UI.getCurrent().add(fontIcon);
        Assert.assertEquals("home", fontIcon.getLigature());

        // Detach
        fontIcon.removeFromParent();
        ligatureSignal.set("settings");
        Assert.assertEquals("home", fontIcon.getLigature());

        // Reattach
        UI.getCurrent().add(fontIcon);
        Assert.assertEquals("settings", fontIcon.getLigature());

        ligatureSignal.set("search");
        Assert.assertEquals("search", fontIcon.getLigature());
    }

    @Test(expected = BindingActiveException.class)
    public void bindLigature_setLigatureWhileBound_throwsException() {
        fontIcon.bindLigature(ligatureSignal);
        UI.getCurrent().add(fontIcon);

        fontIcon.setLigature("settings");
    }

    @Test(expected = BindingActiveException.class)
    public void bindLigature_bindAgainWhileBound_throwsException() {
        fontIcon.bindLigature(ligatureSignal);
        UI.getCurrent().add(fontIcon);

        ValueSignal<String> anotherSignal = new ValueSignal<>("settings");
        fontIcon.bindLigature(anotherSignal);
    }

    // ===== CHAR CODE BINDING TESTS =====

    @Test
    public void bindCharCode_signalBound_charCodeSynchronizedWhenAttached() {
        fontIcon.bindCharCode(charCodeSignal);
        UI.getCurrent().add(fontIcon);

        Assert.assertEquals("e001", fontIcon.getCharCode());

        charCodeSignal.set("e002");
        Assert.assertEquals("e002", fontIcon.getCharCode());

        charCodeSignal.set("e003");
        Assert.assertEquals("e003", fontIcon.getCharCode());
    }

    @Test
    public void bindCharCode_signalBound_noEffectWhenDetached() {
        fontIcon.bindCharCode(charCodeSignal);
        // Not attached to UI

        String initialCharCode = fontIcon.getCharCode();
        charCodeSignal.set("e002");
        Assert.assertEquals(initialCharCode, fontIcon.getCharCode());
    }

    @Test
    public void bindCharCode_signalBound_detachAndReattach() {
        fontIcon.bindCharCode(charCodeSignal);
        UI.getCurrent().add(fontIcon);
        Assert.assertEquals("e001", fontIcon.getCharCode());

        // Detach
        fontIcon.removeFromParent();
        charCodeSignal.set("e002");
        Assert.assertEquals("e001", fontIcon.getCharCode());

        // Reattach
        UI.getCurrent().add(fontIcon);
        Assert.assertEquals("e002", fontIcon.getCharCode());

        charCodeSignal.set("e003");
        Assert.assertEquals("e003", fontIcon.getCharCode());
    }

    @Test(expected = BindingActiveException.class)
    public void bindCharCode_setCharCodeWhileBound_throwsException() {
        fontIcon.bindCharCode(charCodeSignal);
        UI.getCurrent().add(fontIcon);

        fontIcon.setCharCode("e002");
    }

    @Test(expected = BindingActiveException.class)
    public void bindCharCode_bindAgainWhileBound_throwsException() {
        fontIcon.bindCharCode(charCodeSignal);
        UI.getCurrent().add(fontIcon);

        ValueSignal<String> anotherSignal = new ValueSignal<>("e002");
        fontIcon.bindCharCode(anotherSignal);
    }
}
