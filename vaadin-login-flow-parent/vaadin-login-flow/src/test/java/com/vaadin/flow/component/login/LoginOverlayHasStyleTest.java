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
package com.vaadin.flow.component.login;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.server.VaadinSession;

public class LoginOverlayHasStyleTest {

    private UI ui = new UI();
    private LoginOverlay overlay;

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        overlay = new LoginOverlay();
        ui.add(overlay);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addClassName_overlayHasOverlayClass() {
        overlay.addClassName("foo");
        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                "foo");

        overlay.addClassName("bar");
        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                "foo bar");
    }

    @Test
    public void removeClassName_overlayHasOverlayClass() {
        overlay.addClassName("foo");

        overlay.removeClassName("foo");
        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                null);
    }

    @Test
    public void setClassNameString_overlayHasOverlayClass() {
        overlay.setClassName("foo");
        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                "foo");

        overlay.setClassName("bar");
        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                "bar");
    }

    @Test
    public void setClassNameBoolean_overlayHasOverlayClass() {
        overlay.setClassName("foo", true);

        overlay.setClassName("foo", false);

        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                null);
    }

    @Test
    public void setClassNameMultiple_overlayHasOverlayClass() {
        overlay.setClassName("foo bar");
        overlay.getClassNames().set("foo", false);

        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                "bar");
    }

    @Test
    public void addClassNames_overlayHasOverlayClass() {
        overlay.addClassNames("foo", "bar");
        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                "foo bar");

        overlay.addClassNames("baz", "qux");
        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                "foo bar baz qux");
    }

    @Test
    public void removeClassNames_overlayHasOverlayClass() {
        overlay.addClassNames("foo", "bar", "baz", "qux");

        overlay.removeClassNames("foo", "bar");
        Assert.assertEquals(overlay.getElement().getProperty("overlayClass"),
                "baz qux");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getStyle_unsupported() {
        overlay.getStyle();
    }

    @Test
    public void getClassNames_usesProxy() {
        Assert.assertTrue(
                overlay.getClassNames() instanceof OverlayClassListProxy);
    }
}
