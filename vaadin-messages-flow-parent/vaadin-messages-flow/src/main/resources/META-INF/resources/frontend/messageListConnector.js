/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
window.Vaadin.Flow.messageListConnector = {
  setItems(list, items, locale) {
    const formatter = new Intl.DateTimeFormat(locale, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric'
    });
    list.items = items.map((item) =>
      item.time
        ? Object.assign(item, {
            time: formatter.format(new Date(item.time))
          })
        : item
    );
  }
};
