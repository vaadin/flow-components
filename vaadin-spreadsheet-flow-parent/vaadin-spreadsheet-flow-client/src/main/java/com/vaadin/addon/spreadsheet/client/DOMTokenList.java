/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * A GWT wrapper for the native JavaScript {@code DOMTokenList} interface.
 * <p>
 * This represents a set of space-separated tokens, primarily used for the
 * {@code part} property of an HTML element, allowing for manipulation of its
 * CSS classes.
 *
 * @see <a href=
 *      "https://developer.mozilla.org/en-US/docs/Web/API/DOMTokenList">DOMTokenList
 *      on MDN</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface DOMTokenList {
    /**
     * Adds the given token to the list.
     * <p>
     * If the token already exists in the list, it is not added again.
     *
     * @param token
     *            The token to add to the list.
     */
    void add(String... token);

    /**
     * Removes the given token from the list. If the token does not exist in the
     * list, no action is taken.
     *
     * @param token
     *            The string token to remove from the list.
     */
    void remove(String token);

    /**
     * Returns a boolean value indicating whether the list contains the given
     * token.
     *
     * @param token
     *            The string token to check for.
     * @return {@code true} if the token is found, otherwise {@code false}.
     */
    boolean contains(String token);

    /**
     * Removes the given token from the list if it is present, and adds it if it
     * is not.
     * 
     * @param token
     *            The token to toggle.
     * @return {@code true} if the token is now present in the list, and
     *         {@code false} otherwise.
     */
    boolean toggle(String token);

    /**
     * Turns the toggle into a one way-only operation. If the `force` parameter
     * is true, this method adds the specified token if it's not already
     * present. If `force` is false, it removes the token if it is present.
     *
     * @param token
     *            The token to toggle.
     * @param force
     *            If true, the token is added. If false, the token is removed.
     * @return {@code true} if the token is now present in the list, and
     *         {@code false} otherwise.
     */
    boolean toggle(String token, boolean force);

    /**
     * Replaces an existing token with a new token.
     * <p>
     * If the token to replace is not found in the list, this method does
     * nothing and returns {@code false}. If the token is found, it is replaced
     * by the new token and the method returns {@code true}.
     *
     * @param oldToken
     *            The token to be replaced.
     * @param newToken
     *            The new token.
     * @return {@code true} if the replacement was successful, {@code false}
     *         otherwise.
     */
    boolean replace(String oldToken, String newToken);

    /**
     * Gets the number of tokens in the list.
     *
     * @return the number of tokens.
     */
    @JsProperty
    int getLength();

    /**
     * Gets the string representation of the token list.
     *
     * @return a space-separated string containing all the tokens in the list.
     */
    @JsProperty
    String getValue();
}
