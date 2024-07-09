/*
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import format from 'date-fns/format';
import parse from 'date-fns/parse';
import isValid from 'date-fns/isValid';

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.datepickerDateFns = {
    format,
    parse,
    isValid,
};
