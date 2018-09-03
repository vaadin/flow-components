window.Vaadin.Flow.crudConnector = {
  initLazy: function (crud) {
    // Check whether the connector was already initialized for the crud
    if (crud.$connector) {
      return;
    }

    Vaadin.CrudElement.prototype.__setDialogOpened = function(isOpen) {
      this.$.dialog.opened = isOpen;
    }

    Vaadin.CrudElement.prototype.__new = function() {
      this.dispatchEvent(new CustomEvent('new', {cancelable: true}));
    }

    Vaadin.CrudElement.prototype.__edit = function(item) {
      this.dispatchEvent(new CustomEvent('edit', {detail: {item: item}, cancelable: true}));
    }

    Vaadin.CrudElement.prototype.__save = function() {
      this.dispatchEvent(new CustomEvent('save', {cancelable: true}));
    }

    Vaadin.CrudElement.prototype.__delete = function() {
      this.dispatchEvent(new CustomEvent('delete', {cancelable: true}));
    }

    Vaadin.CrudElement.prototype.__cancel = function() {
      this.dispatchEvent(new CustomEvent('cancel', {cancelable: true}));
    }
  }
}
