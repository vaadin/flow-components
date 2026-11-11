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
package com.vaadin.flow.component.ai.provider;

import java.util.regex.Pattern;

/**
 * Checks whether a SQL query has a row limit of its own, for the default paging
 * of {@link DatabaseProvider}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
final class QueryLimitCheck {

    private static final Pattern QUOTED_OR_COMMENT = Pattern.compile(
            "'[^']*'|\"[^\"]*\"|`[^`]*`|--[^\\n]*|/\\*.*?\\*/", Pattern.DOTALL);

    private static final Pattern INNERMOST_PARENTHESES = Pattern
            .compile("\\([^()]*\\)");

    private static final Pattern LIMIT_WORD = Pattern
            .compile("\\b(?:limit|offset|fetch)\\b", Pattern.CASE_INSENSITIVE);

    private QueryLimitCheck() {
        // Static methods only
    }

    /**
     * Returns whether the given query has a row limit of its own, which a limit
     * cannot follow: the word LIMIT, OFFSET or FETCH outside of quoted
     * identifiers, string literals, comments and parentheses. A query that is
     * in parentheses as a whole is checked inside of them.
     *
     * @param sql
     *            the query to check
     * @return {@code true} if the query has a row limit of its own,
     *         {@code false} otherwise
     */
    static boolean limitsRows(String sql) {
        var bare = QUOTED_OR_COMMENT.matcher(sql).replaceAll(" ");
        var outer = bare;
        String previous;
        do {
            // Each round leaves out the innermost parentheses
            previous = outer;
            outer = INNERMOST_PARENTHESES.matcher(outer).replaceAll(" ");
        } while (!outer.equals(previous));
        return LIMIT_WORD.matcher(outer.isBlank() ? bare : outer).find();
    }
}
