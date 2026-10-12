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
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class SvgIconSignalTest extends AbstractSignalsTest {

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

    @TestFactory
    Stream<DynamicTest> bindSymbol() {
        return generateBindingTests(SvgIcon::new, SvgIcon::bindSymbol,
                SvgIcon::getSymbol, SvgIcon::setSymbol, "icon-home",
                "icon-search");
    }

    @Test
    void constructor_stringWithSignal_bindsSymbolCorrectly() {
        var symbolSignal = new ValueSignal<>("icon-home");
        var svgIcon = new SvgIcon("path/to/sprite.svg", symbolSignal);
        UI.getCurrent().add(svgIcon);

        Assertions.assertEquals("path/to/sprite.svg", svgIcon.getSrc());
        Assertions.assertEquals("icon-home", svgIcon.getSymbol());

        symbolSignal.set("icon-search");
        Assertions.assertEquals("icon-search", svgIcon.getSymbol());
    }

    @Test
    void constructor_downloadHandlerWithSignal_bindsSymbolCorrectly() {
        var symbolSignal = new ValueSignal<>("icon-home");
        DownloadHandler handler = getDownloadHandler();
        var svgIcon = new SvgIcon(handler, symbolSignal);
        UI.getCurrent().add(svgIcon);

        Assertions.assertEquals("icon-home", svgIcon.getSymbol());

        symbolSignal.set("icon-search");
        Assertions.assertEquals("icon-search", svgIcon.getSymbol());
    }
}
