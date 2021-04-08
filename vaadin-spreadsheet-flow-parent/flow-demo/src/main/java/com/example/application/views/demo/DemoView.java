package com.example.application.views.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.example.application.views.demo.views.BasicFunctionalityExample;
import com.example.application.views.demo.views.CollaborativeExample;
import com.example.application.views.demo.views.DataBindingExample;
import com.example.application.views.demo.views.EmbeddedChartsExample;
import com.example.application.views.demo.views.FileUploadExample;
import com.example.application.views.demo.views.FormattingExample;
import com.example.application.views.demo.views.GroupingExample;
import com.example.application.views.demo.views.InlineComponentsExample;
import com.example.application.views.demo.views.ReportModeExample;
import com.example.application.views.demo.views.SimpleInvoiceExample;
import com.example.application.views.main.MainView;
import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

@Route(value = "demo/:demoID", layout = MainView.class)
@JavaScript("prettify.js")
public class DemoView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

    private final Tab tabDemo;
    private final Tab tabSource;
    private final Div pageDemo;
    private final Div pageSource;
    private String title;
    private Pre pre;

    public DemoView() {
        tabDemo = new Tab("Demo");
        tabSource = new Tab("Java Source");

        pageDemo = new Div();
        pageDemo.setSizeFull();
        pageSource = new Div();
        pageSource.setSizeFull();
        pageSource.add(pre = new Pre());
        pre.addClassName("prettyprint");

        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tabDemo, pageDemo);
        tabsToPages.put(tabSource, pageSource);
        Tabs tabs = new Tabs(tabDemo, tabSource);
        Div pages = new Div(pageDemo, pageSource);
        pages.setSizeFull();

        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
        });

        add(tabs, pages);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        String parameter = beforeEnterEvent.getRouteParameters().get("demoID").orElse("");
        title = parameter;
        Class demoClass = null;
        switch (parameter) {
            case "basic": demoClass = BasicFunctionalityExample.class; break;
            case "collaborative": demoClass = CollaborativeExample.class; break;
            case "formatting": demoClass = FormattingExample.class; break;
            case "grouping": demoClass = GroupingExample.class; break;
            case "reportMode": demoClass = ReportModeExample.class; break;
            case "simpleInvoice": demoClass = SimpleInvoiceExample.class; break;
            case "upload": demoClass = FileUploadExample.class; break;
            case "inlineComponents": demoClass = InlineComponentsExample.class; break;
            case "dataBinding": demoClass = DataBindingExample.class; break;
            case "embeddedCharts": demoClass = EmbeddedChartsExample.class; break;
            default: break;
        }
        try {
            pageDemo.add(demoClass != null?(Component) demoClass.newInstance():new Text("" + parameter + " not supported"));
            pre.add(getSource(demoClass));
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private String getSource(Class clazz) throws IOException {
        InputStream resourceAsStream = clazz.getResourceAsStream(clazz.getSimpleName() + ".java");
        String code = IOUtils.toString(resourceAsStream);
        code = code.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
        return code;
    }

    @Override
    public String getPageTitle() {
        return title;
    }
}
