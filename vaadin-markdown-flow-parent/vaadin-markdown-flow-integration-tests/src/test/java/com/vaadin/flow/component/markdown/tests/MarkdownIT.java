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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.markdown.testbench.MarkdownElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-markdown")
public class MarkdownIT extends AbstractComponentIT {

    private MarkdownElement markdownElement;

    @Before
    public void init() {
        open();
        markdownElement = $(MarkdownElement.class).waitForFirst();
    }

    @Test
    public void contentMatches() {
        var content = markdownElement.getContent();
        Assert.assertEquals("**Hello** _World_", content);
    }

    @Test
    public void appendContent_contentMatches() {
        clickElementWithJs("append-button");
        var content = markdownElement.getContent();
        Assert.assertEquals("**Hello** _World_!", content);
    }

    @Test
    public void setContent_contentMatches() {
        clickElementWithJs("set-button");
        var content = markdownElement.getContent();
        Assert.assertEquals("**Updated** _Markdown_", content);
    }

}
