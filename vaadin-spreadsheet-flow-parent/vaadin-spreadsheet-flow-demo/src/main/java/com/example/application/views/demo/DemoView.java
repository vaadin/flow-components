package com.example.application.views.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.NpmPackage;
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
@NpmPackage(value = "code-prettify", version = "0.1.0")
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

        pageSource.setVisible(false);
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
        Component demoInstance;
        switch (parameter) {
            case "basic": demoInstance = new BasicFunctionalityExample(); break;
            case "collaborative": demoInstance = new CollaborativeExample(); break;
            case "formatting": demoInstance = new FormattingExample(); break;
            case "grouping": demoInstance = new GroupingExample(); break;
            case "reportMode": demoInstance = new ReportModeExample(); break;
            case "simpleInvoice": demoInstance = new SimpleInvoiceExample(); break;
            case "upload": demoInstance = new FileUploadExample(); break;
            case "inlineComponents": demoInstance = new InlineComponentsExample(); break;
            case "dataBinding": demoInstance = new DataBindingExample(); break;
            case "embeddedCharts": demoInstance = new EmbeddedChartsExample(); break;
            default: demoInstance = new Text("demoID " + parameter + " is not supported"); break;
        }
        try {
            pageDemo.removeAll();
            pageDemo.add(demoInstance);
            pre.removeAll();
            pre.add(getSource(demoInstance.getClass()));
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private String getSource(Class clazz) throws IOException {
        InputStream resourceAsStream = clazz.getResourceAsStream(clazz.getSimpleName() + ".java");
        
        String code = "";
        if (resourceAsStream != null) {
            code = IOUtils.toString(resourceAsStream);
        }  else {
        	File file = new File("./src/main/java/", clazz.getName().replace('.', '/') + ".java");
        	if (file.canRead()) {
            	code = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        	}
        } 
        if (code != null) {
            code = code.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;");
        }
        return code;
    }

    @Override
    public String getPageTitle() {
        return title;
    }
}
