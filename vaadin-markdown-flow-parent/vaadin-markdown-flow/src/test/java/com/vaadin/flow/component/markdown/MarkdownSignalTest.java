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
package com.vaadin.flow.component.markdown;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class MarkdownSignalTest extends AbstractSignalsTest {

    private Markdown markdown;
    private ValueSignal<String> contentSignal;
    private Signal<String> computedSignal;
    private ValueSignal<Boolean> lineBreaksSignal;

    @BeforeEach
    void setup() {
        contentSignal = new ValueSignal<>("**foo**");
        computedSignal = Signal.computed(() -> contentSignal.get() + " bar");
        lineBreaksSignal = new ValueSignal<>(true);
    }

    @AfterEach
    void tearDown() {
        if (markdown != null && markdown.isAttached()) {
            markdown.removeFromParent();
        }
    }

    @TestFactory
    Stream<DynamicTest> bindContent() {
        return generateOneWayBindingTests(Markdown::new, Markdown::bindContent,
                Markdown::getContent, Markdown::setContent, "**foo**",
                "**bar**");
    }

    @TestFactory
    Stream<DynamicTest> bindLineBreaks() {
        return generateOneWayBindingTests(Markdown::new,
                Markdown::bindLineBreaks, Markdown::isLineBreaks,
                Markdown::setLineBreaks, true, false);
    }

    @Test
    void bindContent_signalDrivesContent() {
        markdown = new Markdown();
        markdown.bindContent(contentSignal);
        ui.add(markdown);

        Assertions.assertEquals("**foo**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**foo**", false);
    }

    @Test
    void bindContent_signalChanges_updateContent() {
        markdown = new Markdown();
        markdown.bindContent(contentSignal);
        ui.add(markdown);
        assertUpdateMarkdownCall(markdown, "**foo**", false);

        contentSignal.set("**foo** bar");
        Assertions.assertEquals("**foo** bar", markdown.getContent());
        assertUpdateMarkdownCall(markdown, " bar", true);

        contentSignal.set("**different**");
        Assertions.assertEquals("**different**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**different**", false);
    }

    @Test
    void signalConstructor_bindsContent() {
        markdown = new Markdown(contentSignal);
        ui.add(markdown);

        Assertions.assertEquals("**foo**", markdown.getContent());
        contentSignal.set("**bar**");
        Assertions.assertEquals("**bar**", markdown.getContent());
    }

    @Test
    void bindContent_appendContent_throwsBindingActiveException() {
        markdown = new Markdown();
        markdown.bindContent(contentSignal);
        ui.add(markdown);

        Assertions.assertThrows(BindingActiveException.class,
                () -> markdown.appendContent(" more"));
    }

    @Test
    void bindContent_notAttached_noUpdate() {
        markdown = new Markdown();
        markdown.bindContent(contentSignal);

        Assertions.assertEquals(0,
                ui.dumpPendingJavaScriptInvocations().size());
    }

    @Test
    void bindContent_detachAndAttach_reactivates() {
        markdown = new Markdown();
        markdown.bindContent(contentSignal);
        ui.add(markdown);
        assertUpdateMarkdownCall(markdown, "**foo**", false);

        markdown.removeFromParent();

        // Signal changes while detached must not produce JS invocations
        contentSignal.set("**while-detached**");
        Assertions.assertEquals(0,
                ui.dumpPendingJavaScriptInvocations().size());

        ui.add(markdown);
        Assertions.assertEquals("**while-detached**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**while-detached**", false);
    }

    @Test
    void bindContent_nullSignalValue_clearsContent() {
        markdown = new Markdown();
        markdown.bindContent(contentSignal);
        ui.add(markdown);
        assertUpdateMarkdownCall(markdown, "**foo**", false);

        contentSignal.set(null);
        Assertions.assertNull(markdown.getContent());
        assertUpdateMarkdownCall(markdown, null, false);
    }

    @Test
    void bindContent_computedSignal_drivesContent() {
        markdown = new Markdown();
        markdown.bindContent(computedSignal);
        ui.add(markdown);

        Assertions.assertEquals("**foo** bar", markdown.getContent());

        contentSignal.set("**baz**");
        Assertions.assertEquals("**baz** bar", markdown.getContent());
    }

    @Test
    void bindLineBreaks_nullSignalValue_lineBreaksDisabled() {
        markdown = new Markdown();
        markdown.bindLineBreaks(lineBreaksSignal);
        ui.add(markdown);

        lineBreaksSignal.set(null);
        Assertions.assertFalse(markdown.isLineBreaks());
    }

    private void assertUpdateMarkdownCall(Component component, String content,
            boolean isAppend) {
        List<JsCall> calls = ui.dumpJsCalls(component.getElement(),
                MarkdownJs.class);

        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals(isAppend ? "appendContent" : "setContent",
                calls.get(0).methodName());
        Assertions.assertEquals(Collections.singletonList(content),
                calls.get(0).arguments());
    }
}
