package com.vaadin.addon.charts;

import com.vaadin.flow.model.TemplateModel;
import com.vaadin.router.HasUrlParameter;
import com.vaadin.router.PageTitle;
import com.vaadin.router.Route;
import com.vaadin.router.WildcardParameter;
import com.vaadin.router.event.BeforeNavigationEvent;
import com.vaadin.ui.Tag;
import com.vaadin.ui.common.HtmlImport;
import com.vaadin.ui.common.StyleSheet;
import com.vaadin.ui.event.AttachEvent;
import com.vaadin.ui.polymertemplate.Id;
import com.vaadin.ui.polymertemplate.PolymerTemplate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Route("")
@PageTitle("Vaadin Charts for Flow Demo")
@Tag("charts-demo-app")
@StyleSheet("frontend://style.css")
@HtmlImport("frontend://src/charts-demo-app.html")
public class MainView extends PolymerTemplate<MainView.Model> implements HasUrlParameter<String> {

    public interface Model extends TemplateModel {
        String getCategory();

        void setCategory(String category);

        String getPage();

        void setPage(String page);

        List<Category> getCategories();

        void setCategories(List<Category> categories);
    }

    private static final Map<String, Class<? extends AbstractChartExample>> NAME_INDEXED_SUBTYPES;
    private static final List<Category> CATEGORIES;

    @Id("demo-snippet")
    private DemoSnippet snippet;

    @Id("demo-area")
    private DemoArea demoArea;

    private Pair<String, String> currentExample;

    static {
        final String GROUP_ORDER = "basic,column,bar,pie,area,lineandscatter,dynamic,combinations,"
                + "other,timeline,declarative,container";

        NAME_INDEXED_SUBTYPES = new Reflections("com.vaadin.addon.charts.examples")
                .getSubTypesOf(AbstractChartExample.class)
                .stream()
                .filter(example -> !example.isAnnotationPresent(SkipFromDemo.class))
                .collect(toMap(e -> e.getSimpleName().toLowerCase(), Function.identity()));

        CATEGORIES = NAME_INDEXED_SUBTYPES
                .values()
                .stream()
                .sorted(comparing(Class::getSimpleName))
                .collect(groupingBy(MainView::lastTokenInPackageName))
                .entrySet()
                .stream()
                .map(group -> {
                    Category category = new Category(group.getKey());
                    category.setDemos(group.getValue()
                            .stream()
                            .map(demo -> new Category.Demo(demo.getSimpleName()))
                            .collect(toList()));
                    return category;
                })
                .sorted(comparing(category -> GROUP_ORDER.indexOf(category.getName())))
                .collect(toList());
    }

    public MainView() {
        getModel().setCategories(CATEGORIES);
    }

    @Override
    public void onAttach(AttachEvent e) {
        getElement().getClassList().add("hiddensplitter");
        // TODO(sayo-vaadin): Workaround for Flow not properly handling reroute for wildcard parameters.
        if (currentExample == null) {
            setParameter(null, null);
        }
    }

    @Override
    public void setParameter(BeforeNavigationEvent event, @WildcardParameter String parameter) {
        currentExample = getTargetExample(parameter);
        getModel().setCategory(currentExample.getKey());
        getModel().setPage(currentExample.getValue());

        try {
            Class<? extends AbstractChartExample> exampleClass
                    = NAME_INDEXED_SUBTYPES.get(currentExample.getValue());

            demoArea.setContent(exampleClass.newInstance());
            snippet.setSource(IOUtils.toString(getClass().getResourceAsStream(
                    "/examples/" + currentExample.getKey()
                            + "/" + exampleClass.getSimpleName() + ".java")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Pair<String, String> getTargetExample(String route) {
        Optional<Pair<String, String>> categoryPagePair = split(route);
        if (!categoryPagePair.isPresent()
                || !NAME_INDEXED_SUBTYPES.containsKey(categoryPagePair.get().getValue())) {
            return new ImmutablePair<>("basic", "emptychart");
        }

        return categoryPagePair.get();
    }

    private Optional<Pair<String, String>> split(String route) {
        if (route == null || !route.contains("/")) {
            return Optional.empty();
        }

        String[] tokens = route.split("/");
        return Optional.of(new ImmutablePair<>(tokens[tokens.length - 2], tokens[tokens.length - 1]));
    }

    private static String lastTokenInPackageName(Class<? extends AbstractChartExample> clazz) {
        String name = clazz.getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
