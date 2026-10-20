/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import {
  animateIn,
  animateOut,
  ANIMATE_IN_CLASS,
  ANIMATE_OUT_CLASS,
  overlayAnimationStyles
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/OverlayAnimation.js';

/**
 * An overlay drops 4px into place while fading in, over 120ms, with an
 * `animate-in` class that comes off once the animation has run.
 */
describe('Overlay opening animation', () => {
  let host: HTMLElement;
  let overlay: HTMLElement;

  beforeEach(() => {
    host = document.createElement('div');
    document.body.appendChild(host);
    const root = host.attachShadow({ mode: 'open' });
    const style = document.createElement('style');
    style.textContent =
      `${overlayAnimationStyles.cssText} .overlay.animate-in { animation: overlay-animate-in 120ms; }` +
      ' .overlay.animate-out { animation: overlay-animate-out 120ms; }';
    overlay = document.createElement('div');
    overlay.className = 'overlay';
    overlay.textContent = 'note';
    root.append(style, overlay);
  });

  afterEach(() => {
    host.remove();
  });

  it('plays the drop and fade', () => {
    animateIn(overlay);

    expect(overlay.classList.contains(ANIMATE_IN_CLASS)).to.be.true;
    const [animation] = overlay.getAnimations() as CSSAnimation[];
    expect(animation.animationName).to.equal('overlay-animate-in');
    expect(animation.effect!.getTiming().duration).to.equal(120);
    // The first frame: 4px up and not yet visible.
    expect(getComputedStyle(overlay).transform).to.equal('matrix(1, 0, 0, 1, 0, -4)');
    expect(getComputedStyle(overlay).opacity).to.equal('0');
  });

  it('takes the class off once the animation has run', async () => {
    animateIn(overlay);
    const ended = new Promise((resolve) => overlay.addEventListener('animationend', resolve, { once: true }));

    overlay.getAnimations().forEach((animation) => animation.finish());
    await ended;

    expect(overlay.classList.contains(ANIMATE_IN_CLASS)).to.be.false;
  });

  it('takes the class off at once when a stylesheet gives the overlay no animation', () => {
    overlay.style.animation = 'none';

    animateIn(overlay);

    expect(overlay.classList.contains(ANIMATE_IN_CLASS)).to.be.false;
  });
});

/**
 * An overlay fades out over 120ms as it closes, with an `animate-out`
 * class, and goes once the fade has run. One still dropping into place
 * finishes first and then goes without a fade.
 */
describe('Overlay closing fade', () => {
  let host: HTMLElement;
  let overlay: HTMLElement;

  beforeEach(() => {
    host = document.createElement('div');
    document.body.appendChild(host);
    const root = host.attachShadow({ mode: 'open' });
    const style = document.createElement('style');
    style.textContent =
      `${overlayAnimationStyles.cssText} .overlay.animate-in { animation: overlay-animate-in 120ms; }` +
      ' .overlay.animate-out { animation: overlay-animate-out 120ms; }';
    overlay = document.createElement('div');
    overlay.className = 'overlay';
    overlay.textContent = 'note';
    root.append(style, overlay);
  });

  afterEach(() => {
    host.remove();
  });

  /** Run every animation on the overlay to its end and let the events land. */
  async function finish(): Promise<void> {
    overlay.getAnimations().forEach((animation) => animation.finish());
    await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));
  }

  it('fades out', () => {
    animateOut(overlay, () => {});

    expect(overlay.classList.contains(ANIMATE_OUT_CLASS)).to.be.true;
    const [animation] = overlay.getAnimations() as CSSAnimation[];
    expect(animation.animationName).to.equal('overlay-animate-out');
    expect(animation.effect!.getTiming().duration).to.equal(120);
    // From as it was to gone, on the default easing.
    animation.currentTime = 0;
    expect(getComputedStyle(overlay).opacity).to.equal('1');
    animation.currentTime = 119;
    expect(Number(getComputedStyle(overlay).opacity)).to.be.below(0.01);
  });

  it('takes no clicks while it fades', () => {
    animateOut(overlay, () => {});

    expect(getComputedStyle(overlay).pointerEvents).to.equal('none');
  });

  it('goes once the fade has run, and not before', async () => {
    let gone = 0;
    animateOut(overlay, () => gone++);
    expect(gone).to.equal(0);

    await finish();

    expect(gone).to.equal(1);
    expect(overlay.classList.contains(ANIMATE_OUT_CLASS)).to.be.false;
  });

  it('goes at once when a stylesheet gives it no fade', () => {
    overlay.style.animation = 'none';
    let gone = 0;

    animateOut(overlay, () => gone++);

    expect(gone).to.equal(1);
    expect(overlay.classList.contains(ANIMATE_OUT_CLASS)).to.be.false;
  });

  it('goes at once on a hidden page, which runs no animations', () => {
    Object.defineProperty(document, 'visibilityState', { value: 'hidden', configurable: true });
    try {
      let gone = 0;
      animateIn(overlay);

      animateOut(overlay, () => gone++);

      expect(gone).to.equal(1);
      expect(overlay.classList.contains(ANIMATE_IN_CLASS)).to.be.false;
      expect(overlay.classList.contains(ANIMATE_OUT_CLASS)).to.be.false;
    } finally {
      delete (document as { visibilityState?: string }).visibilityState;
    }
  });

  it('lets an overlay still dropping in finish, and then go without a fade', async () => {
    let gone = 0;
    animateIn(overlay);

    animateOut(overlay, () => gone++);

    expect(overlay.classList.contains(ANIMATE_OUT_CLASS)).to.be.false;
    expect(gone).to.equal(0);
    await finish();
    expect(gone).to.equal(1);
    expect(overlay.classList.contains(ANIMATE_IN_CLASS)).to.be.false;
  });

  it('stays when shown again while it fades', async () => {
    let gone = 0;
    animateOut(overlay, () => gone++);

    animateIn(overlay);

    expect(overlay.classList.contains(ANIMATE_OUT_CLASS)).to.be.false;
    expect(overlay.classList.contains(ANIMATE_IN_CLASS)).to.be.true;
    expect((overlay.getAnimations() as CSSAnimation[]).map((a) => a.animationName)).to.deep.equal([
      'overlay-animate-in'
    ]);
    await finish();
    expect(gone).to.equal(0);
    expect(overlay.classList.contains(ANIMATE_IN_CLASS)).to.be.false;
  });

  it('drops in all the way when shown again while it fades', async () => {
    const frames = (): Promise<unknown> =>
      new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));
    animateOut(overlay, () => {});
    await frames();

    animateIn(overlay);
    // The fade that was called off ends a moment after the drop has begun.
    await frames();

    expect(overlay.classList.contains(ANIMATE_IN_CLASS)).to.be.true;
  });

  it('does everything asked of it when closed twice while it fades', async () => {
    const done: string[] = [];
    animateOut(overlay, () => done.push('first'));
    animateOut(overlay, () => done.push('second'));

    await finish();

    expect(done).to.deep.equal(['first', 'second']);
  });
});
