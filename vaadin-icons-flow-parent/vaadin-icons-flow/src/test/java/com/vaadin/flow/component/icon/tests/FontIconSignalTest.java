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
import com.vaadin.flow.component.icon.FontIcon;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class FontIconSignalTest extends AbstractSignalsTest {

    private FontIcon fontIcon;
    private ValueSignal<String> ligatureSignal;
    private ValueSignal<String> charCodeSignal;

    @BeforeEach
    void setup() {
        fontIcon = new FontIcon();
        ligatureSignal = new ValueSignal<>("home");
        charCodeSignal = new ValueSignal<>("e001");
    }

    @AfterEach
    void tearDown() {
        if (fontIcon != null && fontIcon.isAttached()) {
            fontIcon.removeFromParent();
        }
    }

    // ===== LIGATURE BINDING TESTS =====

    @Test
    void bindLigature_signalBound_ligatureSynchronizedWhenAttached() {
        fontIcon.bindLigature(ligatureSignal);
        UI.getCurrent().add(fontIcon);

        Assertions.assertEquals("home", fontIcon.getLigature());

        ligatureSignal.set("settings");
        Assertions.assertEquals("settings", fontIcon.getLigature());

        ligatureSignal.set("search");
        Assertions.assertEquals("search", fontIcon.getLigature());
    }

    @Test
    void bindLigature_signalBound_noEffectWhenDetached() {
        fontIcon.bindLigature(ligatureSignal);
        // Not attached to UI

        String initialLigature = fontIcon.getLigature();
        ligatureSignal.set("settings");
        Assertions.assertEquals(initialLigature, fontIcon.getLigature());
    }

    @Test
    void bindLigature_signalBound_detachAndReattach() {
        fontIcon.bindLigature(ligatureSignal);
        UI.getCurrent().add(fontIcon);
        Assertions.assertEquals("home", fontIcon.getLigature());

        // Detach
        fontIcon.removeFromParent();
        ligatureSignal.set("settings");
        Assertions.assertEquals("home", fontIcon.getLigature());

        // Reattach
        UI.getCurrent().add(fontIcon);
        Assertions.assertEquals("settings", fontIcon.getLigature());

        ligatureSignal.set("search");
        Assertions.assertEquals("search", fontIcon.getLigature());
    }

    @Test
    void bindLigature_setLigatureWhileBound_throwsException() {
        fontIcon.bindLigature(ligatureSignal);
        UI.getCurrent().add(fontIcon);

        Assertions.assertThrows(BindingActiveException.class,
                () -> fontIcon.setLigature("settings"));
    }

    @Test
    void bindLigature_bindAgainWhileBound_throwsException() {
        fontIcon.bindLigature(ligatureSignal);
        UI.getCurrent().add(fontIcon);

        ValueSignal<String> anotherSignal = new ValueSignal<>("settings");
        Assertions.assertThrows(BindingActiveException.class,
                () -> fontIcon.bindLigature(anotherSignal));
    }

    // ===== CHAR CODE BINDING TESTS =====

    @Test
    void bindCharCode_signalBound_charCodeSynchronizedWhenAttached() {
        fontIcon.bindCharCode(charCodeSignal);
        UI.getCurrent().add(fontIcon);

        Assertions.assertEquals("e001", fontIcon.getCharCode());

        charCodeSignal.set("e002");
        Assertions.assertEquals("e002", fontIcon.getCharCode());

        charCodeSignal.set("e003");
        Assertions.assertEquals("e003", fontIcon.getCharCode());
    }

    @Test
    void bindCharCode_signalBound_noEffectWhenDetached() {
        fontIcon.bindCharCode(charCodeSignal);
        // Not attached to UI

        String initialCharCode = fontIcon.getCharCode();
        charCodeSignal.set("e002");
        Assertions.assertEquals(initialCharCode, fontIcon.getCharCode());
    }

    @Test
    void bindCharCode_signalBound_detachAndReattach() {
        fontIcon.bindCharCode(charCodeSignal);
        UI.getCurrent().add(fontIcon);
        Assertions.assertEquals("e001", fontIcon.getCharCode());

        // Detach
        fontIcon.removeFromParent();
        charCodeSignal.set("e002");
        Assertions.assertEquals("e001", fontIcon.getCharCode());

        // Reattach
        UI.getCurrent().add(fontIcon);
        Assertions.assertEquals("e002", fontIcon.getCharCode());

        charCodeSignal.set("e003");
        Assertions.assertEquals("e003", fontIcon.getCharCode());
    }

    @Test
    void bindCharCode_setCharCodeWhileBound_throwsException() {
        fontIcon.bindCharCode(charCodeSignal);
        UI.getCurrent().add(fontIcon);

        Assertions.assertThrows(BindingActiveException.class,
                () -> fontIcon.setCharCode("e002"));
    }

    @Test
    void bindCharCode_bindAgainWhileBound_throwsException() {
        fontIcon.bindCharCode(charCodeSignal);
        UI.getCurrent().add(fontIcon);

        ValueSignal<String> anotherSignal = new ValueSignal<>("e002");
        Assertions.assertThrows(BindingActiveException.class,
                () -> fontIcon.bindCharCode(anotherSignal));
    }
}
