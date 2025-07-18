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
package com.vaadin.flow.component.treegrid;

import com.vaadin.flow.component.grid.GridArrayUpdater;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater;

/**
 * Array update strategy aware class for TreeGrid.
 *
 * @author Vaadin Ltd
 * @deprecated since 24.9 and will be removed in Vaadin 25. Use
 *             {@link GridArrayUpdater} instead.
 */
@Deprecated(since = "24.9", forRemoval = true)
public interface TreeGridArrayUpdater
        extends GridArrayUpdater, HierarchicalArrayUpdater {

}
