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
package com.vaadin.flow.data.renderer;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.trigger.ClientValue;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.dom.JsFunction;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.tests.MockUIExtension;

class LitRendererTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void doNotAllowFunctionNamesWithFunctions() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction(
                        "foo=0; alert(\"gotcha\"); const bar", item -> {
                        }));
    }

    @Test
    void doNotAllowFunctionNamesWithSpaces() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("illegal name", item -> {
                }));
    }

    @Test
    void doNotAllowFunctionNamesWithDots() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("illegal.name", item -> {
                }));
    }

    @Test
    void doNotAllowFunctionNamesWithParenthesis() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("illegalname()", item -> {
                }));
    }

    @Test
    void allowAlphaNumericFunctionNames() {
        LitRenderer.of("<div></div>").withFunction("legalName1", item -> {
        });
    }

    @Test
    void supportGettingValueProviders() {
        LitRenderer<?> renderer = LitRenderer.of("<div></div>")
                .withProperty("foo", item -> 1).withProperty("bar", item -> 2);

        Assertions.assertTrue(
                renderer.getValueProviders().keySet().contains("foo"));
        Assertions.assertTrue(
                renderer.getValueProviders().keySet().contains("bar"));
        Assertions.assertTrue(renderer.getValueProviders().size() == 2);
    }

    @Test
    void clientAction_oneBindingSendsTheActionFunctionToTheClient() {
        TestDiv container = new TestDiv();
        ui.add(container);

        LitRenderer<String> renderer = LitRenderer.<String> of(
                "<span>${item.email}</span><button @click=${copy}>Copy</button>")
                .withProperty("email", ValueProvider.identity())
                .withClientAction("copy", Clipboard.write()
                        .text(ClientValue.itemProperty("email")));

        renderer.render(container.getElement(), new KeyMapper<>(), "renderer");
        ui.getUI().getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();

        List<Object> parameters = ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("setLitRenderer"))
                .findFirst().orElseThrow().getParameters();

        // The action names the template can call, and the functions behind
        // them: one function for the whole renderer, not one per item.
        Assertions.assertEquals("[\"copy\"]", parameters.get(6).toString());
        JsFunction actions = (JsFunction) parameters.get(7);
        Assertions.assertEquals("return [$0]", actions.getBody());

        JsFunction copy = (JsFunction) actions.getCaptures().get(0);
        Assertions.assertEquals(List.of("event", "context"),
                copy.getArgumentNames());
        Assertions.assertTrue(
                copy.getBody().contains("window.Vaadin.Flow.clipboard"),
                "expected a clipboard write, got: " + copy.getBody());

        // The copied value is read from the item the action fired for, so the
        // same function serves every rendered row.
        JsFunction text = (JsFunction) copy.getCaptures().get(0);
        Assertions.assertEquals("return context[$0][$1]", text.getBody());
        Assertions.assertEquals(List.of("item", "email"), text.getCaptures());
    }

    @Test
    void noClientActions_clientStillGetsAnEmptyBundle() {
        TestDiv container = new TestDiv();
        ui.add(container);

        LitRenderer.<String> of("<span>${item}</span>")
                .render(container.getElement(), new KeyMapper<>(), "renderer");
        ui.getUI().getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();

        List<Object> parameters = ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("setLitRenderer"))
                .findFirst().orElseThrow().getParameters();

        Assertions.assertEquals("[]", parameters.get(6).toString());
        Assertions.assertEquals("return []",
                ((JsFunction) parameters.get(7)).getBody());
    }

    @Test
    void doNotAllowClientActionNamesWithSpaces() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withClientAction("illegal name",
                        Clipboard.write().text(ClientValue.of("x"))));
    }

    @Test
    void doNotAllowClientActionNameAlreadyUsedByAFunction() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("copy", item -> {
                }).withClientAction("copy",
                        Clipboard.write().text(ClientValue.of("x"))));
    }
}
