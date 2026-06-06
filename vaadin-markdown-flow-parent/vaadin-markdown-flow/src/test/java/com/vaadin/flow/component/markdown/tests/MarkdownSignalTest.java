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

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class MarkdownSignalTest extends AbstractSignalsTest {

    private Markdown markdown;
    private ValueSignal<String> contentSignal;
    private Signal<String> computedSignal;

    @BeforeEach
    void setup() {
        contentSignal = new ValueSignal<>("**foo**");
        computedSignal = Signal.computed(() -> contentSignal.get() + " bar");
    }

    @AfterEach
    void tearDown() {
        if (markdown != null && markdown.isAttached()) {
            markdown.removeFromParent();
        }
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
    void bindContent_setContent_throwsBindingActiveException() {
        markdown = new Markdown();
        markdown.bindContent(contentSignal);
        ui.add(markdown);

        Assertions.assertThrows(BindingActiveException.class,
                () -> markdown.setContent("imperative"));
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

        Assertions.assertEquals(0, getPendingJavaScriptInvocations().size());
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
        Assertions.assertEquals(0, getPendingJavaScriptInvocations().size());

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

    private void assertUpdateMarkdownCall(Component component, String content,
            boolean isAppend) {
        var pendingJavaScriptInvocations = getPendingJavaScriptInvocations();

        Assertions.assertEquals(1, pendingJavaScriptInvocations.size());

        var pendingJavaScriptInvocation = pendingJavaScriptInvocations.get(0);
        var parameters = pendingJavaScriptInvocation.getInvocation()
                .getParameters();
        var element = component != null ? component.getElement() : null;

        if (isAppend) {
            Assertions.assertEquals(
                    "return (async function() { this.content += $0}).apply($1)",
                    pendingJavaScriptInvocation.getInvocation()
                            .getExpression());
        } else {
            Assertions.assertEquals(
                    "return (async function() { this.content = $0}).apply($1)",
                    pendingJavaScriptInvocation.getInvocation()
                            .getExpression());
        }
        Assertions.assertEquals(content, parameters.get(0));
        Assertions.assertEquals(element, parameters.get(1));
    }

    private List<PendingJavaScriptInvocation> getPendingJavaScriptInvocations() {
        return ui.dumpPendingJavaScriptInvocations();
    }
}
