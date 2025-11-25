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

import com.vaadin.flow.component.ai.provider.LLMProvider;

import java.io.Serializable;

/**
 * Validates user input and attachments before they are sent to the LLM.
 * <p>
 * Validators are essential for security, particularly for preventing prompt
 * injection attacks in multimodal contexts. They can:
 * </p>
 * <ul>
 * <li>Validate text input for malicious patterns</li>
 * <li>Scan file attachments for suspicious content</li>
 * <li>Check for prompt injection attempts</li>
 * <li>Enforce content policies</li>
 * </ul>
 * <p>
 * Example implementation for basic prompt injection detection:
 * </p>
 *
 * <pre>
 * public class PromptInjectionValidator implements InputValidator {
 *     private static final List&lt;String&gt; SUSPICIOUS_PATTERNS = List.of(
 *         "ignore previous instructions",
 *         "ignore all previous",
 *         "disregard previous",
 *         "new instructions:",
 *         "system:",
 *         "assistant:"
 *     );
 *
 *     &#64;Override
 *     public ValidationResult validateInput(String userMessage) {
 *         String lowerInput = userMessage.toLowerCase();
 *         for (String pattern : SUSPICIOUS_PATTERNS) {
 *             if (lowerInput.contains(pattern)) {
 *                 return ValidationResult.reject(
 *                     "Input contains potentially malicious content"
 *                 );
 *             }
 *         }
 *         return ValidationResult.accept();
 *     }
 * }
 * </pre>
 *
 * @author Vaadin Ltd
 */
public interface InputValidator extends Serializable {

    /**
     * Validates user text input before sending to the LLM.
     *
     * @param userMessage
     *            the user's message text
     * @return validation result indicating whether the input is acceptable
     */
    default ValidationResult validateInput(String userMessage) {
        return ValidationResult.accept();
    }

    /**
     * Validates a file attachment before including it in the LLM request.
     * <p>
     * This is particularly important for image attachments in multimodal
     * models, as they can contain embedded instructions or malicious content.
     * </p>
     *
     * @param attachment
     *            the file attachment to validate
     * @return validation result indicating whether the attachment is acceptable
     */
    default ValidationResult validateAttachment(
            LLMProvider.Attachment attachment) {
        return ValidationResult.accept();
    }

    /**
     * Result of input validation.
     */
    class ValidationResult implements Serializable {
        private final boolean accepted;
        private final String rejectionMessage;

        private ValidationResult(boolean accepted, String rejectionMessage) {
            this.accepted = accepted;
            this.rejectionMessage = rejectionMessage;
        }

        /**
         * Creates an accepted validation result.
         *
         * @return accepted result
         */
        public static ValidationResult accept() {
            return new ValidationResult(true, null);
        }

        /**
         * Creates a rejected validation result with a message.
         *
         * @param message
         *            the reason for rejection
         * @return rejected result
         */
        public static ValidationResult reject(String message) {
            return new ValidationResult(false, message);
        }

        /**
         * Checks if the input was accepted.
         *
         * @return true if accepted, false if rejected
         */
        public boolean isAccepted() {
            return accepted;
        }

        /**
         * Gets the rejection message if the input was rejected.
         *
         * @return the rejection message, or null if accepted
         */
        public String getRejectionMessage() {
            return rejectionMessage;
        }
    }
}
