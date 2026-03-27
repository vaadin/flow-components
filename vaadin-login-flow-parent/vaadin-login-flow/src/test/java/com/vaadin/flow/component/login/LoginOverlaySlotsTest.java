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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.tests.MockUIExtension;

class LoginOverlaySlotsTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private LoginOverlay overlay;

    @Tag("div")
    private static class TestComponent extends Component {
    }

    @BeforeEach
    void setup() {
        overlay = new LoginOverlay();
        ui.add(overlay);
    }

    @Test
    void customFormArea_addBeforeOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);

        Assertions.assertEquals(foo.getElement().getParent(),
                overlay.getElement());
        Assertions.assertEquals(bar.getElement().getParent(),
                overlay.getElement());
        Assertions.assertEquals("custom-form-area",
                foo.getElement().getAttribute("slot"));
        Assertions.assertEquals("custom-form-area",
                bar.getElement().getAttribute("slot"));
    }

    @Test
    void customFormArea_addAfterOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.setOpened(true);
        overlay.getCustomFormArea().add(foo, bar);

        Assertions.assertEquals(foo.getElement().getParent(),
                overlay.getElement());
        Assertions.assertEquals(bar.getElement().getParent(),
                overlay.getElement());
        Assertions.assertEquals("custom-form-area",
                foo.getElement().getAttribute("slot"));
        Assertions.assertEquals("custom-form-area",
                bar.getElement().getAttribute("slot"));
    }

    @Test
    void customFormArea_removeBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);

        overlay.getCustomFormArea().remove(foo);
        Assertions.assertEquals(null, foo.getElement().getParent());
        Assertions.assertNotEquals("custom-form-area",
                foo.getElement().getAttribute("slot"));

        overlay.getCustomFormArea().remove(bar);
        Assertions.assertEquals(null, bar.getElement().getParent());
        Assertions.assertNotEquals("custom-form-area",
                bar.getElement().getAttribute("slot"));
    }

    @Test
    void customFormArea_removeAfterOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);
        overlay.setOpened(true);

        overlay.getCustomFormArea().remove(foo);
        Assertions.assertEquals(null, foo.getElement().getParent());
        Assertions.assertNotEquals("custom-form-area",
                foo.getElement().getAttribute("slot"));
    }

    @Test
    void customFormArea_removeAllBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);

        overlay.getCustomFormArea().removeAll();

        Assertions.assertEquals(null, foo.getElement().getParent());
        Assertions.assertEquals(null, bar.getElement().getParent());
    }

    @Test
    void customFormArea_removeAllAfterOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);
        overlay.setOpened(true);

        overlay.getCustomFormArea().removeAll();

        Assertions.assertEquals(null, foo.getElement().getParent());
        Assertions.assertEquals(null, bar.getElement().getParent());
    }

    @Test
    void footer_addBeforeOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);

        Assertions.assertEquals(foo.getElement().getParent(),
                overlay.getElement());
        Assertions.assertEquals(bar.getElement().getParent(),
                overlay.getElement());
        Assertions.assertEquals("footer",
                foo.getElement().getAttribute("slot"));
        Assertions.assertEquals("footer",
                bar.getElement().getAttribute("slot"));
    }

    @Test
    void footer_addAfterOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.setOpened(true);
        overlay.getFooter().add(foo, bar);

        Assertions.assertEquals(foo.getElement().getParent(),
                overlay.getElement());
        Assertions.assertEquals(bar.getElement().getParent(),
                overlay.getElement());
        Assertions.assertEquals("footer",
                foo.getElement().getAttribute("slot"));
        Assertions.assertEquals("footer",
                bar.getElement().getAttribute("slot"));
    }

    @Test
    void footer_removeBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);

        overlay.getFooter().remove(foo);
        Assertions.assertEquals(null, foo.getElement().getParent());
        Assertions.assertNotEquals("footer",
                foo.getElement().getAttribute("slot"));

        overlay.getFooter().remove(bar);
        Assertions.assertEquals(null, bar.getElement().getParent());
    }

    @Test
    void footer_removeAfterOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);
        overlay.setOpened(true);

        overlay.getFooter().remove(foo);

        Assertions.assertEquals(null, foo.getElement().getParent());
        Assertions.assertNotEquals("footer",
                foo.getElement().getAttribute("slot"));
    }

    @Test
    void footer_removeAllBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);

        overlay.getFooter().removeAll();

        Assertions.assertEquals(null, foo.getElement().getParent());
        Assertions.assertEquals(null, bar.getElement().getParent());
    }

    @Test
    void footer_removeAllAfterOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);
        overlay.setOpened(true);

        overlay.getFooter().removeAll();

        Assertions.assertEquals(null, foo.getElement().getParent());
        Assertions.assertEquals(null, bar.getElement().getParent());
    }
}
