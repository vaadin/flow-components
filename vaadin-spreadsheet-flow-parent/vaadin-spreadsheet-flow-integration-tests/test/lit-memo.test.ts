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
  Memo,
  sameInputs
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/Memo.js';

/**
 * A result kept until one of what it was computed from is replaced: what
 * lets the cell grid write its style rules once per change of styles rather
 * than once per frame.
 */
describe('Memo', () => {
  it('computes once for the same inputs', () => {
    const memo = new Memo<string>();
    const styles = { 1: 'color: red;' };
    let computed = 0;
    const compute = (): string => `rules ${++computed}`;

    expect(memo.get([styles, true], compute)).to.equal('rules 1');
    expect(memo.get([styles, true], compute)).to.equal('rules 1');
    expect(computed).to.equal(1);
  });

  it('computes again when any one input is replaced', () => {
    const memo = new Memo<number>();
    const a = {};
    const b = {};
    let computed = 0;
    const compute = (): number => ++computed;

    memo.get([a, b], compute);
    memo.get([a, {}], compute);
    memo.get([{}, {}], compute);

    expect(computed).to.equal(3);
  });

  it('takes an equal but new input for a change: it compares by identity', () => {
    const memo = new Memo<number>();
    let computed = 0;

    memo.get([{ 1: 'x' }], () => ++computed);
    memo.get([{ 1: 'x' }], () => ++computed);

    expect(computed).to.equal(2);
  });

  it('computes again when a primitive input changes', () => {
    const memo = new Memo<number>();
    let computed = 0;

    memo.get([true], () => ++computed);
    memo.get([false], () => ++computed);
    memo.get([false], () => ++computed);

    expect(computed).to.equal(2);
  });

  it('gives the latest result after recomputing', () => {
    const memo = new Memo<string>();

    memo.get([1], () => 'first');

    expect(memo.get([2], () => 'second')).to.equal('second');
    expect(memo.get([2], () => 'third')).to.equal('second');
  });
});

describe('sameInputs', () => {
  it('holds for the same inputs in the same order', () => {
    const a = {};
    expect(sameInputs([a, 1, 'x'], [a, 1, 'x'])).to.be.true;
  });

  it('does not hold for another order, another length or nothing to compare with', () => {
    const a = {};
    const b = {};
    expect(sameInputs([a, b], [b, a])).to.be.false;
    expect(sameInputs([a], [a, b])).to.be.false;
    expect(sameInputs([a, b], [a])).to.be.false;
    expect(sameInputs([a], null)).to.be.false;
  });
});
