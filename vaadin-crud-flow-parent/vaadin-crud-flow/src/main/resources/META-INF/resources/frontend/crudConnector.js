window.Vaadin.Flow.crudConnector = {
  initLazy: crud => {
    if (crud.$connector) return;

    // Hook into all client-side events and propagate to the server-side
    ['new', 'edit', 'cancel', 'save', 'delete'].forEach(eventName =>
      crud.addEventListener(eventName, event => {
        event.preventDefault();
        crud.dispatchEvent(new CustomEvent(`crud-${eventName}`, {detail: event.detail}));
      })
    );

    // Mark editor as clean on new and edit
    ['new', 'edit'].forEach(e => crud.addEventListener(e, () => crud._dirty = false));

    crud.closeDialog = () => crud.__closeEditor();

    crud.$connector = true;
  }
};
