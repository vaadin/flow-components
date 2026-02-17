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
package com.vaadin.flow.component.markdown.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class MarkdownSignalTest extends AbstractSignalsUnitTest {

    private Markdown markdown;
    private ValueSignal<String> contentSignal;

    @Before
    public void setup() {
        markdown = new Markdown();
        contentSignal = new ValueSignal<>("");
    }

    @After
    public void tearDown() {
        if (markdown != null && markdown.isAttached()) {
            markdown.removeFromParent();
        }
    }

    @Test
    public void bindContent_signalBound_contentSynchronizedWhenAttached() {
        markdown.bindContent(contentSignal);
        UI.getCurrent().add(markdown);

        Assert.assertEquals("", markdown.getContent());

        contentSignal.set("# Hello");
        Assert.assertEquals("# Hello", markdown.getContent());

        contentSignal.set("## World");
        Assert.assertEquals("## World", markdown.getContent());
    }

    @Test
    public void bindContent_signalBound_noEffectWhenDetached() {
        markdown.bindContent(contentSignal);
        // Not attached to UI

        String initial = markdown.getContent();
        contentSignal.set("# Hello");
        Assert.assertEquals(initial, markdown.getContent());
    }

    @Test
    public void bindContent_signalBound_detachAndReattach() {
        markdown.bindContent(contentSignal);
        UI.getCurrent().add(markdown);
        Assert.assertEquals("", markdown.getContent());

        // Detach
        markdown.removeFromParent();
        contentSignal.set("# Hello");
        Assert.assertEquals("", markdown.getContent());

        // Reattach
        UI.getCurrent().add(markdown);
        Assert.assertEquals("# Hello", markdown.getContent());

        contentSignal.set("## World");
        Assert.assertEquals("## World", markdown.getContent());
    }

    @Test(expected = BindingActiveException.class)
    public void bindContent_setContentWhileBound_throwsException() {
        markdown.bindContent(contentSignal);
        UI.getCurrent().add(markdown);

        markdown.setContent("manual");
    }

    @Test(expected = BindingActiveException.class)
    public void bindContent_appendContentWhileBound_throwsException() {
        markdown.bindContent(contentSignal);
        UI.getCurrent().add(markdown);

        markdown.appendContent(" appended");
    }

    @Test(expected = BindingActiveException.class)
    public void bindContent_bindAgainWhileBound_throwsException() {
        markdown.bindContent(contentSignal);
        UI.getCurrent().add(markdown);

        ValueSignal<String> anotherSignal = new ValueSignal<>("other");
        markdown.bindContent(anotherSignal);
    }
}
