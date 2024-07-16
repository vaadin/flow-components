/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
import { Debouncer } from '@polymer/polymer/lib/utils/debounce.js';
import { timeOut, animationFrame } from '@polymer/polymer/lib/utils/async.js';
import { GridElement } from '@vaadin/vaadin-grid/src/vaadin-grid.js';
import { ItemCache } from '@vaadin/vaadin-grid/src/vaadin-grid-data-provider-mixin.js';
import { isFocusable } from '@vaadin/vaadin-grid/src/vaadin-grid-active-item-mixin.js';
import './gridConnector.js';

window.Vaadin.Flow.Legacy.Debouncer = Debouncer;
window.Vaadin.Flow.Legacy.timeOut = timeOut;
window.Vaadin.Flow.Legacy.animationFrame = animationFrame;
window.Vaadin.Flow.Legacy.GridElement = GridElement;
window.Vaadin.Flow.Legacy.ItemCache = ItemCache;
window.Vaadin.Flow.Legacy.isFocusable = isFocusable;