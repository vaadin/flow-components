package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * A series which is based on data from a DataProvider.
 * <p>
 * You must use {@link #setY(SerializableFunction)} to define which part of the
 * data bean to use as <code>y</code> values.
 * <p>
 * Note that even if you use a lazy loading {@link DataProvider}, this series
 * will work in an eager fashion and load all the data from the provider at
 * once.
 */
public class DataProviderSeries<T> extends AbstractSeries {

    @JsonIgnore
    private final DataProvider<T, ?> dataProvider;
    public static final String X_ATTRIBUTE = "x";
    public static final String Y_ATTRIBUTE = "y";
    private static final String NAME_ATTRIBUTE = "name";

    public static final String HIGH_PROPERTY = "high";
    public static final String LOW_PROPERTY = "low";
    public static final String OPEN_PROPERTY = "open";
    public static final String CLOSE_PROPERTY = "close";

    @JsonIgnore
    private final Map<String, SerializableFunction<T, Object>> chartAttributeToCallback;

    @JsonIgnore
    private boolean automaticChartUpdateEnabled = true;

    @JsonIgnore
    private Registration dataProviderRegistration;

    @JsonIgnore
    private DataProviderListener<T> listener = (DataProviderListener<T>) event -> {
        updateSeries();
    };

    /**
     * Creates a new series using data from the given data provider.
     * <p>
     * Many chart types such as {@link ChartType#BAR}, {@link ChartType#LINE},
     * {@link ChartType#AREA} etc use {@code y} values to define the data points
     * to show in the chart. For these chart types you should use either
     * {@link #DataProviderSeries(DataProvider, SerializableFunction)} or
     * {@link #setY(SerializableFunction)} to define the function (lambda) which
     * extracts the values from the bean in the provider.
     * <p>
     * Other chart types such as {@link ChartType#ERRORBAR} do not require
     * {@code y} values but instead {@code high} and {@code low} values.
     * Functions for extracting these are set using
     * {@link #setHigh(SerializableFunction)} and
     * {@link #setLow(SerializableFunction)} respectively.
     *
     * @param dataProvider
     *            the data provider which contains the data
     */
    public DataProviderSeries(DataProvider<T, ?> dataProvider) {
        this.dataProvider = dataProvider;
        chartAttributeToCallback = new HashMap<>();
        dataProviderRegistration = dataProvider
                .addDataProviderListener(listener);
    }

    /**
     * Creates a new series using data from the given data provider and y
     * values.
     *
     * @param dataProvider
     *            the data provider which contains the data
     * @param callBack
     *            the function which retrieves the y values
     */
    public DataProviderSeries(DataProvider<T, ?> dataProvider,
            SerializableFunction<T, Object> callBack) {
        this(dataProvider);
        setY(callBack);
    }

    /**
     * Sets the function used for retrieving the value for the given property
     * name from the given data provider.
     *
     * @param propertyName
     *            the property name
     * @param callBack
     *            the function which retrieves the value for the property
     */
    public void setProperty(String propertyName,
            SerializableFunction<T, Object> callBack) {
        chartAttributeToCallback.put(propertyName, callBack);
    }

    /**
     * Sets the function used for retrieving <code>x</code> values from the bean
     * provided by the data provider.
     * <p>
     * How exactly the values are used depends on the used chart type.
     *
     * @param callBack
     *            the function which retrieves the values
     */
    public void setX(SerializableFunction<T, Object> callBack) {
        setProperty(X_ATTRIBUTE, callBack);
    }

    /**
     * Sets the function used for retrieving <code>y</code> values from the bean
     * provided by the data provider.
     * <p>
     * How exactly the values are used depends on the used chart type.
     *
     * @param callBack
     *            the function which retrieves the values
     */
    public void setY(SerializableFunction<T, Object> callBack) {
        setProperty(Y_ATTRIBUTE, callBack);
    }

    /**
     * Sets the function used for retrieving <code>name</code> values from the
     * bean provided by the data provider.
     * <p>
     * How exactly the values are used depends on the used chart type.
     *
     * @param callBack
     *            the function which retrieves the values
     */
    public void setPointName(SerializableFunction<T, Object> callBack) {
        setProperty(NAME_ATTRIBUTE, callBack);
    }

    /**
     * Sets the function used for retrieving <code>low</code> values from the
     * bean provided by the data provider.
     * <p>
     * How exactly the values are used depends on the used chart type.
     *
     * @param callBack
     *            the function which retrieves the values
     */
    public void setLow(SerializableFunction<T, Object> callBack) {
        setProperty(LOW_PROPERTY, callBack);
    }

    /**
     * Sets the function used for retrieving <code>high</code> values from the
     * bean provided by the data provider.
     * <p>
     * How exactly the values are used depends on the used chart type.
     *
     * @param callBack
     *            the function which retrieves the values
     */
    public void setHigh(SerializableFunction<T, Object> callBack) {
        setProperty(HIGH_PROPERTY, callBack);
    }

    /**
     * Sets the function used for retrieving <code>open</code> values from the
     * bean provided by the data provider.
     * <p>
     * How exactly the values are used depends on the used chart type.
     *
     * @param callBack
     *            the function which retrieves the values
     */
    public void setOpen(SerializableFunction<T, Object> callBack) {
        setProperty(OPEN_PROPERTY, callBack);
    }

    /**
     * Sets the function used for retrieving <code>close</code> values from the
     * bean provided by the data provider.
     * <p>
     * How exactly the values are used depends on the used chart type.
     *
     * @param callBack
     *            the function which retrieves the values
     */
    public void setClose(SerializableFunction<T, Object> callBack) {
        setProperty(CLOSE_PROPERTY, callBack);
    }

    /**
     * Returns the underlying data provider.
     *
     * @return the underlying data provider.
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider;
    }

    /**
     * Returns a list mappings between chart attributes(keys) and values. For
     * example: x->1, x->2, y->2, y->3 for linear chart
     *
     * @return
     */

    public List<Map<String, Optional<Object>>> getValues() {
        return dataProvider.fetch(new Query<>())
                .map((item) -> chartAttributeToCallback.entrySet().stream()
                        .collect(toMap(Entry::getKey,
                                entry -> (entry.getValue() != null)
                                        ? Optional.ofNullable(
                                                entry.getValue().apply(item))
                                        : Optional.empty())))
                .collect(toList());
    }

    /**
     * Returns a set of chart attributes(keys).
     *
     * @return
     */
    public Set<String> getChartAttributes() {
        return chartAttributeToCallback.keySet();
    }

    /**
     * Returns true if the chart is updated automatically when a DataChangeEvent
     * is emitted by the data provider. Default is true.
     *
     * @return
     */
    public boolean isAutomaticChartUpdateEnabled() {
        return automaticChartUpdateEnabled;
    }

    /**
     * Sets if the chart should be updated automatically when a DataChangeEvent
     * is emitted by the data provider. Default is true.
     *
     * @param automaticChartUpdateEnabled
     *            True sets the chart updating to enabled, false disables it.
     */
    public void setAutomaticChartUpdateEnabled(
            boolean automaticChartUpdateEnabled) {
        this.automaticChartUpdateEnabled = automaticChartUpdateEnabled;

        if (automaticChartUpdateEnabled) {
            if (dataProviderRegistration == null) {
                // TODO Enable it again
                dataProviderRegistration = dataProvider
                        .addDataProviderListener(listener);
            }
        } else {
            if (dataProviderRegistration != null) {
                dataProviderRegistration.remove();
                dataProviderRegistration = null;
            }
        }
    }
}
