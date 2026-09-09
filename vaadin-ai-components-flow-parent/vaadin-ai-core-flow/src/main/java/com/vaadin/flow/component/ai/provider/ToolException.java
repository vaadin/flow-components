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

/**
 * Signals a tool failure whose message is safe to pass back to the LLM.
 * <p>
 * Throw this from tool code — a
 * {@link LLMProvider.ToolSpec#execute(tools.jackson.databind.JsonNode)
 * ToolSpec.execute} implementation, a controller callback, or a
 * {@link DatabaseProvider} — when the LLM should see why the call failed so it
 * can correct its next attempt instead of retrying blindly. The
 * {@link #getMessage() message} is forwarded verbatim as the tool's error
 * output, so you must ensure it is safe to expose: no PII, no internal
 * identifiers beyond what the LLM already sent, no third-party error text.
 * <p>
 * Any other exception thrown from tool code is caught, logged, and replaced
 * with a generic error message before reaching the LLM, so internal details are
 * not leaked.
 * <p>
 * This contract applies only to tools defined through the framework-agnostic
 * {@link LLMProvider.ToolSpec} API — the tools registered by
 * {@link com.vaadin.flow.component.ai.orchestrator.AIController}
 * implementations and by {@link DatabaseProvider}-backed tools. Tools defined
 * with vendor-specific annotations (LangChain4j's or Spring AI's {@code @Tool})
 * are executed by the vendor framework itself, whose own error handling decides
 * what reaches the LLM — by default both frameworks relay the raw message of
 * any exception, so neither the verbatim-forwarding nor the generic-replacement
 * behavior described above applies to them.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public class ToolException extends RuntimeException {

    /**
     * Creates a new tool exception.
     *
     * @param llmFacingMessage
     *            the failure message, forwarded verbatim to the LLM
     */
    public ToolException(String llmFacingMessage) {
        super(llmFacingMessage);
    }

    /**
     * Creates a new tool exception with a cause. Only the message is forwarded
     * to the LLM; the cause is available for logging.
     *
     * @param llmFacingMessage
     *            the failure message, forwarded verbatim to the LLM
     * @param cause
     *            the underlying cause
     */
    public ToolException(String llmFacingMessage, Throwable cause) {
        super(llmFacingMessage, cause);
    }
}
