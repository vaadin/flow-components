package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * Control for https://github.com/vaadin/flow-components/issues/1103 - lets us
 * reach {@code /repro-1103} through client-side (SPA) navigation, which the
 * 2025 comment says opens the overlay correctly, unlike a direct load.
 */
@Route("repro-1103-nav")
public class Repro1103NavView extends VerticalLayout {

    public Repro1103NavView() {
        RouterLink link = new RouterLink("go to repro-1103",
                Repro1103View.class);
        link.setId("nav-link");
        add(new NativeButton("noop"), link);
    }
}
