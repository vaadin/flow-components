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
  hideFromTopLayer,
  showInTopLayer
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/TopLayer.js';

describe('Top layer', () => {
  let el: HTMLElement;

  beforeEach(() => {
    el = document.createElement('div');
    el.popover = 'manual';
  });

  afterEach(() => {
    el.remove();
  });

  it('shows a popover in the top layer', () => {
    document.body.appendChild(el);

    showInTopLayer(el);

    expect(el.matches(':popover-open')).to.be.true;
  });

  it('leaves a popover that is already shown as it is', () => {
    document.body.appendChild(el);
    showInTopLayer(el);

    expect(() => showInTopLayer(el)).to.not.throw();
    expect(el.matches(':popover-open')).to.be.true;
  });

  it('does nothing for an element that is not in the document', () => {
    showInTopLayer(el);

    expect(el.matches(':popover-open')).to.be.false;
  });

  it('does nothing for an element that is not a popover', () => {
    const plain = document.createElement('div');
    document.body.appendChild(plain);

    expect(() => showInTopLayer(plain)).to.not.throw();
    expect(plain.matches(':popover-open')).to.be.false;
    plain.remove();
  });

  it('takes a popover out of the top layer, and leaves one that is not there alone', () => {
    document.body.appendChild(el);
    showInTopLayer(el);

    hideFromTopLayer(el);
    expect(el.matches(':popover-open')).to.be.false;
    expect(() => hideFromTopLayer(el)).to.not.throw();
  });
});
