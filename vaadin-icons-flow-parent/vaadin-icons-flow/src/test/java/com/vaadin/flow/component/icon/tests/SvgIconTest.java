/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;

public class SvgIconTest {
    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void emptyConstructor_hasNoSrc() {
        var icon = new SvgIcon();
        Assert.assertNull(icon.getSrc());
        Assert.assertNull(icon.getElement().getAttribute("src"));
    }

    @Test
    public void sourceConstructor_hasSrc() {
        var path = "path/to/file.svg";
        var icon = new SvgIcon(path);
        Assert.assertEquals(path, icon.getSrc());
        Assert.assertEquals(path, icon.getElement().getAttribute("src"));
    }

    @Test
    public void sourceWithSymbolConstructor_hasSrcAndSymbol() {
        var path = "path/to/file.svg";
        var symbol = "symbol";
        var icon = new SvgIcon(path, symbol);
        Assert.assertEquals(path, icon.getSrc());
        Assert.assertEquals(path, icon.getElement().getAttribute("src"));
        Assert.assertEquals(symbol, icon.getSymbol());
        Assert.assertEquals(symbol, icon.getElement().getProperty("symbol"));
    }

    @Test
    public void streamResourceConstructor_hasSrc() {
        UI.setCurrent(new UI());
        var resource = getStreamResource();
        var icon = new SvgIcon(resource);
        Assert.assertTrue(icon.getSrc().contains("image.svg"));
        Assert.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
    }

    @Test
    public void streamResourceConstructorWithSymbol_hasSrcAndSymbol() {
        UI.setCurrent(new UI());
        var resource = getStreamResource();
        var symbol = "symbol";
        var icon = new SvgIcon(resource, symbol);
        Assert.assertTrue(icon.getSrc().contains("image.svg"));
        Assert.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
        Assert.assertEquals(symbol, icon.getSymbol());
        Assert.assertEquals(symbol, icon.getElement().getProperty("symbol"));
    }

    @Test
    public void downloadHandlerConstructor_hasSrc() {
        UI.setCurrent(new UI());
        var resource = getDownloadHandler();
        var icon = new SvgIcon(resource);
        Assert.assertTrue(icon.getSrc().contains("image.svg"));
        Assert.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
    }

    @Test
    public void downloadHandlerConstructorWithSymbol_hasSrcAndSymbol() {
        UI.setCurrent(new UI());
        var resource = getDownloadHandler();
        var symbol = "symbol";
        var icon = new SvgIcon(resource, symbol);
        Assert.assertTrue(icon.getSrc().contains("image.svg"));
        Assert.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
        Assert.assertEquals(symbol, icon.getSymbol());
        Assert.assertEquals(symbol, icon.getElement().getProperty("symbol"));
    }

    @Test
    public void setSrc_hasSrc() {
        var icon = new SvgIcon();
        var path = "path/to/file.svg";
        icon.setSrc(path);
        Assert.assertEquals(path, icon.getSrc());
        Assert.assertEquals(path, icon.getElement().getAttribute("src"));
    }

    @Test
    public void setSrcWithSymbol_hasSrcAndSymbol() {
        var icon = new SvgIcon();
        var path = "path/to/file.svg";
        var symbol = "symbol";
        icon.setSrc(path, symbol);

        Assert.assertEquals(path, icon.getSrc());
        Assert.assertEquals(path, icon.getElement().getAttribute("src"));
        Assert.assertEquals(symbol, icon.getSymbol());
        Assert.assertEquals(symbol, icon.getElement().getProperty("symbol"));
    }

    @Test
    public void hasStreamResource_setSrcWithSymbol_hasSrcAndSymbol() {
        UI.setCurrent(new UI());
        var resource = getStreamResource();
        var symbol = "symbol";
        var icon = new SvgIcon();
        icon.setSrc(resource, symbol);

        Assert.assertTrue(icon.getSrc().contains("image.svg"));
        Assert.assertTrue(
                icon.getElement().getAttribute("src").contains("image.svg"));
        Assert.assertEquals(symbol, icon.getSymbol());
        Assert.assertEquals(symbol, icon.getElement().getProperty("symbol"));
    }

    @Test
    public void setSymbol_hasSymbol() {
        var icon = new SvgIcon();
        var symbol = "symbol";
        icon.setSymbol(symbol);
        Assert.assertEquals(symbol, icon.getSymbol());
        Assert.assertEquals(symbol, icon.getElement().getProperty("symbol"));
    }

    @Test
    public void modifySrc_hasModifiedSrc() {
        var icon = new SvgIcon("path/to/file.svg");
        var newPath = "path/to/new/file.svg";
        icon.setSrc(newPath);

        Assert.assertEquals(newPath, icon.getSrc());
        Assert.assertEquals(newPath, icon.getElement().getAttribute("src"));
    }

    @Test
    public void withStreamResource_setSrc_hasSrc() {
        UI.setCurrent(new UI());
        var icon = new SvgIcon();
        var resource = getStreamResource();
        icon.setSrc(resource);
        Assert.assertTrue(icon.getSrc().contains("image.svg"));
    }

    @Test
    public void setColor_hasColor() {
        var icon = new SvgIcon();
        icon.setColor("red");
        Assert.assertEquals("red", icon.getColor());
        Assert.assertEquals("red", icon.getStyle().get("fill"));
    }

    @Test
    public void removeColor_hasNoColor() {
        var icon = new SvgIcon();
        icon.setColor("red");
        icon.setColor(null);
        Assert.assertNull(icon.getColor());
        Assert.assertNull(icon.getStyle().get("fill"));
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
