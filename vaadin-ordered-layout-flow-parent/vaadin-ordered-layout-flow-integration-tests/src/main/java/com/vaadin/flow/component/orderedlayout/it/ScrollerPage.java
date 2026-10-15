/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.orderedlayout.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.Route;

/**
 * View for the {@link Scroller}.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ordered-layout/scroller")
public class ScrollerPage extends Div {

    public ScrollerPage() {
        Scroller scroller = new Scroller();
        NativeButton scrollBottomButton = new NativeButton("scroll to bottom",
                e -> scroller.scrollToBottom());
        scrollBottomButton.setId("scroll-to-bottom-button");

        NativeButton scrollTopButton = new NativeButton("scroll to top",
                e -> scroller.scrollToTop());
        scrollTopButton.setId("scroll-to-top-button");

        Div content = new Div("Text ".repeat(30));

        scroller.setWidth("100px");
        scroller.setHeight("100px");

        scroller.setContent(content);
        add(scrollBottomButton, scrollTopButton, scroller);
    }
}
