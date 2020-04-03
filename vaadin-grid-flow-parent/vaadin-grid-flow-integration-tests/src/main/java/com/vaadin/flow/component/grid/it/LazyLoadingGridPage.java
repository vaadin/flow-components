/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("lazy-loading")
public class LazyLoadingGridPage extends Div {

    private static class LazyLoadingProvider
            extends AbstractBackEndDataProvider<String, Void> {

        @Override
        public Stream<String> fetchFromBackEnd(Query<String, Void> query) {
            int limit = query.getLimit();
            int offset = query.getOffset();
            return IntStream.range(offset, offset + limit)
                    .mapToObj(index -> "Item " + index);
        }

        @Override
        protected int sizeInBackEnd(Query<String, Void> query) {
            /*
             * TODO : this method should be removed or implemented simehow in
             * the super class by default
             */
            // this is wrong
            return Integer.MAX_VALUE;
        }

    }

    public LazyLoadingGridPage() {
        Grid<String> grid = new Grid<>();
        grid.setDataProvider(new LazyLoadingProvider());

        grid.addColumn(ValueProvider.identity()).setHeader("Name");

    }
}
