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
package com.vaadin.flow.component.orderedlayout;

/**
 * Enum for the values of the CSS property {@code box-sizing}.
 *
 *
 * @author Vaadin Ltd.
 * @see <a href=
 *      "https://developer.mozilla.org/en-US/docs/Web/CSS/box-sizing">MDN page
 *      about box-sizing</a>
 *
 */
public enum BoxSizing {

    /**
     * When the box-sizing is undefined, it's up to the element client-side
     * implementation to define how its internal the box model should be
     * defined.
     */
    UNDEFINED,

    /**
     * Sets the default CSS box-sizing behavior. If you set an element's width
     * to 100 pixels, then the element's content box will be 100 pixels wide,
     * and the width of any border or padding will be added to the final
     * rendered width.
     */
    CONTENT_BOX,

    /**
     * Tells the browser to account for any border and padding in the values you
     * specify for an element's width and height. If you set an element's width
     * to 100 pixels, that 100 pixels will include any border or padding you
     * added, and the content box will shrink to absorb that extra width. This
     * typically makes it much easier to size elements.
     */
    BORDER_BOX;

}
