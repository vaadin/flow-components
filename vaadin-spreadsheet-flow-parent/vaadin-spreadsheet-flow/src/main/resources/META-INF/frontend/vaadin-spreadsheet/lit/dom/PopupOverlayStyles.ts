/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css } from 'lit';
import { overlayAnimationStyles } from './OverlayAnimation.js';
import { popupBackground, popupBorderColor } from '../elements/ThemeProperties.js';

/**
 * The popup's card, header and close box.
 *
 * The overlay is the connector's own element in the host's tree, where no
 * internal element's stylesheet reaches, and a rule says what an inline
 * style cannot: "on hover", and "under the Lumo theme". The card is white,
 * rounded and softly shadowed, with an 18px header whose close box sits at
 * its left, grey until pointed at, and whose caption is centred. Under Lumo
 * the close box is Lumo's cross at the right, under a rule, and the caption
 * is at the left in bold.
 */
export const popupOverlayStyles = css`
  ${overlayAnimationStyles}
  .popup-overlay.animate-out {
    animation: overlay-animate-out 120ms;
  }

  .popup-overlay.animate-in {
    animation: overlay-animate-in 120ms;
  }
  .popup-overlay {
    outline: none;
    padding: 4px;
    border: 0;
    border-radius: 4px;
    color: #474747;
    background: ${popupBackground};
    /* The edge is the last shadow, a hairline that takes no room. */
    box-shadow: 0 4px 10px 0 rgba(0, 0, 0, 0.1), 0 3px 5px 0 rgba(0, 0, 0, 0.05), 0 0 0 1px ${popupBorderColor};
  }
  .popup-header {
    position: relative;
    height: 18px;
    padding-bottom: 7px;
  }
  .popup-close {
    position: absolute;
    left: 0;
    top: 0;
    box-sizing: border-box;
    width: 18px;
    height: 18px;
    padding: 0 4px 0 0;
    border: 0;
    border-radius: 0 4px 0 4px;
    background: transparent;
    cursor: pointer;
    font-family: inherit;
    font-size: 21px;
    line-height: 18px;
    text-align: center;
    color: #999;
  }
  .popup-close::before {
    content: '\\00d7';
  }
  .popup-close:hover {
    color: #197de1;
  }
  .header-caption {
    margin: 0 18px;
    height: 18px;
    line-height: 18px;
    text-align: center;
    color: var(--lumo-body-text-color);
  }
  :host([theme~='lumo']) .popup-overlay {
    padding: var(--lumo-space-xs);
    border: 1px solid ${popupBorderColor};
    border-radius: var(--lumo-border-radius-m);
    color: var(--lumo-body-text-color);
    box-shadow: var(--lumo-box-shadow-s);
  }
  :host([theme~='lumo']) .header-caption {
    margin-left: var(--lumo-space-xs);
    font-weight: bold;
    text-align: left;
  }
  :host([theme~='lumo']) .popup-header {
    padding-bottom: var(--lumo-space-xs);
    border-bottom: 1px solid var(--lumo-contrast-20pct);
    margin-bottom: var(--lumo-space-xs);
  }
  :host([theme~='lumo']) .popup-close {
    left: auto;
    right: 0;
    padding-right: var(--lumo-space-xs);
    border-radius: 0 var(--lumo-border-radius-m) 0 var(--lumo-border-radius-m);
    font-size: var(--lumo-font-size-xl);
    color: var(--lumo-contrast-50pct);
    cursor: var(--lumo-clickable-cursor);
  }
  :host([theme~='lumo']) .popup-close::before {
    content: var(--lumo-icons-cross);
    font-family: 'lumo-icons';
  }
  :host([theme~='lumo']) .popup-close:hover {
    color: var(--lumo-primary-color);
  }
`;

/**
 * Gives the root the popup is drawn in its rules, once for every popup: one
 * sheet, parsed once. It goes before the sheets the root has, where the
 * rules were in the cascade when each popup carried them.
 */
export function adoptPopupOverlayStyles(overlay: Element): void {
  const root = overlay.getRootNode();
  const sheet = popupOverlayStyles.styleSheet;
  if (sheet === undefined || !(root instanceof ShadowRoot || root instanceof Document)) {
    return;
  }
  if (!root.adoptedStyleSheets.includes(sheet)) {
    root.adoptedStyleSheets = [sheet, ...root.adoptedStyleSheets];
  }
}
