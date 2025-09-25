/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid.it;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("flattened-hierarchy-test")
public class FlattenedHierarchyTestPage extends Div {

    private static final int NUMBER_OF_MANAGERS = 1000;
    private static final int NUMBER_OF_EMPLOYEES_PER_MANAGER = 100;

    private final TreeGrid<Person> treeGrid;
    private final TreeData<Person> treeData;

    private final IntegerField parentIndexField;
    private final IntegerField childIndexField;

    public FlattenedHierarchyTestPage() {
        treeData = new TreeData<>();
        treeData.addItems(getManagers(),
                FlattenedHierarchyTestPage::getEmployees);
        var treeDataProvider = new TreeDataProvider<>(treeData) {
            @Override
            public HierarchyFormat getHierarchyFormat() {
                return HierarchyFormat.FLATTENED;
            }

            @Override
            public int getDepth(Person item) {
                return item.isManager() ? 0 : 1;
            }
        };

        treeGrid = new TreeGrid<>();
        treeGrid.setDataProvider(treeDataProvider);
        treeGrid.setUniqueKeyDataGenerator("key",
                person -> String.valueOf(person.getId()));
        treeGrid.addHierarchyColumn(Person::getFirstName)
                .setHeader("First name");
        treeGrid.addColumn(Person::getLastName).setHeader("Last name");

        add(treeGrid);

        var controls = new HorizontalLayout();
        controls.setSpacing(true);
        controls.setAlignItems(Alignment.END);

        parentIndexField = new IntegerField("Parent index");
        childIndexField = new IntegerField("Child index");

        parentIndexField.setWidth("120px");
        childIndexField.setWidth("120px");
        parentIndexField.setMin(0);
        childIndexField.setMin(0);
        parentIndexField.setStepButtonsVisible(true);
        childIndexField.setStepButtonsVisible(true);
        parentIndexField.setValue(13);
        childIndexField.setValue(6);

        var treeGridScrollToItem = new Button("Scroll to item TreeGrid API");
        treeGridScrollToItem.addClickListener(
                e -> treeGrid.scrollToItem(getItemToScrollTo()));
        var scrollToItem = new Button("Scroll to item (flat index)");
        scrollToItem.addClickListener(
                e -> scrollToItem(getItemToScrollTo(), false));
        var scrollToItemExpand = new Button(
                "Scroll to item (flat index) expand");
        scrollToItemExpand
                .addClickListener(e -> scrollToItem(getItemToScrollTo(), true));
        var scrollToIndex = new Button("Scroll to index");
        scrollToIndex.addClickListener(e -> treeGrid.scrollToIndex(
                parentIndexField.getValue(), childIndexField.getValue()));
        var scrollToIndexExpand = new Button("Scroll to index expand");
        scrollToIndexExpand.addClickListener(e -> {
            expandAllParents(getItemToScrollTo());
            treeGrid.scrollToIndex(parentIndexField.getValue(),
                    childIndexField.getValue());
        });

        var expandAll = new Button("Expand all");
        expandAll.addClickListener(e -> treeGrid.expand(getManagers()));
        var collapseAll = new Button("Collapse all");
        collapseAll.addClickListener(e -> treeGrid.collapse(getManagers()));

        controls.add(parentIndexField, childIndexField, treeGridScrollToItem,
                scrollToItem, scrollToItemExpand, scrollToIndex,
                scrollToIndexExpand, expandAll, collapseAll);

        add(controls);
    }

    private void scrollToItem(Person item, boolean expand) {
        if (expand) {
            expandAllParents(item);
        }
        var itemToScrollTo = getItemToScrollTo(item);
        var itemId = treeGrid.getDataProvider().getId(itemToScrollTo);
        Predicate<Person> itemMatches = itemToMatch -> Objects.equals(itemId,
                treeGrid.getDataProvider().getId(itemToMatch));
        var countBeforeItem = (int) ((HierarchicalDataProvider<Person, Object>) treeGrid
                .getDataCommunicator().getDataProvider())
                .fetchChildren(treeGrid.getDataCommunicator().buildQuery(null,
                        0, Integer.MAX_VALUE))
                .takeWhile(i -> !itemMatches.test(i)).count();
        if (countBeforeItem > 0) {
            treeGrid.scrollToIndex(countBeforeItem);
        }
    }

    private Person getItemToScrollTo() {
        var manager = treeData.getRootItems().get(parentIndexField.getValue());
        return treeData.getChildren(manager).get(childIndexField.getValue());
    }

    private void expandAllParents(Person item) {
        var parent = treeData.getParent(item);
        while (parent != null) {
            treeGrid.expand(parent);
            parent = treeData.getParent(parent);
        }
    }

    private Person getItemToScrollTo(Person item) {
        var itemToScrollTo = item;
        var parent = treeData.getParent(item);
        while (parent != null) {
            if (!treeGrid.isExpanded(parent)) {
                itemToScrollTo = parent;
            }
            parent = treeData.getParent(parent);
        }
        return itemToScrollTo;
    }

    private static List<Person> getEmployees(Person manager) {
        if (!manager.isManager()) {
            return Collections.emptyList();
        }
        return IntStream.range(0, NUMBER_OF_EMPLOYEES_PER_MANAGER)
                .mapToObj(id -> {
                    var person = new Person();
                    person.setFirstName(
                            "FirstEmployee" + manager.getId() + "-" + id);
                    person.setLastName(
                            "LastEmployee" + manager.getId() + "-" + id);
                    person.setManager(false);
                    person.setManagerId(manager.getId());
                    person.setId(NUMBER_OF_MANAGERS
                            + NUMBER_OF_EMPLOYEES_PER_MANAGER * manager.getId()
                            + id);
                    return person;
                }).toList();
    }

    public static List<Person> getManagers() {
        return IntStream.range(0, NUMBER_OF_MANAGERS).mapToObj(id -> {
            var person = new Person();
            person.setFirstName("FirstManager" + id);
            person.setLastName("LastManager" + id);
            person.setManager(true);
            person.setId(id);
            return person;
        }).toList();
    }

    public static class Person {

        private String firstName;

        private String lastName;

        private Integer id;

        private Integer managerId;

        private Boolean manager;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Person other)) {
                return false;
            }
            return Objects.equals(id, other.id);
        }

        public Integer getManagerId() {
            return managerId;
        }

        public void setManagerId(Integer managerId) {
            this.managerId = managerId;
        }

        public boolean isManager() {
            return manager;
        }

        public void setManager(boolean manager) {
            this.manager = manager;
        }
    }
}
