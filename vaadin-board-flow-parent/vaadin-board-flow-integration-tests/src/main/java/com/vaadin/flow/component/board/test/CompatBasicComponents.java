package com.vaadin.flow.component.board.test;

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;

/**
 * http://localhost:8080/Dash24BasicComponents$ButtonUI
 */
public class CompatBasicComponents {

    public static class ButtonView extends CompatBasicView {
        @Override
        protected Component createTestComponent() {
            return new Button();
        }
    }

    // DASH-106
    public static class GridView extends CompatBasicView {

        private static class Person {
            private String name;
            private int birthYear;

            public Person(String name, int birthYear) {
                this.name = name;
                this.birthYear = birthYear;
            }

            public String getName() {
                return name;
            }

            public int getBirthYear() {
                return birthYear;
            }
        }

        @Override
        protected Component createTestComponent() {
            List<Person> people = Arrays.asList(
                    new Person("Nicolaus Copernicus", 1543),
                    new Person("Galileo Galilei", 1564),
                    new Person("Johannes Kepler", 1571));
            Grid<Person> grid = new Grid<>();
            grid.setItems(people);
            grid.addColumn(Person::getName).setHeader("Name");
            grid.addColumn(Person::getBirthYear).setHeader("Year of birth");
            grid.setWidth("100%");
            return grid;
        }

    }

}
