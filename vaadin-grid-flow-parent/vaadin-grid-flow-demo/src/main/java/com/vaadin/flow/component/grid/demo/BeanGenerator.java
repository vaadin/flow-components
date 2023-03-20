
package com.vaadin.flow.component.grid.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Base class for other random data generators used in the demo.
 * 
 * @author Vaadin Ltd.
 *
 */
abstract class BeanGenerator {

    /*
     * Each property of the bean should use its own Random instance. This
     * ensures that new added fields don't change the values of old fields
     * already present in the bean.
     */
    private final Map<String, Random> randomMap = new HashMap<>();

    protected Random getRandom(String propertyName) {
        return randomMap.computeIfAbsent(propertyName, key -> new Random(0));
    }
}
