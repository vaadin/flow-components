package com.vaadin.flow.component.treegrid.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.demo.GridDemo.Person;
import com.vaadin.flow.component.grid.demo.PeopleGenerator;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

@Route("vaadin-tree-grid")
@HtmlImport("grid-demo-styles.html")
public class TreeGridDemo extends DemoView {

    /**
     * Example object.
     */
    public static class PersonWithLevel extends Person {

        private int level;

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

    public static final List<PersonWithLevel> rootItems = createRootItems();

    @Override
    protected void initView() {
        createBasicTreeGridUsage();
        createLazyLoadingTreeGridUsage();
    }

    private void createBasicTreeGridUsage() {
        Map<PersonWithLevel, List<PersonWithLevel>> childMap = new HashMap<>();
        TextArea message = new TextArea("");
        message.setHeight("100px");
        message.setReadOnly(true);

        // begin-source-example
        // source-example-heading: TreeGrid Basics
        TreeGrid<PersonWithLevel> grid = new TreeGrid<>();
        grid.setItems(getRootItems(), item -> {
            if ((item.getLevel() == 0 && item.getId() > 10)
                    || item.getLevel() > 1) {
                return Collections.emptyList();
            }
            if (!childMap.containsKey(item)) {
                childMap.put(item, createSubItems(81, item.getLevel() + 1));
            }
            return childMap.get(item);
        });
        grid.addHierarchyColumn(Person::getfirstName).setHeader("Hierarchy");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.addExpandListener(event -> message.setValue(
                String.format("Expanded %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));
        grid.addCollapseListener(event -> message.setValue(
                String.format("Collapsed %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));

        // end-source-example
        grid.setId("treegridbasic");

        TextField name = new TextField("Name of selected person");
        grid.addSelectionListener(event -> name.setValue(event
                .getFirstSelectedItem().map(Person::getfirstName).orElse("")));
        NativeButton save = new NativeButton("Save", event -> {
            grid.getSelectionModel().getFirstSelectedItem()
                    .ifPresent(person -> person.setfirstName(name.getValue()));
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(
                    person -> grid.getDataProvider().refreshItem(person));
        });
        HorizontalLayout nameEditor = new HorizontalLayout(name, save);

        addCard("TreeGrid Basics", withTreeGridToggleButtons(getRootItems(),
                grid, nameEditor, message));
    }

    private <T> Component[] withTreeGridToggleButtons(List<T> roots,
            TreeGrid<T> grid, Component... other) {
        NativeButton toggleFirstItem = new NativeButton("Toggle first item",
                evt -> {
                    if (grid.isExpanded(roots.get(0))) {
                        grid.collapse(roots.get(0));
                    } else {
                        grid.expand(roots.get(0));
                    }
                });
        toggleFirstItem.setId("treegrid-toggle-first-item");
        Div div1 = new Div(toggleFirstItem);

        NativeButton toggleSeveralItems = new NativeButton(
                "Toggle first three items", evt -> {
                    List<T> collapse = new ArrayList<>();
                    List<T> expand = new ArrayList<>();
                    roots.stream().limit(3).collect(Collectors.toList())
                            .forEach(p -> {
                                if (grid.isExpanded(p)) {
                                    collapse.add(p);
                                } else {
                                    expand.add(p);
                                }
                            });
                    if (!expand.isEmpty()) {
                        grid.expand(expand);
                    }
                    if (!collapse.isEmpty()) {
                        grid.collapse(collapse);
                    }
                });
        toggleSeveralItems.setId("treegrid-toggle-first-five-item");
        Div div2 = new Div(toggleSeveralItems);

        NativeButton toggleRecursivelyFirstItem = new NativeButton(
                "Toggle first item recursively", evt -> {
                    if (grid.isExpanded(roots.get(0))) {
                        grid.collapseRecursively(roots.stream().limit(1), 2);
                    } else {
                        grid.expandRecursively(roots.stream().limit(1), 2);
                    }
                });
        toggleFirstItem.setId("treegrid-toggle-first-item-recur");
        Div div3 = new Div(toggleRecursivelyFirstItem);

        NativeButton toggleAllRecursively = new NativeButton(
                "Toggle all recursively", evt -> {
                    List<T> collapse = new ArrayList<>();
                    List<T> expand = new ArrayList<>();
                    roots.forEach(p -> {
                        if (grid.isExpanded(p)) {
                            collapse.add(p);
                        } else {
                            expand.add(p);
                        }
                    });
                    if (!expand.isEmpty()) {
                        grid.expandRecursively(expand, 2);
                    }
                    if (!collapse.isEmpty()) {
                        grid.collapseRecursively(collapse, 2);
                    }
                });
        toggleAllRecursively.setId("treegrid-toggle-all-recur");
        Div div4 = new Div(toggleAllRecursively);

        return Stream.concat(Stream.of(grid, div1, div2, div3, div4),
                Stream.of(other)).toArray(Component[]::new);
    }

    // TreeGrid with lazy loading
    private void createLazyLoadingTreeGridUsage() {
        TextArea message = new TextArea("");
        message.setHeight("100px");
        message.setReadOnly(true);

        // begin-source-example
        // source-example-heading: TreeGrid with lazy loading
        TreeGrid<HierarchicalTestBean> grid = new TreeGrid<>();
        grid.addHierarchyColumn(HierarchicalTestBean::toString)
                .setHeader("Hierarchy");
        grid.addColumn(HierarchicalTestBean::getDepth).setHeader("Depth");
        grid.addColumn(HierarchicalTestBean::getIndex)
                .setHeader("Index on this depth");
        grid.setDataProvider(
                new AbstractBackEndHierarchicalDataProvider<HierarchicalTestBean, Void>() {

                    private final int nodesPerLevel = 3;
                    private final int depth = 2;

                    @Override
                    public int getChildCount(
                            HierarchicalQuery<HierarchicalTestBean, Void> query) {

                        Optional<Integer> count = query.getParentOptional()
                                .flatMap(parent -> Optional.of(Integer
                                        .valueOf((internalHasChildren(parent)
                                                ? nodesPerLevel
                                                : 0))));

                        return count.orElse(nodesPerLevel);
                    }

                    @Override
                    public boolean hasChildren(HierarchicalTestBean item) {
                        return internalHasChildren(item);
                    }

                    private boolean internalHasChildren(
                            HierarchicalTestBean node) {
                        return node.getDepth() < depth;
                    }

                    @Override
                    protected Stream<HierarchicalTestBean> fetchChildrenFromBackEnd(
                            HierarchicalQuery<HierarchicalTestBean, Void> query) {
                        final int depth = query.getParentOptional().isPresent()
                                ? query.getParent().getDepth() + 1
                                : 0;
                        final Optional<String> parentKey = query
                                .getParentOptional()
                                .flatMap(parent -> Optional.of(parent.getId()));

                        List<HierarchicalTestBean> list = new ArrayList<>();
                        int limit = Math.min(query.getLimit(), nodesPerLevel);
                        for (int i = 0; i < limit; i++) {
                            list.add(new HierarchicalTestBean(
                                    parentKey.orElse(null), depth,
                                    i + query.getOffset()));
                        }
                        return list.stream();
                    }
                });

        // end-source-example
        grid.setId("treegridlazy");

        grid.addExpandListener(event -> message.setValue(
                String.format("Expanded %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));
        grid.addCollapseListener(event -> message.setValue(
                String.format("Collapsed %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));

        addCard("TreeGrid with lazy loading", withTreeGridToggleButtons(grid
                .getDataProvider()
                .fetch(new HierarchicalQuery<HierarchicalTestBean, SerializablePredicate<HierarchicalTestBean>>(
                        null, null))
                .collect(Collectors.toList()), grid, message));
    }

    private static List<PersonWithLevel> getRootItems() {
        return rootItems;
    }

    private static List<PersonWithLevel> createRootItems() {
        return createSubItems(500, 0);
    }

    private static List<PersonWithLevel> createSubItems(int number, int level) {
        return new PeopleGenerator().generatePeopleWithLevels(number, level);
    }

}
