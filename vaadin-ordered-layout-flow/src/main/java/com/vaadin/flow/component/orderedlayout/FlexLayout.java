/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.orderedlayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

/**
 * A layout component that implements Flexbox. It uses the default
 * flex-direction and doesn't have any predetermined width or height.
 * <p>
 * This component can be used as a base class for more advanced layouts.
 *
 * @see <a href=
 *      "https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Flexible_Box_Layout/Using_CSS_flexible_boxes">Using
 *      CSS Flexible boxes on MDN</a>
 */
@Tag(Tag.DIV)
public class FlexLayout extends Component implements FlexComponent<FlexLayout> {

    /**
     * Default constructor. Creates an empty layout.
     */
    public FlexLayout() {
        getStyle().set("display", "flex");
    }

    /**
     * Convenience constructor to create a layout with the children already
     * inside it.
     *
     * @param children
     *            the items to add to this layout
     * @see #add(Component...)
     */
    public FlexLayout(Component... children) {
        this();
        add(children);
    }

}
