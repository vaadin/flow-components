package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "Page1", layout = AppRouterLayout.class)
public class Page1 extends Div {

    public Page1() {

        String html = 
"<h1>This is Page 1</h1><br>" +
        "<code>@BodySize<br>" +
"@Theme(Lumo.class)<br>" +
"public class AppRouterLayout extends AbstractAppRouterLayout {<br>" +
"<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;@Override<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;protected void configure(AppLayout appLayout) {<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;appLayout.setBranding(new Span(\"Vaadin\").getElement());<br>" +
"<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;IntStream.range(1, 3).forEach(i -><br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;appLayout.addMenuItem(new ActionMenuItem(<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VaadinIcon.SAFE_LOCK.create(), \"Action \" + i,<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;e -> Notification.show(\"Action \" + i + \" executed!\"))));<br>" +
"<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;IntStream.range(1, 3).forEach(i -><br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;appLayout.addMenuItem(new RoutingMenuItem(<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VaadinIcon.LOCATION_ARROW.create(), \"Page \" + i, \"Page\" + i)));<br>" +
"<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;appLayout.addMenuItem(new ActionMenuItem(<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VaadinIcon.USER.create(), \"Logout\",<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;e -> UI.getCurrent().navigate(\"LoggedOut\")));<br>" +
"&nbsp;&nbsp;&nbsp;&nbsp;}<br>" +
"}</code>";

        Div div = new Div();
        div.getElement().setProperty("innerHTML", html);
        add(div);
    }
}
