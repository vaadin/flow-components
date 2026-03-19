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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.tests.MockUIRule;

public class LoginOverlaySlotsTest {
    @Rule
    public MockUIRule ui = new MockUIRule();

    private LoginOverlay overlay;

    @Tag("div")
    private static class TestComponent extends Component {
    }

    @Before
    public void setup() {
        overlay = new LoginOverlay();
        ui.add(overlay);
    }

    @Test
    public void customFormArea_addBeforeOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);

        Assert.assertEquals(foo.getElement().getParent(), overlay.getElement());
        Assert.assertEquals(bar.getElement().getParent(), overlay.getElement());
        Assert.assertEquals("custom-form-area",
                foo.getElement().getAttribute("slot"));
        Assert.assertEquals("custom-form-area",
                bar.getElement().getAttribute("slot"));
    }

    @Test
    public void customFormArea_addAfterOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.setOpened(true);
        overlay.getCustomFormArea().add(foo, bar);

        Assert.assertEquals(foo.getElement().getParent(), overlay.getElement());
        Assert.assertEquals(bar.getElement().getParent(), overlay.getElement());
        Assert.assertEquals("custom-form-area",
                foo.getElement().getAttribute("slot"));
        Assert.assertEquals("custom-form-area",
                bar.getElement().getAttribute("slot"));
    }

    @Test
    public void customFormArea_removeBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);

        overlay.getCustomFormArea().remove(foo);
        Assert.assertEquals(null, foo.getElement().getParent());
        Assert.assertNotEquals("custom-form-area",
                foo.getElement().getAttribute("slot"));

        overlay.getCustomFormArea().remove(bar);
        Assert.assertEquals(null, bar.getElement().getParent());
        Assert.assertNotEquals("custom-form-area",
                bar.getElement().getAttribute("slot"));
    }

    @Test
    public void customFormArea_removeAfterOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);
        overlay.setOpened(true);

        overlay.getCustomFormArea().remove(foo);
        Assert.assertEquals(null, foo.getElement().getParent());
        Assert.assertNotEquals("custom-form-area",
                foo.getElement().getAttribute("slot"));
    }

    @Test
    public void customFormArea_removeAllBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);

        overlay.getCustomFormArea().removeAll();

        Assert.assertEquals(null, foo.getElement().getParent());
        Assert.assertEquals(null, bar.getElement().getParent());
    }

    @Test
    public void customFormArea_removeAllAfterOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFormArea().add(foo, bar);
        overlay.setOpened(true);

        overlay.getCustomFormArea().removeAll();

        Assert.assertEquals(null, foo.getElement().getParent());
        Assert.assertEquals(null, bar.getElement().getParent());
    }

    @Test
    public void footer_addBeforeOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);

        Assert.assertEquals(foo.getElement().getParent(), overlay.getElement());
        Assert.assertEquals(bar.getElement().getParent(), overlay.getElement());
        Assert.assertEquals("footer", foo.getElement().getAttribute("slot"));
        Assert.assertEquals("footer", bar.getElement().getAttribute("slot"));
    }

    @Test
    public void footer_addAfterOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.setOpened(true);
        overlay.getFooter().add(foo, bar);

        Assert.assertEquals(foo.getElement().getParent(), overlay.getElement());
        Assert.assertEquals(bar.getElement().getParent(), overlay.getElement());
        Assert.assertEquals("footer", foo.getElement().getAttribute("slot"));
        Assert.assertEquals("footer", bar.getElement().getAttribute("slot"));
    }

    @Test
    public void footer_removeBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);

        overlay.getFooter().remove(foo);
        Assert.assertEquals(null, foo.getElement().getParent());
        Assert.assertNotEquals("footer", foo.getElement().getAttribute("slot"));

        overlay.getFooter().remove(bar);
        Assert.assertEquals(null, bar.getElement().getParent());
    }

    @Test
    public void footer_removeAfterOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);
        overlay.setOpened(true);

        overlay.getFooter().remove(foo);

        Assert.assertEquals(null, foo.getElement().getParent());
        Assert.assertNotEquals("footer", foo.getElement().getAttribute("slot"));
    }

    @Test
    public void footer_removeAllBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);

        overlay.getFooter().removeAll();

        Assert.assertEquals(null, foo.getElement().getParent());
        Assert.assertEquals(null, bar.getElement().getParent());
    }

    @Test
    public void footer_removeAllAfterOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);
        overlay.setOpened(true);

        overlay.getFooter().removeAll();

        Assert.assertEquals(null, foo.getElement().getParent());
        Assert.assertEquals(null, bar.getElement().getParent());
    }
}
