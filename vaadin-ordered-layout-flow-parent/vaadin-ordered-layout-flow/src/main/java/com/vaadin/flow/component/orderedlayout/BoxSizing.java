/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
