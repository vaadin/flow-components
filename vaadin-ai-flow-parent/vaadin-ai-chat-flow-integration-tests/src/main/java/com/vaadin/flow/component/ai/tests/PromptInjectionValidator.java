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
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.ai.orchestrator.InputValidator;
import com.vaadin.flow.component.ai.provider.LLMProvider;

import java.util.List;

/**
 * Example input validator that detects common prompt injection patterns.
 * <p>
 * This validator scans user input for suspicious patterns that might indicate
 * prompt injection attacks. It provides basic protection against common attack
 * vectors in multimodal AI applications.
 * </p>
 * <p>
 * <strong>Note:</strong> This is a demonstration implementation. Production
 * applications should use more sophisticated detection mechanisms and consider
 * integrating with specialized security tools.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class PromptInjectionValidator implements InputValidator {

    /**
     * Common patterns that might indicate prompt injection attempts.
     */
    private static final List<String> SUSPICIOUS_PATTERNS = List.of(
            "ignore previous instructions",
            "ignore all previous",
            "disregard previous",
            "forget previous",
            "new instructions:",
            "system:",
            "assistant:",
            "you are now",
            "act as if",
            "pretend you are",
            "override your",
            "bypass your",
            "<|endoftext|>",
            "<|im_start|>",
            "<|im_end|>");

    /**
     * Maximum allowed length for user input to prevent resource exhaustion.
     */
    private static final int MAX_INPUT_LENGTH = 10000;

    /**
     * Maximum file size for attachments (5 MB).
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public ValidationResult validateInput(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ValidationResult.reject("Input cannot be empty");
        }

        // Check length
        if (userMessage.length() > MAX_INPUT_LENGTH) {
            return ValidationResult
                    .reject("Input too long (max " + MAX_INPUT_LENGTH
                            + " characters)");
        }

        // Check for suspicious patterns (case-insensitive)
        String lowerInput = userMessage.toLowerCase();
        for (String pattern : SUSPICIOUS_PATTERNS) {
            if (lowerInput.contains(pattern)) {
                return ValidationResult.reject(
                        "Input contains potentially malicious content. Please rephrase your message.");
            }
        }

        // Check for excessive special characters (might indicate encoding
        // attacks)
        long specialCharCount = userMessage.chars()
                .filter(c -> !Character.isLetterOrDigit(c)
                        && !Character.isWhitespace(c))
                .count();
        double specialCharRatio = (double) specialCharCount
                / userMessage.length();
        if (specialCharRatio > 0.3) {
            return ValidationResult.reject(
                    "Input contains too many special characters");
        }

        return ValidationResult.accept();
    }

    @Override
    public ValidationResult validateAttachment(
            LLMProvider.Attachment attachment) {
        if (attachment == null) {
            return ValidationResult.reject("Invalid attachment");
        }

        // Check file size
        byte[] data = attachment.data();
        if (data != null && data.length > MAX_FILE_SIZE) {
            return ValidationResult.reject(
                    "File too large (max " + (MAX_FILE_SIZE / 1024 / 1024)
                            + " MB)");
        }

        // Check content type
        String contentType = attachment.contentType();
        if (contentType == null || contentType.isEmpty()) {
            return ValidationResult.reject("Unknown file type");
        }

        // Allow only specific content types
        if (!isAllowedContentType(contentType)) {
            return ValidationResult.reject(
                    "File type not allowed: " + contentType);
        }

        // For images, could add additional checks here:
        // - Check for embedded metadata that might contain instructions
        // - Validate image format
        // - Check image dimensions
        // - Scan for steganography

        return ValidationResult.accept();
    }

    /**
     * Checks if a content type is allowed.
     *
     * @param contentType
     *            the content type to check
     * @return true if allowed, false otherwise
     */
    private boolean isAllowedContentType(String contentType) {
        return contentType.startsWith("image/png")
                || contentType.startsWith("image/jpeg")
                || contentType.startsWith("image/jpg")
                || contentType.startsWith("image/gif")
                || contentType.startsWith("image/webp")
                || contentType.startsWith("application/pdf")
                || contentType.startsWith("text/plain");
    }
}
