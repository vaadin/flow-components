package com.example.application.views.main;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.PageTitle;

import com.example.application.views.demo.DemoView;
import com.example.application.views.demoUI.DemoUIView;

/**
 * The main view is a top-level placeholder for other views.
 */
@CssImport("./views/main/main-view.css")
public class MainView extends AppLayout {

    private final Tabs menu;
    private H1 viewTitle;

    public MainView() {
        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        menu = createMenu();
        addToDrawer(createDrawerContent(menu));
    }

    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        viewTitle = new H1();
        layout.add(viewTitle);
        layout.add(new Avatar());
        return layout;
    }

    private Component createDrawerContent(Tabs menu) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.add(new Image("images/logo.png", "demo logo"));
        logoLayout.add(new H1("demo"));
        layout.add(logoLayout, menu);
        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {
        return new Tab[]{createTab("DemoUI", DemoUIView.class, "Multiple tests")
                , createTab("Basic functionality", DemoView.class, "basic", "Edit imported Excel file with "
                        + "<br>formatting, basic formulas, and a <br>chart. "
                        + "Updates dynamically when <br> values are edited.")
                , createTab("Collaborative features", DemoView.class, "collaborative", "Freeze panes, protected cells <br> and add comments")
                , createTab("Formatting", DemoView.class, "formatting", "Style your spreadsheet")
                , createTab("Grouping", DemoView.class, "grouping", "Use the Excel feature for <br> grouping rows and colums")
                , createTab("Report mode", DemoView.class, "reportMode", "Use the read only mode <br> of spreadsheet")
                , createTab("Simple invoice", DemoView.class, "simpleInvoice", "Use the spreadsheet for invoices")
                , createTab("Upload Excel files", DemoView.class, "upload", "Upload a .xlsx or .xls file")
                , createTab("Use inline components", DemoView.class, "inlineComponents", "Use Vaadin components within <br> a spreadsheet", false)
                , createTab("Data binding", DemoView.class, "dataBinding", "Display spreadsheet data <br> using Vaadin charts", false)
                , createTab("Embedded charts", DemoView.class, "embeddedCharts", "Display charts from an Excel file <br> in the spreadsheet", false)
        };
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget, String description) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        tab.getElement().setAttribute("title", description);
        return tab;
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget, String parameter, String description) {
        return createTab(text, navigationTarget, parameter, description, true);
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget, String parameter, String description, boolean enabled) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget, new RouteParameters("demoID", parameter)));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        tab.getElement().setAttribute("title", description);
        tab.setEnabled(enabled);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);
        viewTitle.setText(getCurrentPageTitle());
    }

    private Optional<Tab> getTabForComponent(Component component) {
        return menu.getChildren().filter(tab -> ComponentUtil.getData(tab, Class.class).equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        if (title != null) return title.value();
        else if (getContent() instanceof HasDynamicTitle) return ((HasDynamicTitle)getContent()).getPageTitle();
        else return "";
    }
}
