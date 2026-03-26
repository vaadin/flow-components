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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.tests.MockUIExtension;

class SvgIconTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Test
    void emptyConstructor_hasNoSrc() {
        var icon = new SvgIcon();
        Assertions.assertNull(icon.getSrc());
        Assertions.assertNull(icon.getElement().getAttribute("src"));
    }

    @Test
    void sourceConstructor_hasSrc() {
        var path = "path/to/file.svg";
        var icon = new SvgIcon(path);
        Assertions.assertEquals(path, icon.getSrc());
        Assertions.assertEquals(path, icon.getElement().getAttribute("src"));
    }

    @Test
    void sourceWithSymbolConstructor_hasSrcAndSymbol() {
        var path = "path/to/file.svg";
        var symbol = "symbol";
        var icon = new SvgIcon(path, symbol);
        Assertions.assertEquals(path, icon.getSrc());
        Assertions.assertEquals(path, icon.getElement().getAttribute("src"));
        Assertions.assertEquals(symbol, icon.getSymbol());
        Assertions.assertEquals(symbol,
                icon.getElement().getProperty("symbol"));
    }

    @Test
    void streamResourceConstructor_hasSrc() {
        var resource = getStreamResource();
        var icon = new SvgIcon(resource);
        Assertions.assertTrue(icon.getSrc().contains("image.svg"));
        Assertions.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
    }

    @Test
    void streamResourceConstructorWithSymbol_hasSrcAndSymbol() {
        var resource = getStreamResource();
        var symbol = "symbol";
        var icon = new SvgIcon(resource, symbol);
        Assertions.assertTrue(icon.getSrc().contains("image.svg"));
        Assertions.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
        Assertions.assertEquals(symbol, icon.getSymbol());
        Assertions.assertEquals(symbol,
                icon.getElement().getProperty("symbol"));
    }

    @Test
    void downloadHandlerConstructor_hasSrc() {
        var resource = getDownloadHandler();
        var icon = new SvgIcon(resource);
        Assertions.assertTrue(icon.getSrc().contains("image.svg"));
        Assertions.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
    }

    @Test
    void downloadHandlerConstructorWithSymbol_hasSrcAndSymbol() {
        var resource = getDownloadHandler();
        var symbol = "symbol";
        var icon = new SvgIcon(resource, symbol);
        Assertions.assertTrue(icon.getSrc().contains("image.svg"));
        Assertions.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
        Assertions.assertEquals(symbol, icon.getSymbol());
        Assertions.assertEquals(symbol,
                icon.getElement().getProperty("symbol"));
    }

    @Test
    void setSrc_hasSrc() {
        var icon = new SvgIcon();
        var path = "path/to/file.svg";
        icon.setSrc(path);
        Assertions.assertEquals(path, icon.getSrc());
        Assertions.assertEquals(path, icon.getElement().getAttribute("src"));
    }

    @Test
    void setSrcWithSymbol_hasSrcAndSymbol() {
        var icon = new SvgIcon();
        var path = "path/to/file.svg";
        var symbol = "symbol";
        icon.setSrc(path, symbol);

        Assertions.assertEquals(path, icon.getSrc());
        Assertions.assertEquals(path, icon.getElement().getAttribute("src"));
        Assertions.assertEquals(symbol, icon.getSymbol());
        Assertions.assertEquals(symbol,
                icon.getElement().getProperty("symbol"));
    }

    @Test
    void hasStreamResource_setSrcWithSymbol_hasSrcAndSymbol() {
        var resource = getStreamResource();
        var symbol = "symbol";
        var icon = new SvgIcon();
        icon.setSrc(resource, symbol);

        Assertions.assertTrue(icon.getSrc().contains("image.svg"));
        Assertions.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
        Assertions.assertEquals(symbol, icon.getSymbol());
        Assertions.assertEquals(symbol,
                icon.getElement().getProperty("symbol"));
    }

    @Test
    void setSymbol_hasSymbol() {
        var icon = new SvgIcon();
        var symbol = "symbol";
        icon.setSymbol(symbol);
        Assertions.assertEquals(symbol, icon.getSymbol());
        Assertions.assertEquals(symbol,
                icon.getElement().getProperty("symbol"));
    }

    @Test
    void modifySrc_hasModifiedSrc() {
        var icon = new SvgIcon("path/to/file.svg");
        var newPath = "path/to/new/file.svg";
        icon.setSrc(newPath);

        Assertions.assertEquals(newPath, icon.getSrc());
        Assertions.assertEquals(newPath, icon.getElement().getAttribute("src"));
    }

    @Test
    void withStreamResource_setSrc_hasSrc() {
        var icon = new SvgIcon();
        var resource = getStreamResource();
        icon.setSrc(resource);
        Assertions.assertTrue(icon.getSrc().contains("image.svg"));
    }

    @Test
    void setColor_hasColor() {
        var icon = new SvgIcon();
        icon.setColor("red");
        Assertions.assertEquals("red", icon.getColor());
        Assertions.assertEquals("red", icon.getStyle().get("fill"));
    }

    @Test
    void removeColor_hasNoColor() {
        var icon = new SvgIcon();
        icon.setColor("red");
        icon.setColor(null);
        Assertions.assertNull(icon.getColor());
        Assertions.assertNull(icon.getStyle().get("fill"));
    }

    private static StreamResource getStreamResource() {
        return new StreamResource("image.svg", () -> new ByteArrayInputStream(
                "<svg></svg>".getBytes(StandardCharsets.UTF_8)));
    }

    private static DownloadHandler getDownloadHandler() {
        return new DownloadHandler() {
            @Override
            public void handleDownloadRequest(DownloadEvent downloadEvent) {
                try {
                    downloadEvent.getOutputStream().write(
                            "<svg></svg>".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getUrlPostfix() {
                return "image.svg";
            }
        };
    }
}
