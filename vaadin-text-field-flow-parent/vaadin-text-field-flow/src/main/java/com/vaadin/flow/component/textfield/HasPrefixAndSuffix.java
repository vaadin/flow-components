/*
 * Copyright 2000-2022 Vaadin Ltd.
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
