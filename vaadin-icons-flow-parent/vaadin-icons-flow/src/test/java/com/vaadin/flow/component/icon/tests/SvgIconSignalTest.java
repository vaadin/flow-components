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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class SvgIconSignalTest extends AbstractSignalsUnitTest {

    private SvgIcon svgIcon;
    private ValueSignal<String> symbolSignal;

    @Before
    public void setup() {
        svgIcon = new SvgIcon();
        symbolSignal = new ValueSignal<>("icon-home");
    }

    @After
    public void tearDown() {
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
    public void bindSymbol_signalBound_symbolSynchronizedWhenAttached() {
        svgIcon.bindSymbol(symbolSignal);
        UI.getCurrent().add(svgIcon);

        Assert.assertEquals("icon-home", svgIcon.getSymbol());

        symbolSignal.value("icon-search");
        Assert.assertEquals("icon-search", svgIcon.getSymbol());

        symbolSignal.value("icon-user");
        Assert.assertEquals("icon-user", svgIcon.getSymbol());
    }

    @Test
    public void bindSymbol_signalBound_noEffectWhenDetached() {
        svgIcon.bindSymbol(symbolSignal);
        // Not attached to UI

        String initialSymbol = svgIcon.getSymbol();
        symbolSignal.value("icon-search");
        Assert.assertEquals(initialSymbol, svgIcon.getSymbol());
    }

    @Test
    public void bindSymbol_signalBound_detachAndReattach() {
        svgIcon.bindSymbol(symbolSignal);
        UI.getCurrent().add(svgIcon);
        Assert.assertEquals("icon-home", svgIcon.getSymbol());

        // Detach
        svgIcon.removeFromParent();
        symbolSignal.value("icon-search");
        Assert.assertEquals("icon-home", svgIcon.getSymbol());

        // Reattach
        UI.getCurrent().add(svgIcon);
        Assert.assertEquals("icon-search", svgIcon.getSymbol());

        symbolSignal.value("icon-user");
        Assert.assertEquals("icon-user", svgIcon.getSymbol());
    }

    @Test(expected = BindingActiveException.class)
    public void bindSymbol_setSymbolWhileBound_throwsException() {
        svgIcon.bindSymbol(symbolSignal);
        UI.getCurrent().add(svgIcon);

        svgIcon.setSymbol("icon-search");
    }

    @Test(expected = BindingActiveException.class)
    public void bindSymbol_bindAgainWhileBound_throwsException() {
        svgIcon.bindSymbol(symbolSignal);
        UI.getCurrent().add(svgIcon);

        ValueSignal<String> anotherSignal = new ValueSignal<>("icon-search");
        svgIcon.bindSymbol(anotherSignal);
    }

    @Test
    public void constructor_stringWithSignal_bindsSymbolCorrectly() {
        svgIcon = new SvgIcon("path/to/sprite.svg", symbolSignal);
        UI.getCurrent().add(svgIcon);

        Assert.assertEquals("path/to/sprite.svg", svgIcon.getSrc());
        Assert.assertEquals("icon-home", svgIcon.getSymbol());

        symbolSignal.value("icon-search");
        Assert.assertEquals("icon-search", svgIcon.getSymbol());
    }

    @Test
    public void constructor_downloadHandlerWithSignal_bindsSymbolCorrectly() {
        DownloadHandler handler = getDownloadHandler();
        svgIcon = new SvgIcon(handler, symbolSignal);
        UI.getCurrent().add(svgIcon);

        Assert.assertEquals("icon-home", svgIcon.getSymbol());

        symbolSignal.value("icon-search");
        Assert.assertEquals("icon-search", svgIcon.getSymbol());
    }
}
