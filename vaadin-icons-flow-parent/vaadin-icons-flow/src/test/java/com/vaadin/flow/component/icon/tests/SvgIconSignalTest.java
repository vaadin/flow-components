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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class SvgIconSignalTest extends AbstractSignalsTest {

    private SvgIcon svgIcon;
    private ValueSignal<String> symbolSignal;

    @BeforeEach
    void setup() {
        svgIcon = new SvgIcon();
        symbolSignal = new ValueSignal<>("icon-home");
    }

    @AfterEach
    void tearDown() {
        if (svgIcon != null && svgIcon.isAttached()) {
            svgIcon.removeFromParent();
        }
    }

    private static DownloadHandler getDownloadHandler() {
        return downloadEvent -> {
            try {
                downloadEvent.getOutputStream()
                        .write("<svg></svg>".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    // ===== SYMBOL BINDING TESTS =====

    @Test
    void bindSymbol_signalBound_symbolSynchronizedWhenAttached() {
        svgIcon.bindSymbol(symbolSignal);
        UI.getCurrent().add(svgIcon);

        Assertions.assertEquals("icon-home", svgIcon.getSymbol());

        symbolSignal.set("icon-search");
        Assertions.assertEquals("icon-search", svgIcon.getSymbol());

        symbolSignal.set("icon-user");
        Assertions.assertEquals("icon-user", svgIcon.getSymbol());
    }

    @Test
    void bindSymbol_signalBound_noEffectWhenDetached() {
        svgIcon.bindSymbol(symbolSignal);
        // Not attached to UI

        String initialSymbol = svgIcon.getSymbol();
        symbolSignal.set("icon-search");
        Assertions.assertEquals(initialSymbol, svgIcon.getSymbol());
    }

    @Test
    void bindSymbol_signalBound_detachAndReattach() {
        svgIcon.bindSymbol(symbolSignal);
        UI.getCurrent().add(svgIcon);
        Assertions.assertEquals("icon-home", svgIcon.getSymbol());

        // Detach
        svgIcon.removeFromParent();
        symbolSignal.set("icon-search");
        Assertions.assertEquals("icon-home", svgIcon.getSymbol());

        // Reattach
        UI.getCurrent().add(svgIcon);
        Assertions.assertEquals("icon-search", svgIcon.getSymbol());

        symbolSignal.set("icon-user");
        Assertions.assertEquals("icon-user", svgIcon.getSymbol());
    }

    @Test
    void bindSymbol_setSymbolWhileBound_throwsException() {
        svgIcon.bindSymbol(symbolSignal);
        UI.getCurrent().add(svgIcon);

        Assertions.assertThrows(BindingActiveException.class,
                () -> svgIcon.setSymbol("icon-search"));
    }

    @Test
    void bindSymbol_bindAgainWhileBound_throwsException() {
        svgIcon.bindSymbol(symbolSignal);
        UI.getCurrent().add(svgIcon);

        ValueSignal<String> anotherSignal = new ValueSignal<>("icon-search");
        Assertions.assertThrows(BindingActiveException.class,
                () -> svgIcon.bindSymbol(anotherSignal));
    }

    @Test
    void constructor_stringWithSignal_bindsSymbolCorrectly() {
        svgIcon = new SvgIcon("path/to/sprite.svg", symbolSignal);
        UI.getCurrent().add(svgIcon);

        Assertions.assertEquals("path/to/sprite.svg", svgIcon.getSrc());
        Assertions.assertEquals("icon-home", svgIcon.getSymbol());

        symbolSignal.set("icon-search");
        Assertions.assertEquals("icon-search", svgIcon.getSymbol());
    }

    @Test
    void constructor_downloadHandlerWithSignal_bindsSymbolCorrectly() {
        DownloadHandler handler = getDownloadHandler();
        svgIcon = new SvgIcon(handler, symbolSignal);
        UI.getCurrent().add(svgIcon);

        Assertions.assertEquals("icon-home", svgIcon.getSymbol());

        symbolSignal.set("icon-search");
        Assertions.assertEquals("icon-search", svgIcon.getSymbol());
    }
}
