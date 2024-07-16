/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
window.dragData = {text: 'foo'};

window.fireDragStart = draggable => {
  const event = new Event('dragstart', {
    bubbles: true,
    cancelable: true,
    composed: true
  });
  event.dataTransfer = {
    setDragImage: () => {
    },
    setData: (type, data) => dragData[type] = data
  };
  if (draggable.getAttribute('draggable') === 'true') {
	  draggable.dispatchEvent(event);
  }
};


window.fireDragEnd = grid => {
  const event = new Event('dragend', {
    bubbles: true,
    cancelable: true,
    composed: true
  });
  grid.$.table.dispatchEvent(event);
};

window.fireDrop = (draggable, location) => {

  // First fire drag-over to get the dropLocation
  fireDragOver(draggable, location);

  const event = new Event('drop', {
    bubbles: true,
    cancelable: true,
    composed: true
  });
  event.dataTransfer = {
    getData: type => dragData[type],
    types: Object.keys(dragData)
  };
  // Draggable is the vaadin-grid-cell-content element
  const row = draggable.assignedSlot.parentNode.parentNode;
  if (!row.hasAttribute('drop-disabled')) {
	  draggable.dispatchEvent(event);
  }
};


window.fireDragOver = (row, location) => {
  const event = new Event('dragover', {
    bubbles: true,
    cancelable: true,
    composed: true
  });
  const rect = row.getBoundingClientRect();
  if (location === 'on-top') {
    event.clientY = rect.top + rect.height / 2;
  } else if (location === 'above') {
    event.clientY = rect.top;
  } else if (location === 'below') {
    event.clientY = rect.bottom;
  } else if (location === 'under') {
    event.clientY = rect.bottom + rect.height / 2;
  }
  row.dispatchEvent(event);
};
