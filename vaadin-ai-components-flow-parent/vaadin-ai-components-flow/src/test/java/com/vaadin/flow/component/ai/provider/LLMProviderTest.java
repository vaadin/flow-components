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
package com.vaadin.flow.component.ai.provider;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link LLMProvider} interface and its nested interfaces.
 */
public class LLMProviderTest {

    @Test
    public void llmRequestOf_withValidMessage_createsRequest() {
        var request = LLMProvider.LLMRequest.of("Hello world");

        Assert.assertEquals("Hello world", request.userMessage());
        Assert.assertNotNull(request.attachments());
        Assert.assertTrue(request.attachments().isEmpty());
        Assert.assertNull(request.systemPrompt());
        Assert.assertNotNull(request.tools());
        Assert.assertEquals(0, request.tools().length);
    }

    @Test
    public void llmRequestOf_withEmptyMessage_throwsException() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> LLMProvider.LLMRequest.of(null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> LLMProvider.LLMRequest.of(""));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> LLMProvider.LLMRequest.of("   "));
    }

    @Test
    public void llmRequestOf_trimsUserMessage() {
        var request = LLMProvider.LLMRequest.of("  Hello world  ");

        Assert.assertEquals("Hello world", request.userMessage());
    }
}
