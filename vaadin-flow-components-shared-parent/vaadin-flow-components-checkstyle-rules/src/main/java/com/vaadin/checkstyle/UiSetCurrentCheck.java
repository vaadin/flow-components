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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checkstyle rule that ensures UI.setCurrent() calls in test classes are used
 * correctly. This check enforces two requirements:
 * <p>
 * <b>1. Field Reference Requirement:</b> The argument to UI.setCurrent() must
 * be a class field reference, to prevent garbage collection of the
 * WeakReference-held UI instance. The UI.setCurrent() method stores the UI in a
 * ThreadLocal using a WeakReference (via CurrentInstance). If the UI is not
 * stored in a class field, it can be garbage collected, causing UI.getCurrent()
 * to return null unexpectedly.
 * <p>
 * <b>2. Non-Static Method Requirement:</b> UI.setCurrent() must not be called
 * from a static method because CurrentInstanceCleanerListener from
 * flow-test-generic resets the current UI before each test. Calls should be
 * moved to instance methods or {@code @Before}/{@code @BeforeEach} setup
 * methods.
 * <p>
 * <b>Valid usage:</b>
 *
 * <pre>{@code
 * public class MyTest {
 *     private UI ui;
 *
 *     @Before
 *     public void setup() {
 *         ui = new MockUI();
 *         UI.setCurrent(ui); // OK: instance method with field reference
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Invalid usage:</b>
 *
 * <pre>
 * public class MyTest {
 *     &#64;BeforeClass
 *     public static void setupClass() {
 *         UI.setCurrent(new MockUI()); // ERROR: static method
 *     }
 *
 *     &#64;Before
 *     public void setup() {
 *         UI.setCurrent(new MockUI()); // ERROR: not a field reference
 *     }
 * }
 * </pre>
 */
public class UiSetCurrentCheck extends AbstractCheck {

    /**
     * Message key for the field reference violation.
     */
    public static final String MSG_KEY = "ui.setcurrent.no.field";

    /**
     * Message key for the static method violation.
     */
    public static final String MSG_KEY_STATIC_METHOD = "ui.setcurrent.static.method";

    /**
     * Field names of type UI in the current class.
     */
    private final Set<String> uiFieldNames = new HashSet<>();

    /**
     * Whether the current file is a test class.
     */
    private boolean isTestFile = false;

    /**
     * Stack tracking whether we're inside a static method context. Each
     * METHOD_DEF pushes true (static) or false (non-static), and pops on leave.
     */
    private final Deque<Boolean> staticContextStack = new ArrayDeque<>();

    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF,
                TokenTypes.METHOD_CALL };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        uiFieldNames.clear();
        staticContextStack.clear();
        // Check if this is a test file based on path
        String filePath = getFilePath();
        isTestFile = filePath.contains("/src/test/");
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (!isTestFile) {
            return;
        }

        switch (ast.getType()) {
        case TokenTypes.CLASS_DEF:
            collectUiFields(ast);
            break;
        case TokenTypes.METHOD_DEF:
            staticContextStack.push(hasStaticModifier(ast));
            break;
        case TokenTypes.METHOD_CALL:
            checkUiSetCurrentCall(ast);
            break;
        default:
            break;
        }
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (!isTestFile) {
            return;
        }

        if (ast.getType() == TokenTypes.METHOD_DEF) {
            staticContextStack.pop();
        }
    }

    /**
     * Collects all field names that are of type UI.
     */
    private void collectUiFields(DetailAST classDef) {
        DetailAST objBlock = classDef.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return;
        }

        for (DetailAST child = objBlock
                .getFirstChild(); child != null; child = child
                        .getNextSibling()) {
            if (child.getType() == TokenTypes.VARIABLE_DEF) {
                if (isUiType(child)) {
                    DetailAST ident = child.findFirstToken(TokenTypes.IDENT);
                    if (ident != null) {
                        uiFieldNames.add(ident.getText());
                    }
                }
            }
        }
    }

    /**
     * Checks if a variable definition is of type UI.
     */
    private boolean isUiType(DetailAST variableDef) {
        DetailAST type = variableDef.findFirstToken(TokenTypes.TYPE);
        if (type != null) {
            DetailAST ident = type.findFirstToken(TokenTypes.IDENT);
            if (ident != null && "UI".equals(ident.getText())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a method call is UI.setCurrent() and validates that it is not
     * in a static method and the argument is a class field.
     */
    private void checkUiSetCurrentCall(DetailAST methodCall) {
        if (!isUiSetCurrentCall(methodCall)) {
            return;
        }

        // Check for static method context first
        if (isInStaticContext()) {
            log(methodCall.getLineNo(), MSG_KEY_STATIC_METHOD);
            return; // Don't double-report field reference issues
        }

        // Get the argument passed to setCurrent
        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist == null) {
            return;
        }

        DetailAST arg = elist.findFirstToken(TokenTypes.EXPR);
        if (arg == null) {
            return;
        }

        // Check if argument is null (UI.setCurrent(null) is OK)
        if (arg.findFirstToken(TokenTypes.LITERAL_NULL) != null) {
            return;
        }

        // Check if argument is a simple identifier (variable name)
        DetailAST argIdent = arg.findFirstToken(TokenTypes.IDENT);
        if (argIdent != null) {
            String argName = argIdent.getText();
            // Check if this identifier is a field
            if (!uiFieldNames.contains(argName)) {
                log(methodCall.getLineNo(), MSG_KEY, argName);
            }
            return;
        }

        // Check if argument is this.fieldName
        DetailAST dot = arg.findFirstToken(TokenTypes.DOT);
        if (dot != null && isThisFieldAccess(dot)) {
            return; // this.field is OK
        }

        // Flag any other expression (constructor, method call, etc.)
        log(methodCall.getLineNo(), MSG_KEY, "expression");
    }

    /**
     * Checks if a method call AST represents UI.setCurrent().
     */
    private boolean isUiSetCurrentCall(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return false;
        }

        DetailAST firstChild = dot.getFirstChild();
        DetailAST lastChild = dot.getLastChild();

        return firstChild != null && "UI".equals(firstChild.getText())
                && lastChild != null
                && "setCurrent".equals(lastChild.getText());
    }

    /**
     * Checks if a DOT expression is this.fieldName where fieldName is a UI
     * field.
     */
    private boolean isThisFieldAccess(DetailAST dot) {
        DetailAST first = dot.getFirstChild();
        if (first != null && first.getType() == TokenTypes.LITERAL_THIS) {
            DetailAST fieldName = dot.getLastChild();
            return fieldName != null
                    && uiFieldNames.contains(fieldName.getText());
        }
        return false;
    }

    /**
     * Checks if the given AST node (METHOD_DEF) has a static modifier.
     */
    private boolean hasStaticModifier(DetailAST methodDef) {
        DetailAST modifiers = methodDef.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        return modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
    }

    /**
     * Checks if we are currently inside a static method context.
     */
    private boolean isInStaticContext() {
        return !staticContextStack.isEmpty()
                && Boolean.TRUE.equals(staticContextStack.peek());
    }
}
