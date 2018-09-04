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

    crud.__setDialogOpened = isOpen => crud.$.dialog.opened = isOpen;

    crud.$connector = true;
  }
};
