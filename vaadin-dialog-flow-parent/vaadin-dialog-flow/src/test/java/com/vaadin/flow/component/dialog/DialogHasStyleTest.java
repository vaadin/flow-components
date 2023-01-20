/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.server.VaadinSession;

public class DialogHasStyleTest {

    private UI ui = new UI();
    private Dialog dialog;

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        dialog = new Dialog();
        ui.add(dialog);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addClassName_dialogHasOverlayClass() {
        dialog.addClassName("foo");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo");

        dialog.addClassName("bar");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo bar");
    }

    @Test
    public void removeClassName_dialogHasOverlayClass() {
        dialog.addClassName("foo");

        dialog.removeClassName("foo");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                null);
    }

    @Test
    public void setClassNameString_dialogHasOverlayClass() {
        dialog.setClassName("foo");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo");

        dialog.setClassName("bar");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "bar");
    }

    @Test
    public void setClassNameBoolean_dialogHasOverlayClass() {
        dialog.setClassName("foo", true);

        dialog.setClassName("foo", false);

        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                null);
    }

    @Test
    public void addClassNames_dialogHasOverlayClass() {
        dialog.addClassNames("foo", "bar");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo bar");

        dialog.addClassNames("baz", "qux");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo bar baz qux");
    }

    @Test
    public void removeClassNames_dialogHasOverlayClass() {
        dialog.addClassNames("foo", "bar", "baz", "qux");

        dialog.removeClassNames("foo", "bar");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "baz qux");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getStyle_unsupported() {
        dialog.getStyle();
    }

    @Test
    public void getClassNames_usesProxy() {
        Assert.assertTrue(
                dialog.getClassNames() instanceof OverlayClassListProxy);
    }
}
