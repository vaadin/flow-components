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

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checkstyle rule that flags any usage of {@code UI.setCurrent()}.
 * <p>
 * Direct calls to {@code UI.setCurrent()} should be avoided because the UI is
 * held internally via a WeakReference (through CurrentInstance), which can lead
 * to unexpected garbage collection when the UI is not explicitly stored in a
 * test class field or variable. Additionally, calling UI.setCurrent in a static
 * context can lead to the UI being cleaned up after individual tests. Use
 * {@code MockUIRule} to set up a UI context instead.
 */
public class UiSetCurrentCheck extends AbstractCheck {

    /**
     * Message key for the violation.
     */
    public static final String MSG_KEY = "ui.setcurrent";

    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.METHOD_CALL };
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
    public void visitToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.METHOD_CALL
                && isUiSetCurrentCall(ast)) {
            log(ast.getLineNo(), MSG_KEY);
        }
    }

    /**
     * Checks if a method call AST represents {@code UI.setCurrent()}.
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
}
