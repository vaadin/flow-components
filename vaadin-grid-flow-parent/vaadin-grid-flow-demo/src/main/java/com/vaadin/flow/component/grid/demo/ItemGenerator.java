/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.demo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.demo.GridDemo.Item;

/**
 * Helper class used for generating stable random data for demo purposes.
 *
 * @author Vaadin Ltd.
 *
 */
class ItemGenerator extends BeanGenerator {

    private final int numberOfRows;

    public ItemGenerator() {
        numberOfRows = -1;
    }

    public ItemGenerator(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public List<Item> generateItems(int amount) {
        LocalDate baseDate = LocalDate.of(2018, 1, 10);
        return IntStream.range(0, amount)
                .mapToObj(index -> createItem(index + 1, baseDate))
                .collect(Collectors.toList());
    }

    public Stream<Item> generateItems(int offset, int limit) {
        LocalDate baseDate = LocalDate.of(2020, 1, 10);
        int end = offset + limit;
        if (numberOfRows > 0 && numberOfRows < end) {
            end = numberOfRows;
        }
        return IntStream.range(offset, end)
                .mapToObj(index -> createItem(index + 1, baseDate));
    }

    private Item createItem(int index, LocalDate baseDate) {
        Item item = new Item();
        item.setName("Item " + index);
        item.setPrice(100 * getRandom("price").nextDouble());
        item.setPurchaseDate(baseDate.atTime(12, 0).minus(
                1 + getRandom("purchaseDate").nextInt(3600),
                ChronoUnit.SECONDS));
        item.setEstimatedDeliveryDate(baseDate.plus(
                1 + getRandom("estimatedDeliveryDate").nextInt(15),
                ChronoUnit.DAYS));
        return item;
    }
}
