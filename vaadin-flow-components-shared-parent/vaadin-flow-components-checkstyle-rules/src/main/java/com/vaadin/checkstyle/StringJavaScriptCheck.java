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
package com.vaadin.checkstyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checkstyle rule that flags calls sending JavaScript to the browser as a
 * string, which the browser compiles and a Content Security Policy without
 * <code>unsafe-eval</code> blocks. Such JavaScript has to be declared in a
 * <code>@JsDefinition</code> interface instead.
 * <p>
 * The rule only runs when its <code>cspMode</code> property is
 * <code>strict</code>. The property comes from the <code>csp.mode</code> Maven
 * property of the module, so that modules are switched one by one while they
 * are migrated. It checks main sources only: tests and integration test modules
 * are left out.
 * <p>
 * Only calls that send a string whatever its value are flagged:
 * <ul>
 * <li><code>executeJs(...)</code> whose first argument is not a class
 * literal</li>
 * <li><code>JsFunction.of(...)</code> and <code>addDynamicImport(...)</code>
 * </li>
 * <li><code>LitRenderer.of(...)</code> with a string</li>
 * <li><code>addEventData(...)</code> whose argument is neither a class literal
 * nor a declared <code>DomEventData</code>, and every
 * <code>addEventDataElement(...)</code>. Their string overloads are deprecated:
 * a property path is added with <code>addEventDataPath</code> or
 * <code>addEventDataElementPath</code>, and any other expression is a declared
 * <code>DomEventData</code>.</li>
 * <li><code>setFilter(...)</code> on a DOM listener registration, with any
 * argument but <code>null</code>. A filter is declared as a
 * <code>DomEventFilter</code> and set with <code>setFilterExpression</code>
 * instead.</li>
 * </ul>
 * <code>@EventData</code> annotations are not checked: Flow puts their values
 * into the bundle.
 * <p>
 * The rule has no type information. For <code>LitRenderer.of</code>, a string
 * is recognized as a literal, a concatenation, or a name declared as
 * <code>String</code> in the same file. For <code>addEventData</code>, the
 * argument is a declared <code>DomEventData</code> when it is a name declared
 * as <code>DomEventData</code> in the same file, or a method call on
 * <code>JsSnippets.of(...)</code> or on a name initialized with it in the same
 * file. Anything else is flagged, so a <code>DomEventData</code> from another
 * class is kept in a <code>DomEventData</code> variable first. As other APIs,
 * such as data views, also have a <code>setFilter</code> method, it is only
 * flagged on a DOM listener registration: a call chain through
 * <code>addEventListener(...)</code>, or a name declared as
 * <code>DomListenerRegistration</code> or initialized with such a chain in the
 * same file.
 * <p>
 * Code that has to keep sending a string, such as a deprecated public API, is
 * marked with
 * <code>@SuppressWarnings("checkstyle:StringJavaScriptCheck")</code>.
 */
public class StringJavaScriptCheck extends AbstractCheck {

    /**
     * Message key for a call that sends JavaScript as a string.
     */
    public static final String MSG_KEY = "string.javascript.call";

    /**
     * Message key for event data added as a string.
     */
    public static final String EVENT_DATA_MSG_KEY = "string.eventdata.call";

    /**
     * Message key for an event filter set as a string.
     */
    public static final String FILTER_MSG_KEY = "string.filter.call";

    private String cspMode = "off";

    private boolean skipFile;

    private final Set<String> stringNames = new HashSet<>();

    private final Set<String> eventDataNames = new HashSet<>();

    private final Set<String> snippetsNames = new HashSet<>();

    private final Set<String> registrationNames = new HashSet<>();

    private final List<DetailAST> calls = new ArrayList<>();

