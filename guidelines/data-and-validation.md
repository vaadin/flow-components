# Data & Validation

## Data providers and data views

List components (ComboBox, Select, Grid, VirtualList, CheckboxGroup, …)
integrate with Vaadin's data provider system. Implement the interface(s) that
match the data model:

- `HasListDataView<T, V>` — in-memory list.
- `HasLazyDataView<T, F, V>` — lazy/server-driven, optional filter.
- `HasDataView<T, F, V>` — general data view.

Lazy components use a `DataCommunicator<T>` to track client-cached items and
serve pages on demand (e.g. `ComboBoxDataCommunicator`), usually bridged by a
JS connector (see [Connectors](component-implementation.md#connectors-javascript-glue)).

Expose the standard `setItems` family, each returning the specific data view
so callers can chain item-level operations:

```java
ExampleListDataView<T> setItems(T... items);
ExampleListDataView<T> setItems(Collection<T> items);
ExampleListDataView<T> setItems(ListDataProvider<T> dataProvider);
ExampleLazyDataView<T> setItems(CallbackDataProvider.FetchCallback<T, F> fetch);
ExampleLazyDataView<T> setItems(FetchCallback<T, F> fetch, CountCallback<T, F> count);
```

## Validation

- **Simple state.** `HasValidationProperties` gives `setInvalid` / `isInvalid`
  / `setErrorMessage` backed by element properties.
- **Automatic vs manual.** Field components validate on value change. Both
  modes MUST work: `setManualValidation(true)` hands full control to the
  application, which then sets `invalid` and the error message itself.
- **ValidationController.** Complex fields use
  `com.vaadin.flow.component.shared.internal.ValidationController` to
  coordinate built-in constraints, application validators, and the current
  mode — follow `ComboBoxBase`, `Select`, `DatePicker`.
- **Binder.** Implementing `HasValidator<V>` lets `Binder` pick up the default
  validator automatically.
