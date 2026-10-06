/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css } from 'lit';

/**
 * The GWT client's opening animation for its overlays - the filter popup,
 * the context menu and the notes: dropping 4px into place while fading in.
 * It played in either theme.
 *
 * Keyframes belong to the shadow tree whose stylesheet defines them, so
 * every tree an overlay is drawn in carries these.
 */
export const overlayAnimationStyles = css`
  @keyframes overlay-animate-in {
    0% {
      transform: translateY(-4px);
      opacity: 0;
    }
  }
`;

/** The class that plays it, while it plays - see {@link animateIn}. */
export const ANIMATE_IN_CLASS = 'animate-in';

/**
 * Play the opening animation on an overlay that has just appeared, the way
 * the GWT client did: with the {@code animate-in} class, taken off again
 * once the animation has run. An overlay that a stylesheet gives no
 * animation loses the class at once.
 */
export function animateIn(el: HTMLElement): void {
  el.classList.add(ANIMATE_IN_CLASS);
  if (getComputedStyle(el).animationName === 'none') {
    el.classList.remove(ANIMATE_IN_CLASS);
    return;
  }
  const done = (e: Event): void => {
    if (e.target !== el) {
      return;
    }
    el.classList.remove(ANIMATE_IN_CLASS);
    el.removeEventListener('animationend', done);
    el.removeEventListener('animationcancel', done);
  };
  el.addEventListener('animationend', done);
  el.addEventListener('animationcancel', done);
}
