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
  ANIMATE_IN_CLASS,
  overlayAnimationStyles
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/OverlayAnimation.js';

/**
 * The GWT client's overlays dropped 4px into place while fading in, over
 * 120ms, with an `animate-in` class that came off once the animation had
 * run.
 */
describe('Overlay opening animation', () => {
  let host: HTMLElement;
  let overlay: HTMLElement;

  beforeEach(() => {
    host = document.createElement('div');
    document.body.appendChild(host);
    const root = host.attachShadow({ mode: 'open' });
    const style = document.createElement('style');
    style.textContent = `${overlayAnimationStyles.cssText} .overlay.animate-in { animation: overlay-animate-in 120ms; }`;
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
