/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { coerceArray, coerceNumberArray, coerceObject } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/wire/Coerce.js';

/**
 * Taking a collection off the wire.
 *
 * Flow hands collections over as a JSON string on some lanes and as the
 * real thing on others, and which lane a given push takes is not
 * something a setter can tell by looking. Every setter that accepts a
 * collection runs it through one of these first.
 *
 * They are worth pinning on their own: a setter that forgets one does not
 * fail loudly. It iterates the string character by character and quietly
 * produces nonsense — that is how the whole sheet once rendered blank
 * with a single entry keyed "colundefined rowundefined".
 */

describe('coerceArray', () => {
  it('passes an array straight through', () => {
    const input = [1, 2, 3];
    expect(coerceArray(input)).to.equal(input);
  });

  it('parses the JSON string Flow sends', () => {
    expect(coerceArray('[1,2,3]')).to.deep.equal([1, 2, 3]);
  });

  it('parses a JSON string of objects', () => {
    expect(coerceArray('[{"col":1,"row":2}]')).to.deep.equal([
      { col: 1, row: 2 }
    ]);
  });

  it('reads null and undefined as empty', () => {
    // The server clears a collection by pushing null; a setter must not
    // have to guard for it.
    expect(coerceArray(null)).to.deep.equal([]);
    expect(coerceArray(undefined)).to.deep.equal([]);
  });

  it('reads an empty string as empty', () => {
    expect(coerceArray('')).to.deep.equal([]);
  });

  it('reads malformed JSON as empty rather than throwing', () => {
    // A throw here would abort the whole property batch, taking every
    // later push in the same response with it.
    expect(coerceArray('{oops')).to.deep.equal([]);
  });

  it('reads a JSON value that is not an array as empty', () => {
    expect(coerceArray('{"a":1}')).to.deep.equal([]);
    expect(coerceArray('42')).to.deep.equal([]);
  });

  it('converts a typed array to a real one', () => {
    // Downstream code calls .map and .filter on the result.
    const result = coerceArray(Int32Array.from([4, 5]) as any);
    expect(Array.isArray(result)).to.be.true;
    expect(result).to.deep.equal([4, 5]);
  });
});

describe('coerceObject', () => {
  it('passes an object straight through', () => {
    const input = { a: 1 };
    expect(coerceObject(input)).to.equal(input);
  });

  it('parses the JSON string Flow sends', () => {
    expect(coerceObject('{"a":1,"b":2}')).to.deep.equal({ a: 1, b: 2 });
  });

  it('reads null and undefined as empty', () => {
    expect(coerceObject(null)).to.deep.equal({});
    expect(coerceObject(undefined)).to.deep.equal({});
  });

  it('reads an empty string as empty', () => {
    // This is what the server sends for a cleared map, and it is not
    // valid JSON.
    expect(coerceObject('')).to.deep.equal({});
  });

  it('reads malformed JSON as empty rather than throwing', () => {
    expect(coerceObject('{oops')).to.deep.equal({});
  });

  it('reads a JSON null as empty', () => {
    expect(coerceObject('null')).to.deep.equal({});
  });
});

describe('coerceNumberArray', () => {
  it('keeps a typed array as it is', () => {
    // Row heights and column widths arrive as typed arrays for whole
    // sheets; copying them on every push would be wasteful.
    const input = Float32Array.from([1.5, 2.5]);
    expect(coerceNumberArray(input)).to.equal(input);
    const ints = Int32Array.from([1, 2]);
    expect(coerceNumberArray(ints)).to.equal(ints);
  });

  it('passes a plain array through', () => {
    const input = [1, 2];
    expect(coerceNumberArray(input)).to.equal(input);
  });

  it('parses the JSON string Flow sends', () => {
    expect(coerceNumberArray('[1,2.5]')).to.deep.equal([1, 2.5]);
  });

  it('reads null, empty and malformed as empty', () => {
    expect(coerceNumberArray(null)).to.deep.equal([]);
    expect(coerceNumberArray('')).to.deep.equal([]);
    expect(coerceNumberArray('[1,')).to.deep.equal([]);
  });

  it('reads a JSON value that is not an array as empty', () => {
    expect(coerceNumberArray('{"a":1}')).to.deep.equal([]);
  });
});
