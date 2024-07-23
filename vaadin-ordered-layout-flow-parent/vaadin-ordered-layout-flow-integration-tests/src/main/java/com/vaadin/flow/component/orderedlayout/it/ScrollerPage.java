/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.orderedlayout.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * View for the ordered layouts {@link HorizontalLayout}, {@link VerticalLayout}
 * and {@link Scroller}.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ordered-layout/scroller")
public class ScrollerPage extends AbstractLayout {
    
    public ScrollerPage() {
        Scroller scroller = new Scroller();
        NativeButton scrollBottomButton = new NativeButton("scroll to bottom",
                e -> scroller.scrollToBottom());
        scrollBottomButton.setId("scroll-to-bottom-button");

        NativeButton scrollTopButton = new NativeButton("scroll to top",
                e -> scroller.scrollToTop());
        scrollTopButton.setId("scroll-to-top-button");

        Div content = new Div("Text ".repeat(30));

        scroller.setContent(content);
        add(scrollBottomButton, scrollTopButton, scroller);
    }
}