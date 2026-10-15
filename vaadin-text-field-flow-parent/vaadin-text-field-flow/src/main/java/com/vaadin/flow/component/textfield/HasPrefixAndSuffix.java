/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;

/**
 * Mixin interface for text-field components that have prefix and suffix slots
 * for inserting components.
 *
 * @deprecated since Vaadin 24, use {@link HasPrefix} and {@link HasSuffix}
 *             interfaces instead.
 *
 * @author Vaadin Ltd
 */
@Deprecated
public interface HasPrefixAndSuffix extends HasPrefix, HasSuffix {
}
