/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.demo.data;

import java.util.ArrayList;
import java.util.List;

public class StatesData {
    private static final List<String> STATE_LIST = createStateList();

    private static List<String> createStateList() {
        List<String> stateList = new ArrayList<>();

        stateList.add("Alabama");
        stateList.add("California");
        stateList.add("Florida");
        stateList.add("Georgia");
        stateList.add("Maryland");
        stateList.add("Michigan");
        stateList.add("Nevada");
        stateList.add("New York");
        stateList.add("Ohio");
        stateList.add("Washington");

        return stateList;
    }

    public List<String> getAllStates() {
        return STATE_LIST;
    }
}