    /**
     * Sets the CSP mode of the module. The rule only runs for
     * <code>strict</code>.
     *
     * @param cspMode
     *            the CSP mode, <code>off</code>, <code>warn</code> or
     *            <code>strict</code>
     */
    public void setCspMode(String cspMode) {
        this.cspMode = cspMode;
    }

    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.METHOD_CALL, TokenTypes.VARIABLE_DEF,
                TokenTypes.PARAMETER_DEF };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public void beginTree(DetailAST rootAst) {
        String path = getFilePath().replace('\\', '/');
        skipFile = !"strict".equalsIgnoreCase(cspMode)
                || path.contains("/src/test/")
                || path.contains("-integration-tests/");
        stringNames.clear();
        eventDataNames.clear();
        snippetsNames.clear();
        registrationNames.clear();
        calls.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (skipFile) {
            return;
        }
        if (ast.getType() != TokenTypes.METHOD_CALL) {
            // A variable, field or parameter
            String name = ast.findFirstToken(TokenTypes.IDENT).getText();
            String type = typeName(ast.findFirstToken(TokenTypes.TYPE));
            if ("String".equals(type)) {
                stringNames.add(name);
            } else if ("DomEventData".equals(type)) {
                eventDataNames.add(name);
            } else if ("DomListenerRegistration".equals(type)) {
                registrationNames.add(name);
            }
            DetailAST initializer = ast.findFirstToken(TokenTypes.ASSIGN);
            if (initializer != null && initializer.getFirstChild() != null) {
                DetailAST value = unwrap(initializer.getFirstChild());
                if (isJsSnippetsOf(value)) {
                    snippetsNames.add(name);
                } else if (isListenerRegistration(value)) {
                    registrationNames.add(name);
                }
            }
        } else {
            // Checked when the whole file is read, so that a name declared
            // after its use is known
            calls.add(ast);
        }
    }

    @Override
    public void finishTree(DetailAST rootAst) {
        if (!skipFile) {
            calls.forEach(this::checkCall);
        }
    }

    private void checkCall(DetailAST call) {
        String name = methodName(call);
        String messageKey = name == null ? null
                : messageKey(call, name, firstArgument(call));
        if (messageKey != null) {
            String receiver = receiverName(call);
            log(call, messageKey,
                    receiver == null ? name : receiver + "." + name);
        }
    }

    /**
     * The key of the message for a flagged call, or <code>null</code> if the
     * call is not flagged.
     */
    private String messageKey(DetailAST call, String name,
            DetailAST firstArgument) {
        boolean flagged = switch (name) {
        case "executeJs" ->
            firstArgument != null && !isClassLiteral(firstArgument);
        case "addDynamicImport" -> true;
        case "of" ->
            isCalledOn(call, "JsFunction") || (isCalledOn(call, "LitRenderer")
                    && firstArgument != null && isString(firstArgument));
        case "addEventData" ->
            firstArgument != null && !isClassLiteral(firstArgument)
                    && !isDeclaredEventData(firstArgument);
        case "addEventDataElement" -> firstArgument != null;
        case "setFilter" -> firstArgument != null
                && unwrap(firstArgument).getType() != TokenTypes.LITERAL_NULL
                && call.getFirstChild().getType() == TokenTypes.DOT
                && isListenerRegistration(call.getFirstChild().getFirstChild());
        default -> false;
        };
        if (!flagged) {
            return null;
        }
        if ("setFilter".equals(name)) {
            return FILTER_MSG_KEY;
        }
        return name.startsWith("addEventData") ? EVENT_DATA_MSG_KEY : MSG_KEY;
    }

    /**
     * Whether the expression is a DOM listener registration: a call chain
     * through <code>addEventListener(...)</code>, or a name declared as
     * <code>DomListenerRegistration</code> or initialized with such a chain in
     * the file.
     */
    private boolean isListenerRegistration(DetailAST expression) {
        DetailAST node = unwrap(expression);
        // addEventListener(...).addEventData(...) is still the registration
        while (node.getType() == TokenTypes.METHOD_CALL) {
            if ("addEventListener".equals(methodName(node))) {
                return true;
            }
            DetailAST target = node.getFirstChild();
            if (target.getType() != TokenTypes.DOT) {
                return false;
            }
            node = target.getFirstChild();
        }
        return switch (node.getType()) {
        case TokenTypes.IDENT -> registrationNames.contains(node.getText());
        // this.registration
        case TokenTypes.DOT ->
            registrationNames.contains(node.getLastChild().getText());
        default -> false;
        };
    }

    /**
     * Whether the expression is a string: a literal, a text block, a
     * concatenation, or a name declared as <code>String</code> in the file.
     */
    private boolean isString(DetailAST expression) {
        DetailAST node = unwrap(expression);
        return switch (node.getType()) {
        case TokenTypes.STRING_LITERAL, TokenTypes.TEXT_BLOCK_LITERAL_BEGIN,
                TokenTypes.PLUS ->
            true;
        case TokenTypes.IDENT -> stringNames.contains(node.getText());
        default -> false;
        };
    }

    /**
     * Whether the expression is a declared <code>DomEventData</code>: a name
     * declared as <code>DomEventData</code> in the file, or a method call on
     * <code>JsSnippets.of(...)</code> or on a name initialized with it.
     */
    private boolean isDeclaredEventData(DetailAST expression) {
        DetailAST node = unwrap(expression);
        if (node.getType() == TokenTypes.IDENT) {
            return eventDataNames.contains(node.getText());
        }
        if (node.getType() != TokenTypes.METHOD_CALL
                || node.getFirstChild().getType() != TokenTypes.DOT) {
            return false;
        }
        DetailAST receiver = node.getFirstChild().getFirstChild();
        return switch (receiver.getType()) {
        case TokenTypes.METHOD_CALL -> isJsSnippetsOf(receiver);
        case TokenTypes.IDENT -> snippetsNames.contains(receiver.getText());
        // this.SNIPPETS or Outer.SNIPPETS
        case TokenTypes.DOT ->
            snippetsNames.contains(receiver.getLastChild().getText());
        default -> false;
        };
    }

    private static boolean isJsSnippetsOf(DetailAST node) {
        return node.getType() == TokenTypes.METHOD_CALL
                && "of".equals(methodName(node))
                && isCalledOn(node, "JsSnippets");
    }

    /**
     * The simple name of a declared type, such as <code>DomEventData</code> for
     * <code>DomEventData&lt;Boolean&gt;</code>, or <code>null</code>.
     */
    private static String typeName(DetailAST type) {
        DetailAST name = type == null ? null : type.getFirstChild();
        if (name != null && name.getType() == TokenTypes.DOT) {
            name = name.getLastChild();
        }
        return name == null ? null : name.getText();
    }

    private static DetailAST unwrap(DetailAST expression) {
        DetailAST node = expression;
        while (node.getType() == TokenTypes.EXPR) {
            node = node.getFirstChild();
        }
        return node;
    }

    private static boolean isClassLiteral(DetailAST expression) {
        DetailAST node = unwrap(expression);
        return node.getType() == TokenTypes.DOT
                && node.getLastChild().getType() == TokenTypes.LITERAL_CLASS;
    }

    private static DetailAST firstArgument(DetailAST call) {
        DetailAST arguments = call.findFirstToken(TokenTypes.ELIST);
        return arguments == null ? null : arguments.getFirstChild();
    }

    private static String methodName(DetailAST call) {
        DetailAST target = call.getFirstChild();
        if (target.getType() == TokenTypes.IDENT) {
            return target.getText();
        }
        if (target.getType() == TokenTypes.DOT) {
            DetailAST last = target.getLastChild();
            return last.getType() == TokenTypes.IDENT ? last.getText() : null;
        }
        return null;
    }

    /**
     * Whether the call is made on the given simple name, such as
     * <code>JsFunction</code> for <code>JsFunction.of(...)</code>.
     */
    private static boolean isCalledOn(DetailAST call, String receiver) {
        return receiver.equals(receiverName(call));
    }

    /**
     * The receiver of the call as shown in a message, such as
     * <code>getElement(...)</code>, or <code>null</code> for a call without a
     * receiver.
     */
    private static String receiverName(DetailAST call) {
        DetailAST target = call.getFirstChild();
        if (target.getType() != TokenTypes.DOT) {
            return null;
        }
        // LitRenderer.<T> of(...) has the type arguments after the receiver
        DetailAST receiver = target.getFirstChild();
        return switch (receiver.getType()) {
        case TokenTypes.METHOD_CALL -> methodName(receiver) + "(...)";
        // a.b.executeJs(...) shows as b.executeJs(...)
        case TokenTypes.DOT -> receiver.getLastChild().getText();
        default -> receiver.getText();
        };
    }
}
