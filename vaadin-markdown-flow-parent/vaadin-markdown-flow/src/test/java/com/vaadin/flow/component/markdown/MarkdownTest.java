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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.js.JsCall;
import com.vaadin.tests.MockUIExtension;

class MarkdownTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private Markdown markdown;

    @BeforeEach
    void setup() {
        markdown = new Markdown();
        ui.add(markdown);
    }

    @Test
    void testInitialMarkdownContent() {
        Assertions.assertNull(markdown.getContent());
    }

    @Test
    void testSetContent() {
        markdown.setContent("**Hello** _World_");
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);
    }

    @Test
    void testGetMarkdown() {
        markdown.setContent("**Hello** _World_");
        Assertions.assertEquals("**Hello** _World_", markdown.getContent());
    }

    @Test
    void overloadedConstructor_testGetMarkdown() {
        markdown = new Markdown("**Hello** _World_");
        Assertions.assertEquals("**Hello** _World_", markdown.getContent());
    }

    @Test
    void testAppendContent() {
        markdown.appendContent("**Hello**");
        Assertions.assertEquals("**Hello**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Hello**", false);

        markdown.appendContent(" _World_");
        Assertions.assertEquals("**Hello** _World_", markdown.getContent());
        assertUpdateMarkdownCall(markdown, " _World_", true);
    }

    @Test
    void testAppendContentWithSetContent() {
        markdown.setContent("**Hello**");
        Assertions.assertEquals("**Hello**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Hello**", false);

        markdown.setContent("**Hello** _World_");
        Assertions.assertEquals("**Hello** _World_", markdown.getContent());
        assertUpdateMarkdownCall(markdown, " _World_", true);
    }

    @Test
    void testReplaceMarkdown() {
        markdown.setContent("**Hello**");
        Assertions.assertEquals("**Hello**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Hello**", false);

        markdown.setContent("**Foobar**");
        Assertions.assertEquals("**Foobar**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Foobar**", false);
    }

    @Test
    void testSetSameMarkdown() {
        markdown.setContent("**Hello** _World_");
        Assertions.assertEquals("**Hello** _World_", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);

        markdown.setContent("**Hello** _World_");
        Assertions.assertEquals("**Hello** _World_", markdown.getContent());
        Assertions.assertEquals(0,
                ui.dumpPendingJavaScriptInvocations().size());
    }

    @Test
    void testSetContentTwice() {
        markdown.setContent("**Foobar**");
        markdown.setContent("**Hello** _World_");
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);
    }

    @Test
    void testRemoveMarkdown() {
        markdown.setContent("**Hello** _World_");
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);

        markdown.setContent(null);
        Assertions.assertNull(markdown.getContent());
        assertUpdateMarkdownCall(markdown, null, false);
    }

    @Test
    void testDetach_setContent_attach() {
        markdown.removeFromParent();

        markdown.setContent("**Hello** _World_");
        Assertions.assertEquals(0,
                ui.dumpPendingJavaScriptInvocations().size());

        ui.add(markdown);
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);
        Assertions.assertEquals("**Hello** _World_", markdown.getContent());
    }

    @Test
    void testReAttachment_contentPreserved() {
        // Set content while attached
        markdown.setContent("**Hello** _World_");
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);

        // Detach the component
        markdown.removeFromParent();

        // Re-attach the component - content should be sent to client again
        ui.add(markdown);
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);
        Assertions.assertEquals("**Hello** _World_", markdown.getContent());
    }

    @Test
    void testLineBreaks() {
        Assertions.assertFalse(markdown.isLineBreaks());

        markdown.setLineBreaks(true);
        Assertions.assertTrue(markdown.isLineBreaks());
        Assertions.assertTrue(
                markdown.getElement().getProperty("lineBreaks", false));

        markdown.setLineBreaks(false);
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
