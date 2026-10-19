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
package com.vaadin.tests;

import java.util.List;

import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsCall;

/**
 * Helpers for inspecting the invocations that
 * {@link Element#callJsFunction(String, Object...)} schedules.
 * <p>
 * The client is sent the name of the function and the arguments of the call
 * rather than JavaScript that names the function, so every such invocation
 * carries the same expression and the called function can only be told from the
 * call itself.
 */
public final class JsFunctionCallUtil {

    private JsFunctionCallUtil() {
        // Only static members
    }

    /**
     * Gets the name of the function that the given invocation calls.
     *
     * @param invocation
     *            the invocation to read the function name from
     * @return the name of the called function, or <code>null</code> if the
     *         invocation is not a function call, for example JavaScript
     *         scheduled with {@link Element#executeJs(String, Object...)}
     */
    public static String getFunctionName(JavaScriptInvocation invocation) {
        JsCall call = getFunctionCall(invocation);
        return call != null ? (String) call.arguments().get(0) : null;
    }

    /**
     * Gets the arguments that the given invocation calls its function with,
     * without the name of the function and the element it is called on.
     *
     * @param invocation
     *            the invocation to read the arguments from
     * @return the arguments of the call, or an empty list if the invocation is
     *         not a function call
     */
    public static List<Object> getArguments(JavaScriptInvocation invocation) {
        JsCall call = getFunctionCall(invocation);
        if (call == null) {
            return List.of();
        }
        List<Object> arguments = call.flattenArguments();
        return arguments.subList(1, arguments.size());
    }

    private static JsCall getFunctionCall(JavaScriptInvocation invocation) {
        JsCall call = invocation.getJsCall();
        return call != null
                && call.definitionType() == Element.CallFunctionJs.class ? call
                        : null;
    }
}
