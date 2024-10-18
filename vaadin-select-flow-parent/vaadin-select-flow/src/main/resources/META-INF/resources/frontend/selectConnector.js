window.Vaadin.Flow.selectConnector = {}
window.Vaadin.Flow.selectConnector.initLazy = (select) => {
  // do not init this connector twice for the given select
  if (select.$connector) {
    return;
  }

  select.$connector = {};

  select.renderer = (root) => {
    const listBox = select.querySelector('vaadin-select-list-box');
    if (listBox) {
      if (root.firstChild) {
        root.removeChild(root.firstChild);
      }
      root.appendChild(listBox);
    }
  };
}
