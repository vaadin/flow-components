package com.vaadin.flow.component.charts;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import com.vaadin.flow.templatemodel.TemplateModel;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FilenameMatchProcessor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Route("")
@PageTitle("Charts for Vaadin Flow")
@Tag("charts-demo-app")
@StyleSheet("frontend://style.css")
@HtmlImport("frontend://src/charts-demo-app.html")
@JavaScript("frontend://src/google-analytics.js")
public class MainView extends PolymerTemplate<MainView.Model> implements HasUrlParameter<String> {

    public interface Model extends TemplateModel {
        void setCategory(String category);

        void setPage(String page);

        void setCategories(List<Category> categories);

        void setVersion(String version);
    }

    private static final Map<String, Class<? extends AbstractChartExample>> NAME_INDEXED_SUBTYPES;
    private static final List<Category> CATEGORIES;
    private static final Set<String> STYLE_FILEPATHS;
    private static final Properties PROPERTIES = new Properties();

    @Id("java-snippet")
    private DemoSnippet javaSnippet;

    @Id("style-snippet")
    private DemoSnippet styleSnippet;

    @Id("demo-area")
    private DemoArea demoArea;

    private Pair<String, String> currentExample;

    static {
        final String GROUP_ORDER = "column,bar,pie,area,lineandscatter,dynamic,combinations,"
                + "other,timeline,declarative,container";

        NAME_INDEXED_SUBTYPES = new Reflections("com.vaadin.flow.component.charts.examples")
                .getSubTypesOf(AbstractChartExample.class)
                .stream()
                .filter(example -> !example.isAnnotationPresent(SkipFromDemo.class))
                .collect(toMap(Class::getSimpleName, Function.identity()));

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

        STYLE_FILEPATHS = new HashSet<>();
        new FastClasspathScanner()
                .matchFilenamePattern("examples/.*\\.html",
                        (FilenameMatchProcessor) (classpathElt, relativePath) ->
                                STYLE_FILEPATHS.add(relativePath))
                .scan();

        try {
            PROPERTIES.load(MainView.class
                    .getResourceAsStream("config.properties"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public MainView() {
        getModel().setCategories(CATEGORIES);
        getModel().setVersion(getVersion());

        // Preload all themes because Valo theme engine
        // does not support live theme reloading.
        STYLE_FILEPATHS.forEach(style -> UI.getCurrent().getPage().addHtmlImport(style));
    }

    static String getVersion() {
        return (String) PROPERTIES.get("charts.version");
    }

    @Override
    public void onAttach(AttachEvent e) {
        getElement().getClassList().add("hiddensplitter");
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        currentExample = getTargetExample(event, parameter);

        try {
            Class<? extends AbstractChartExample> exampleClass
                    = NAME_INDEXED_SUBTYPES.get(currentExample.getValue());

            String category = lastTokenInPackageName(exampleClass);
            getModel().setCategory(category);
            getModel().setPage(currentExample.getValue());

            demoArea.setContent(exampleClass.newInstance());

            final String exampleName = "/examples/" + category
                    + "/" + exampleClass.getSimpleName();

            javaSnippet.setSource(IOUtils.toString(getClass().getResourceAsStream(
                    exampleName + ".java"), "UTF-8"));

            try {
                styleSnippet.setSource(IOUtils.toString(getClass().getResourceAsStream(
                        exampleName + ".html"), "UTF-8"));
            } catch (NullPointerException expected) {
                styleSnippet.setSource(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Pair<String, String> getTargetExample(BeforeEvent event,
                                                  String route) {
        Pair<String, String> categoryPagePair = new ImmutablePair<>(null, route);
        if (StringUtils.isEmpty(route) || !NAME_INDEXED_SUBTYPES
                .containsKey(categoryPagePair.getValue())) {
            UI.getCurrent().navigate("Column");
            return new ImmutablePair<>("column", "Column");
        }

        return categoryPagePair;
    }

    private static String lastTokenInPackageName(Class<? extends AbstractChartExample> clazz) {
        String name = clazz.getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
