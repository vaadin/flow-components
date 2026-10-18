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
  coerceArray,
  coerceNumberArray,
  coerceObject,
  coerceSet,
  coerceSizes
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/wire/Coerce.js';

/**
 * Taking a collection off the wire.
 *
 * The server sends collections as JSON values, and null to clear one. Every
 * setter that accepts a collection runs it through one of these first, which
 * reads anything else as empty.
 *
 * They are worth pinning on their own: text that slipped through would not
 * fail loudly. It would be iterated character by character and quietly
 * produce nonsense — that is how the whole sheet once rendered blank with a
 * single entry keyed "colundefined rowundefined". The server sent JSON as
 * text then; it no longer does, and the client no longer parses text.
 */

describe('coerceArray', () => {
  it('passes an array straight through', () => {
    const input = [1, 2, 3];
    expect(coerceArray(input)).to.equal(input);
  });

  it('reads text as empty, never as its characters', () => {
    expect(coerceArray('[1,2,3]' as any)).to.deep.equal([]);
    expect(coerceArray('[{"col":1,"row":2}]' as any)).to.deep.equal([]);
  });

  it('reads null and undefined as empty', () => {
    // The server clears a collection by pushing null; a setter must not
    // have to guard for it.
    expect(coerceArray(null)).to.deep.equal([]);
    expect(coerceArray(undefined)).to.deep.equal([]);
  });

  it('reads anything that is not a list as empty rather than throwing', () => {
    // A throw here would abort the whole property batch, taking every
    // later push in the same response with it.
    expect(coerceArray({ a: 1 } as any)).to.deep.equal([]);
    expect(coerceArray(42 as any)).to.deep.equal([]);
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

  it('reads text as empty', () => {
    expect(coerceObject('{"a":1,"b":2}' as any)).to.deep.equal({});
  });

  it('reads null and undefined as empty', () => {
    expect(coerceObject(null)).to.deep.equal({});
    expect(coerceObject(undefined)).to.deep.equal({});
  });

  it('reads anything that is not an object as empty', () => {
    expect(coerceObject(42 as any)).to.deep.equal({});
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

  it('reads null, text and anything that is not a list as empty', () => {
    expect(coerceNumberArray(null)).to.deep.equal([]);
    expect(coerceNumberArray('[1,2.5]' as any)).to.deep.equal([]);
    expect(coerceNumberArray({ a: 1 } as any)).to.deep.equal([]);
  });
});

describe('coerceSet', () => {
  it('holds each value of the list', () => {
    expect(coerceSet([3, 1, 3])).to.eql(new Set([1, 3]));
  });

  it('reads null and text as empty', () => {
    expect(coerceSet(null)).to.eql(new Set());
    expect(coerceSet('["col1row2"]' as any)).to.eql(new Set());
  });
});

describe('coerceSizes', () => {
  // The server sends row heights and column widths as runs of equal sizes:
  // one size per row came to 5.6 MB for the largest sheet.
  it('spells out the runs the server sends', () => {
    expect(coerceSizes({ runs: [15.0, 3, 30.0, 1, 15.0, 2] })).to.deep.equal([15, 15, 15, 30, 15, 15]);
    expect(coerceSizes({ runs: [50, 2, 120, 1] })).to.deep.equal([50, 50, 120]);
  });

  it('spells out a million rows from one run', () => {
    const sizes = coerceSizes({ runs: [15.0, 1048576] });
    expect(sizes.length).to.equal(1_048_576);
    expect(sizes[0]).to.equal(15);
    expect(sizes[1_048_575]).to.equal(15);
  });

  it('keeps hidden rows, of no height, among the others', () => {
    expect(coerceSizes({ runs: [15, 1, 0, 2, 15, 1] })).to.deep.equal([15, 0, 0, 15]);
  });

  it('reads no runs as no sizes', () => {
    expect(coerceSizes({ runs: [] })).to.deep.equal([]);
  });

  it('passes a plain list through', () => {
    const input = [1, 2];
    expect(coerceSizes(input)).to.equal(input);
    const typed = Float32Array.from([1.5]);
    expect(coerceSizes(typed)).to.equal(typed);
  });

  it('reads null, text and anything without runs as no sizes', () => {
    expect(coerceSizes(null)).to.deep.equal([]);
    expect(coerceSizes(undefined)).to.deep.equal([]);
    expect(coerceSizes('{"runs":[15.0,2]}' as any)).to.deep.equal([]);
    expect(coerceSizes({ other: [1, 2] } as any)).to.deep.equal([]);
    expect(coerceSizes(7 as any)).to.deep.equal([]);
  });

  const malformed: Array<[string, unknown[]]> = [
    ['a size without its count', [15, 2, 30]],
    ['a count that is not a whole number', [15, 1.5]],
    ['a count below zero', [15, -1]],
    ['a count that is not a number', [15, '2']],
    ['a size that is not a number', ['15', 2]],
    ['a size that is not finite', [Number.POSITIVE_INFINITY, 2]]
  ];
  for (const [what, runs] of malformed) {
    it(`reads runs with ${what} as no sizes`, () => {
      expect(coerceSizes({ runs } as never)).to.deep.equal([]);
    });
  }

  it('reads runs spelling out more sizes than any sheet has as no sizes', () => {
    // 2^24 is the most; one more is not spelled out at all.
    expect(coerceSizes({ runs: [15, 1 << 24] }).length).to.equal(1 << 24);
    expect(coerceSizes({ runs: [15, 1 << 24, 15, 1] }).length).to.equal(0);
  });
});
