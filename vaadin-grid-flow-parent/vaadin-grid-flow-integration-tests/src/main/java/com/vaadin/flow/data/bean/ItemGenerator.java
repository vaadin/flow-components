/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.data.bean;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Helper class used for generating stable random data for demo purposes.
 *
 * @author Vaadin Ltd.
 *
 */
public class ItemGenerator extends BeanGenerator {

    public List<Item> generateItems(int amount) {
        LocalDate baseDate = LocalDate.of(2018, 1, 10);
        return IntStream.range(0, amount)
                .mapToObj(index -> createItem(index + 1, baseDate))
                .collect(Collectors.toList());
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
