/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
