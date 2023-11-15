import { Tooltip } from '@vaadin/tooltip/src/vaadin-tooltip.js';

const _window = window as any;
_window.Vaadin = _window.Vaadin || {};
_window.Vaadin.Flow = _window.Vaadin.Flow || {};
_window.Vaadin.Flow.tooltip = _window.Vaadin.Flow.tooltip || {};

_window.Vaadin.Flow.tooltip.setDefaultHideDelay = (hideDelay: number) => Tooltip.setDefaultHideDelay(hideDelay);
_window.Vaadin.Flow.tooltip.setDefaultFocusDelay = (focusDelay: number) => Tooltip.setDefaultFocusDelay(focusDelay);
_window.Vaadin.Flow.tooltip.setDefaultHoverDelay = (hoverDelay: number) => Tooltip.setDefaultHoverDelay(hoverDelay);

const { defaultHideDelay, defaultFocusDelay, defaultHoverDelay } = _window.Vaadin.Flow.tooltip;
if (defaultHideDelay) {
  Tooltip.setDefaultHideDelay(defaultHideDelay);
}
if (defaultFocusDelay) {
  Tooltip.setDefaultFocusDelay(defaultFocusDelay);
}
if (defaultHoverDelay) {
  Tooltip.setDefaultHoverDelay(defaultHoverDelay);
}
