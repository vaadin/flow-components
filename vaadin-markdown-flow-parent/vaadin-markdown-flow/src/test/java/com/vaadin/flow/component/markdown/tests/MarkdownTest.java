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
package com.vaadin.flow.component.markdown.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.server.VaadinSession;

public class MarkdownTest {

    private final UI ui = new UI();
    private Markdown markdown;

    @Before
    public void setup() {
        markdown = new Markdown();

        var mockSession = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(mockSession);
        ui.add(markdown);
    }

    @Test
    public void testInitialMarkdownContent() {
        Assert.assertNull(markdown.getContent());
    }

    @Test
    public void testSetContent() {
        markdown.setContent("**Hello** _World_");
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);
    }

    @Test
    public void testGetMarkdown() {
        markdown.setContent("**Hello** _World_");
        Assert.assertEquals("**Hello** _World_", markdown.getContent());
    }

    @Test
    public void overloadedConstructor_testGetMarkdown() {
        markdown = new Markdown("**Hello** _World_");
        Assert.assertEquals("**Hello** _World_", markdown.getContent());
    }

    @Test
    public void testAppendContent() {
        markdown.appendContent("**Hello**");
        Assert.assertEquals("**Hello**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Hello**", false);

        markdown.appendContent(" _World_");
        Assert.assertEquals("**Hello** _World_", markdown.getContent());
        assertUpdateMarkdownCall(markdown, " _World_", true);
    }

    @Test
    public void testAppendContentWithSetContent() {
        markdown.setContent("**Hello**");
        Assert.assertEquals("**Hello**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Hello**", false);

        markdown.setContent("**Hello** _World_");
        Assert.assertEquals("**Hello** _World_", markdown.getContent());
        assertUpdateMarkdownCall(markdown, " _World_", true);
    }

    @Test
    public void testReplaceMarkdown() {
        markdown.setContent("**Hello**");
        Assert.assertEquals("**Hello**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Hello**", false);

        markdown.setContent("**Foobar**");
        Assert.assertEquals("**Foobar**", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Foobar**", false);
    }

    @Test
    public void testSetSameMarkdown() {
        markdown.setContent("**Hello** _World_");
        Assert.assertEquals("**Hello** _World_", markdown.getContent());
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);

        markdown.setContent("**Hello** _World_");
        Assert.assertEquals("**Hello** _World_", markdown.getContent());
        Assert.assertEquals(0, getPendingJavaScriptInvocations().size());
    }

    @Test
    public void testSetContentTwice() {
        markdown.setContent("**Foobar**");
        markdown.setContent("**Hello** _World_");
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);
    }

    @Test
    public void testRemoveMarkdown() {
        markdown.setContent("**Hello** _World_");
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);

        markdown.setContent(null);
        Assert.assertNull(markdown.getContent());
        assertUpdateMarkdownCall(markdown, null, false);
    }

    @Test
    public void testDetach_setContent_attach() {
        markdown.removeFromParent();

        markdown.setContent("**Hello** _World_");
        Assert.assertEquals(0, getPendingJavaScriptInvocations().size());

        ui.add(markdown);
        assertUpdateMarkdownCall(markdown, "**Hello** _World_", false);
        Assert.assertEquals("**Hello** _World_", markdown.getContent());
    }

    private void assertUpdateMarkdownCall(Component component, String content,
            boolean isAppend) {
        var pendingJavaScriptInvocations = getPendingJavaScriptInvocations();

        Assert.assertEquals(1, pendingJavaScriptInvocations.size());

        var pendingJavaScriptInvocation = pendingJavaScriptInvocations.get(0);
        var parameters = pendingJavaScriptInvocation.getInvocation()
                .getParameters();
        var element = component != null ? component.getElement() : null;

        if (isAppend) {
            Assert.assertEquals(
                    "return (async function() { this.content += $0}).apply($1)",
                    pendingJavaScriptInvocation.getInvocation()
                            .getExpression());
        } else {
            Assert.assertEquals(
                    "return (async function() { this.content = $0}).apply($1)",
                    pendingJavaScriptInvocation.getInvocation()
                            .getExpression());
        }
        Assert.assertEquals(content, parameters.get(0));
        Assert.assertEquals(element, parameters.get(1));
    }

    private List<PendingJavaScriptInvocation> getPendingJavaScriptInvocations() {
        fakeClientCommunication();
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

}
