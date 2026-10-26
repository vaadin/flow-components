/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.tests.providers;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.ToolException;

class CollectionQueryTest {

    @Test
    void parsesEveryPart() {
        var query = CollectionQuery
                .parse("""
                        {"source": "sales", "columns": {"_name": "month", "_y": "revenue"},
                         "filter": {"region": "North"}, "sort": [{"field": "monthOrder", "direction": "desc"}]}""");
        Assertions.assertEquals("sales", query.source());
        Assertions.assertEquals(List.of("_name", "_y"),
                List.copyOf(query.columns().keySet()));
        Assertions.assertEquals(Map.of("region", "North"), query.filter());
        Assertions.assertEquals(
                List.of(new CollectionQuery.Sort("monthOrder", true)),
                query.sort());
        Assertions.assertEquals(
                List.of("month", "revenue", "region", "monthOrder"),
                query.fields());
        Assertions.assertEquals("revenue", query.fieldOf("_y"));
    }

    @Test
    void projectsFiltersAndSorts() {
        var query = CollectionQuery
                .parse("""
                        {"source": "t", "columns": {"Name": "name", "Pay": "salary"},
                         "filter": {"dept": "Sales"}, "sort": [{"field": "salary", "direction": "desc"}]}""");
        var alice = Map.<String, Object> of("name", "Alice", "dept", "Sales",
                "salary", 70);
        var bob = Map.<String, Object> of("name", "Bob", "dept", "Sales",
                "salary", 90L);
        var carl = Map.<String, Object> of("name", "Carl", "dept", "Ops",
                "salary", 50);
        Assertions.assertTrue(query.matches(alice));
        Assertions.assertFalse(query.matches(carl));
        Assertions.assertTrue(CollectionQuery.parse(
                "{\"source\":\"t\",\"columns\":{\"n\":\"name\"},\"filter\":{\"salary\":\"70\"}}")
                .matches(alice), "filters compare as text too");
        Assertions.assertTrue(query.comparator().compare(bob, alice) < 0,
                "descending puts the higher salary first");
        Assertions.assertEquals(Map.of("Name", "Alice", "Pay", 70),
                query.project(alice));
    }

    @Test
    void rejectsQueriesTheEndpointsCannotAnswer() {
        assertRejected("not json", "JSON object");
        assertRejected("[1, 2]", "JSON object");
        assertRejected("{\"columns\": {\"a\": \"b\"}}", "source");
        assertRejected("{\"source\": \"t\"}", "columns");
        assertRejected("{\"source\": \"t\", \"columns\": {}}", "columns");
        assertRejected(
                "{\"source\": \"t\", \"columns\": {\"total\": {\"$sum\": \"x\"}}}",
                "not an expression");
        assertRejected(
                "{\"source\": \"t\", \"columns\": {\"a\": \"b\"}, \"filter\": {\"x\": {\"$gt\": 1}}}",
                "only equality");
        assertRejected(
                "{\"source\": \"t\", \"columns\": {\"a\": \"b\"}, \"sort\": \"b desc\"}",
                "sort");
    }

    private static void assertRejected(String json, String messagePart) {
        var e = Assertions.assertThrows(ToolException.class,
                () -> CollectionQuery.parse(json), json);
        Assertions.assertTrue(e.getMessage().contains(messagePart),
                e.getMessage());
    }
}
