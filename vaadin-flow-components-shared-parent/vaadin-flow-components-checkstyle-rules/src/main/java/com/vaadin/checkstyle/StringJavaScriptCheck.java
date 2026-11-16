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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * The rule has no type information. A name is resolved to its declaration in
 * the same file, following the scopes of Java: a local variable or parameter
 * hides a field, and a declaration in one method does not apply to another.
 * <code>this.name</code>, <code>Outer.this.name</code> and
 * <code>Outer.name</code> are resolved to the fields of the class. A name that
 * is not declared in the file, such as an inherited field, is unknown.
 * <p>
 * For <code>LitRenderer.of</code>, a string is recognized as a literal, a
 * concatenation, or a name declared as <code>String</code>. For
 * <code>addEventData</code>, the argument is a declared
 * <code>DomEventData</code> when it is a name declared as
 * <code>DomEventData</code>, or a method call on
 * <code>JsSnippets.of(...)</code> or on a name initialized with it. Anything
 * else is flagged, so a <code>DomEventData</code> from another class is kept in
 * a <code>DomEventData</code> variable first. As other APIs, such as data
 * views, also have a <code>setFilter</code> method, it is only flagged on a DOM
 * listener registration: a call chain through
 * <code>addEventListener(...)</code>, or a name declared as
 * <code>DomListenerRegistration</code> or initialized with such a chain.
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

    /**
     * Nodes that start a scope for the names declared directly in them, or in
     * their parameters, loop header or resources.
     */
    private static final Set<Integer> SCOPES = Set.of(TokenTypes.SLIST,
            TokenTypes.OBJBLOCK, TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF,
            TokenTypes.COMPACT_CTOR_DEF, TokenTypes.LAMBDA,
            TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_TRY,
            TokenTypes.LITERAL_CATCH);

    private static final Set<Integer> CLASSES = Set.of(TokenTypes.CLASS_DEF,
            TokenTypes.INTERFACE_DEF, TokenTypes.ENUM_DEF,
            TokenTypes.RECORD_DEF);

    /**
     * What a declared name is known to hold.
     */
    private enum Kind {
        STRING, EVENT_DATA, SNIPPETS, REGISTRATION, OTHER
    }

    private String cspMode = "off";

    private boolean skipFile;

    /**
     * The declarations of each scope, by name.
     */
    private final Map<DetailAST, Map<String, DetailAST>> scopes = new HashMap<>();

    /**
     * The bodies of the named classes in the file, by simple name.
     */
    private final Map<String, DetailAST> classBodies = new HashMap<>();

    private final Map<DetailAST, Kind> kinds = new HashMap<>();

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
        scopes.clear();
        classBodies.clear();
        kinds.clear();
        calls.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (skipFile) {
            return;
        }
        if (ast.getType() != TokenTypes.METHOD_CALL) {
            declare(ast);
        } else {
            // Checked when the whole file is read, so that a name declared
            // after its use is known
            calls.add(ast);
        }
    }

    /**
     * Records a variable, field or parameter in its scope. Its kind is only
     * found when a call needs it, as its initializer can refer to names
     * declared later in the file.
     */
    private void declare(DetailAST declaration) {
        DetailAST scope = declaration.getParent();
        while (scope != null && !SCOPES.contains(scope.getType())) {
            scope = scope.getParent();
        }
        if (scope == null) {
            return;
        }
        String name = declaration.findFirstToken(TokenTypes.IDENT).getText();
        scopes.computeIfAbsent(scope, key -> new HashMap<>()).putIfAbsent(name,
                declaration);
        DetailAST owner = scope.getParent();
        if (scope.getType() == TokenTypes.OBJBLOCK && owner != null
                && CLASSES.contains(owner.getType())) {
            classBodies.putIfAbsent(
                    owner.findFirstToken(TokenTypes.IDENT).getText(), scope);
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
        return kindOfReference(node) == Kind.REGISTRATION;
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
        default -> kindOfReference(node) == Kind.STRING;
        };
    }

    /**
     * Whether the expression is a declared <code>DomEventData</code>: a name
     * declared as <code>DomEventData</code> in the file, or a method call on
     * <code>JsSnippets.of(...)</code> or on a name initialized with it.
     */
    private boolean isDeclaredEventData(DetailAST expression) {
        DetailAST node = unwrap(expression);
        if (node.getType() != TokenTypes.METHOD_CALL) {
            return kindOfReference(node) == Kind.EVENT_DATA;
        }
        if (node.getFirstChild().getType() != TokenTypes.DOT) {
            return false;
        }
        DetailAST receiver = node.getFirstChild().getFirstChild();
        return isJsSnippetsOf(receiver)
                || kindOfReference(receiver) == Kind.SNIPPETS;
    }

    /**
     * The kind of a reference to a declared name: <code>name</code>,
     * <code>this.name</code>, <code>Outer.this.name</code> or
     * <code>Outer.name</code>, or <code>null</code> if the name is not declared
     * in the file or the expression is not such a reference.
     */
    private Kind kindOfReference(DetailAST expression) {
        DetailAST node = unwrap(expression);
        DetailAST declaration = switch (node.getType()) {
        case TokenTypes.IDENT -> declarationInScope(node, node.getText());
        case TokenTypes.DOT -> fieldDeclaration(node);
        default -> null;
        };
        return declaration == null ? null : kindOf(declaration);
    }

    /**
     * The declaration that a name used at the node refers to: the one in the
     * innermost enclosing scope, or <code>null</code>.
     */
    private DetailAST declarationInScope(DetailAST node, String name) {
        for (DetailAST scope = node.getParent(); scope != null; scope = scope
                .getParent()) {
            Map<String, DetailAST> declarations = scopes.get(scope);
            if (declarations != null && declarations.containsKey(name)) {
                return declarations.get(name);
            }
        }
        return null;
    }

    /**
     * The field declaration of a qualified name, such as <code>this.name</code>
     * or <code>Outer.name</code>, or <code>null</code>.
     */
    private DetailAST fieldDeclaration(DetailAST dot) {
        DetailAST name = dot.getLastChild();
        if (name.getType() != TokenTypes.IDENT) {
            return null;
        }
        DetailAST classBody = classBody(dot, dot.getFirstChild());
        Map<String, DetailAST> fields = classBody == null ? null
                : scopes.get(classBody);
        return fields == null ? null : fields.get(name.getText());
    }

    /**
     * The body of the class that a qualifier refers to: <code>this</code>,
     * <code>Outer.this</code> or <code>Outer</code>, or <code>null</code>.
     */
    private DetailAST classBody(DetailAST node, DetailAST qualifier) {
        if (qualifier.getType() == TokenTypes.LITERAL_THIS) {
            DetailAST body = node.getParent();
            while (body != null && body.getType() != TokenTypes.OBJBLOCK) {
                body = body.getParent();
            }
            return body;
        }
        DetailAST className = qualifier;
        if (className.getType() == TokenTypes.DOT && className.getLastChild()
                .getType() == TokenTypes.LITERAL_THIS) {
            className = className.getFirstChild();
        }
        // A variable hides a class of the same name
        if (className.getType() != TokenTypes.IDENT
                || declarationInScope(className, className.getText()) != null) {
            return null;
        }
        return classBodies.get(className.getText());
    }

    private Kind kindOf(DetailAST declaration) {
        Kind known = kinds.get(declaration);
        if (known != null) {
            return known;
        }
        // Stops a name whose initializer refers back to it
        kinds.put(declaration, Kind.OTHER);
        Kind kind = declaredKind(declaration);
        kinds.put(declaration, kind);
        return kind;
    }

    /**
     * The kind of a declaration from its type, or else from its initializer.
     */
    private Kind declaredKind(DetailAST declaration) {
        String type = typeName(declaration.findFirstToken(TokenTypes.TYPE));
        if ("String".equals(type)) {
            return Kind.STRING;
        }
        if ("DomEventData".equals(type)) {
            return Kind.EVENT_DATA;
        }
        if ("DomListenerRegistration".equals(type)) {
            return Kind.REGISTRATION;
        }
        DetailAST initializer = declaration.findFirstToken(TokenTypes.ASSIGN);
        if (initializer != null && initializer.getFirstChild() != null) {
            DetailAST value = unwrap(initializer.getFirstChild());
            if (isJsSnippetsOf(value)) {
                return Kind.SNIPPETS;
            }
            if (isListenerRegistration(value)) {
                return Kind.REGISTRATION;
            }
        }
        return Kind.OTHER;
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
