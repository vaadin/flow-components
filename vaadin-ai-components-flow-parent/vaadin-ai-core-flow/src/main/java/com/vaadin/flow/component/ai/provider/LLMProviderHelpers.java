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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Utility methods for LLM provider implementations.
 * <p>
 * Intended only for internal use and can be removed in the future.
 */
final class LLMProviderHelpers {

    /**
     * JSON Schema substituted when a tool declares no parameters, i.e.
     * {@link LLMProvider.ToolSpec#getParametersSchema()} returns {@code null}
     * or blank. Declares a single optional {@code reason} property so the LLM
     * always has a well-formed, non-empty object to send as arguments: without
     * a declared property, models disagree on what to send — some send an empty
     * string — and at least Anthropic models on Amazon Bedrock reject the
     * request that replays such a tool call.
     * <p>
     * The placeholder never reaches the tool: a provider that substitutes this
     * schema also passes an empty arguments object to
     * {@link LLMProvider.ToolSpec#execute}, so the property declared here is
     * free to change.
     */
    public static final String NO_PARAMETERS_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "reason": {
                  "type": "string",
                  "description": "Optional note on why this tool is being called. Ignored by the tool."
                }
              }
            }""";

    /**
     * Tells whether a tool declares parameters. A tool whose schema is
     * {@code null} or blank takes none: the provider declares
     * {@link #NO_PARAMETERS_SCHEMA} to the LLM in its place and passes an empty
     * arguments object to the tool.
     *
     * @param tool
     *            the tool to check
     * @return {@code true} if the tool declares a parameters schema
     */
    public static boolean hasParameters(LLMProvider.ToolSpec tool) {
        var schema = tool.getParametersSchema();
        return schema != null && !schema.isBlank();
    }

    /**
     * Decodes byte array as UTF-8 text.
     *
     * @param data
     *            the byte array to decode
     * @param fileName
     *            the file name for error messages
     * @param strict
     *            if {@code true}, throws exception on invalid UTF-8; if
     *            {@code false}, replaces invalid sequences
     * @return the decoded string
     * @throws IllegalArgumentException
     *             if {@code strict} is {@code true} and data contains invalid
     *             UTF-8
     */
    public static String decodeAsUtf8(byte[] data, String fileName,
            boolean strict) {
        if (!strict) {
            return new String(data, StandardCharsets.UTF_8);
        }
        var decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return decoder.decode(ByteBuffer.wrap(data)).toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException(
                    "File '" + fileName + "' contains invalid UTF-8 data.", e);
        }
    }

    public static String getBase64Data(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Formats text content as an attachment block.
     *
     * @param fileName
     *            the file name to include in the attachment tag
     * @param content
     *            the text content
     * @return formatted attachment string
     */
    public static String formatTextAttachment(String fileName, String content) {
        return "\n<attachment filename=\"" + fileName + "\">\n" + content
                + "\n</attachment>\n";
    }

    /**
     * Validates that an attachment and its required fields are not
     * {@code null}.
     *
     * @param attachment
     *            the attachment to validate
     * @throws NullPointerException
     *             if attachment, content type, or data is {@code null}
     */
    public static void validateAttachment(AIAttachment attachment) {
        Objects.requireNonNull(attachment, "Attachment must not be null");
        Objects.requireNonNull(attachment.mimeType(),
                "Attachment content type must not be null");
        Objects.requireNonNull(attachment.data(),
                "Attachment data must not be null");
    }

    /**
     * Parses the arguments a model sent for a tool call into the object a
     * {@link LLMProvider.ToolSpec} expects.
     * <p>
     * Missing arguments become an empty object, so a tool that takes no
     * parameters is callable whether or not the model sends anything. Anything
     * else must parse as a JSON object: a model that has nothing to fill
     * sometimes sends a bare value such as {@code ""} instead, which is valid
     * JSON but carries no arguments.
     *
     * @param arguments
     *            the raw argument JSON from the model, may be {@code null} or
     *            blank
     * @return the parsed arguments, never {@code null}
     * @throws IllegalArgumentException
     *             if the arguments are not valid JSON or parse to something
     *             other than a JSON object
     */
    public static ObjectNode parseToolArguments(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return JacksonUtils.createObjectNode();
        }
        JsonNode parsed;
        try {
            parsed = JacksonUtils.getMapper().readTree(arguments);
        } catch (JacksonException e) {
            // The message that goes back to the model keeps the parser
            // diagnostic it can repair from, but not the location suffix
            // full of Java library internals.
            throw new IllegalArgumentException(e.getOriginalMessage(), e);
        }
        if (!parsed.isObject()) {
            // Reported instead of letting the ObjectNode cast fail, so the
            // message that goes back to the model names the shape it should
            // send rather than a Java class cast.
            throw new IllegalArgumentException(
                    "expected a JSON object, but got " + parsed.getNodeType());
        }
        return (ObjectNode) parsed;
    }
}
