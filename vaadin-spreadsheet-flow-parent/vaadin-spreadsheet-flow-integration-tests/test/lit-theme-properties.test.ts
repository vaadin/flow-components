/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import type { CSSResult } from 'lit';
import * as properties from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/ThemeProperties.js';

/**
 * Each documented property reads the application's value first, then the
 * theme's, then the default. Resolved through a custom property, the chain
 * shows which one wins.
 */
describe('ThemeProperties — the order a property is read in', () => {
  const entries = Object.entries(properties as Record<string, CSSResult>);

  /** The value a custom property holding the read resolves to, with the given properties set around it. */
  function resolve(read: CSSResult, around: Record<string, string>): string {
    const outer = document.createElement('div');
    for (const [name, value] of Object.entries(around)) {
      outer.style.setProperty(name, value);
    }
    const inner = document.createElement('div');
    inner.style.setProperty('--resolved', read.cssText);
    outer.appendChild(inner);
    document.body.appendChild(outer);
    const value = getComputedStyle(inner).getPropertyValue('--resolved').trim();
    outer.remove();
    return value;
  }

  /** The documented property a read is for, and its theme layer. */
  function namesOf(read: CSSResult): { documented: string; theme: string; fallback: string } {
    const match = /^var\((--vaadin-spreadsheet-[a-z0-9-]+), var\((--_vaadin-spreadsheet-[a-z0-9-]+), (.*)\)\)$/.exec(
      read.cssText
    );
    expect(match, read.cssText).to.not.equal(null);
    return { documented: match![1], theme: match![2], fallback: match![3] };
  }

  it('reads one documented property each, through its own theme layer', () => {
    expect(entries.length).to.be.greaterThan(30);
    for (const [name, read] of entries) {
      const { documented, theme } = namesOf(read);
      expect(theme, name).to.equal(documented.replace('--vaadin-spreadsheet-', '--_vaadin-spreadsheet-'));
    }
  });

  it('falls back to the default when neither is set', () => {
    for (const [name, read] of entries) {
      const { fallback } = namesOf(read);
      expect(resolve(read, {}), name).to.equal(fallbackOf(fallback));
    }
  });

  it("takes the theme's value over the default", () => {
    for (const [name, read] of entries) {
      const { theme } = namesOf(read);
      expect(resolve(read, { [theme]: 'from-the-theme' }), name).to.equal('from-the-theme');
    }
  });

  it("takes the application's value over the theme's", () => {
    for (const [name, read] of entries) {
      const { documented, theme } = namesOf(read);
      expect(resolve(read, { [theme]: 'from-the-theme', [documented]: 'from-the-app' }), name).to.equal('from-the-app');
    }
  });

  /** A default as a resolved custom property gives it back: its own text, a var() in it resolved. */
  function fallbackOf(text: string): string {
    const probe = document.createElement('div');
    probe.style.setProperty('--resolved', text);
    document.body.appendChild(probe);
    const value = getComputedStyle(probe).getPropertyValue('--resolved').trim();
    probe.remove();
    return value;
  }
});
