/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.sass.linker;

import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.Shardable;

/**
 * Pre-linker that checks for the existence of SASS files in public folders,
 * compiles them to CSS files with the SassCompiler from Vaadin and adds the CSS
 * back into the artifact.
 *
 */
@LinkerOrder(Order.PRE)
@Shardable
public class SassLinker extends AbstractLinker {

    @Override
    public String getDescription() {
        return "Fake SassLinker for spreadsheet";
    }

}
