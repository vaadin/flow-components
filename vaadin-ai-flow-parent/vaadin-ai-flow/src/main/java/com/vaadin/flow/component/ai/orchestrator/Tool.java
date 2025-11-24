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
 * Marks a method as a tool that can be called by the LLM.
 * <p>
 * Methods annotated with this annotation will be automatically discovered and
 * converted into LLM tools when the object containing them is passed to
 * {@link BaseAiOrchestrator.BaseBuilder#setTools(Object...)}.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code
 * @Tool("Fetches data from the user database")
 * public String getUserData(@ParameterDescription("User ID") String userId) {
 *     return "User data for " + userId;
 * }
 * }
 * </pre>
 *
 * @author Vaadin Ltd
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Tool {
    /**
     * The description of what the tool does. This should clearly explain the
     * tool's purpose to help the LLM understand when to use it.
     *
     * @return the tool description
     */
    String value();
}
