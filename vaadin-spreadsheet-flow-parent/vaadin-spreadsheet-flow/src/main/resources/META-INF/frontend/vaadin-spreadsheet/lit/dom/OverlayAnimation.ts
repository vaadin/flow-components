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

/**
 * The animations of the sheet's overlays - the filter popup, the context
 * menu and the notes: dropping 4px into place while fading in as one opens,
 * and fading out as it closes. They play in either theme.
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

  @keyframes overlay-animate-out {
    100% {
      opacity: 0;
    }
  }

  /* On its way out, an overlay takes no more clicks: they go to what is
     under it. */
  .animate-out {
    pointer-events: none;
  }
`;

/** The class that plays the opening animation, while it plays - see {@link animateIn}. */
export const ANIMATE_IN_CLASS = 'animate-in';

/** The class that plays the closing fade, while it plays - see {@link animateOut}. */
export const ANIMATE_OUT_CLASS = 'animate-out';

/** The overlays on their way out, and what each is to do once it has gone. */
const leaving = new WeakMap<HTMLElement, { end: (e: Event) => void; then: Array<() => void> }>();

/** The animations a stylesheet gives an element now, by name. */
function animationNames(el: HTMLElement): string[] {
  return getComputedStyle(el)
    .animationName.split(',')
    .map((name) => name.trim())
    .filter((name) => name !== '' && name !== 'none');
}

/** Call off an overlay's way out, leaving it where it is. */
function stayIn(el: HTMLElement): void {
  const pending = leaving.get(el);
  if (pending === undefined) {
    return;
  }
  leaving.delete(el);
  el.removeEventListener('animationend', pending.end);
  el.removeEventListener('animationcancel', pending.end);
  el.classList.remove(ANIMATE_OUT_CLASS);
}

/**
 * Play the opening animation on an overlay that has just appeared: with the
 * {@code animate-in} class, taken off again
 * once the animation has run. An overlay that a stylesheet gives no
 * animation loses the class at once. One that is on its way out stays.
 */
export function animateIn(el: HTMLElement): void {
  stayIn(el);
  el.classList.add(ANIMATE_IN_CLASS);
  const names = animationNames(el);
  if (names.length === 0) {
    el.classList.remove(ANIMATE_IN_CLASS);
    return;
  }
  // Only the opening animation's own end: calling off a fade ends that one
  // too, a moment after this one has started.
  const done = (e: Event): void => {
    if (e.target !== el || !names.includes((e as AnimationEvent).animationName)) {
      return;
    }
    el.classList.remove(ANIMATE_IN_CLASS);
    el.removeEventListener('animationend', done);
    el.removeEventListener('animationcancel', done);
  };
  el.addEventListener('animationend', done);
  el.addEventListener('animationcancel', done);
}

/**
 * Take an overlay away: fade it out with the {@code animate-out} class and call `done`, which removes it, once the fade
 * has run. One still dropping into place is let finish, and then goes
 * without a fade. One that a stylesheet gives no fade goes at once, and so
 * does one on a hidden page, which runs no animations until it is shown.
 */
export function animateOut(el: HTMLElement, done: () => void): void {
  const pending = leaving.get(el);
  if (pending !== undefined) {
    pending.then.push(done);
    return;
  }
  if (document.visibilityState === 'hidden') {
    el.classList.remove(ANIMATE_IN_CLASS);
    done();
    return;
  }
  let names = el.classList.contains(ANIMATE_IN_CLASS) ? animationNames(el) : [];
  if (names.length === 0) {
    el.classList.add(ANIMATE_OUT_CLASS);
    names = animationNames(el);
  }
  if (names.length === 0) {
    el.classList.remove(ANIMATE_OUT_CLASS);
    done();
    return;
  }
  const then = [done];
  const end = (e: Event): void => {
    if (e.target !== el || !names.includes((e as AnimationEvent).animationName)) {
      return;
    }
    stayIn(el);
    el.classList.remove(ANIMATE_IN_CLASS);
    then.forEach((callback) => callback());
  };
  leaving.set(el, { end, then });
  el.addEventListener('animationend', end);
  el.addEventListener('animationcancel', end);
}
