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
package com.vaadin.flow.component.ai.orchestrator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a description for a parameter of a {@link Tool}-annotated method.
 * <p>
 * This annotation helps the LLM understand what each parameter represents and
 * how to use it correctly.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code
 * @Tool("Fetches user information")
 * public String getUser(
 *     @ParameterDescription("The unique user identifier") String userId,
 *     @ParameterDescription("Whether to include detailed information") boolean includeDetails) {
 *     // implementation
 * }
 * }
 * </pre>
 *
 * @author Vaadin Ltd
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParameterDescription {
    /**
     * The description of the parameter. This should clearly explain what the
     * parameter represents and any constraints on its value.
     *
     * @return the parameter description
     */
    String value();
}
