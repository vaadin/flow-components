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

describe('coerceSet', () => {
  it('holds each value of the list', () => {
    expect(coerceSet([3, 1, 3])).to.eql(new Set([1, 3]));
  });

  it('parses the JSON string Flow sends', () => {
    expect(coerceSet<string>('["col1row2"]')).to.eql(new Set(['col1row2']));
  });

  it('reads null, empty and malformed as empty', () => {
    expect(coerceSet(null)).to.eql(new Set());
    expect(coerceSet('')).to.eql(new Set());
    expect(coerceSet('[1,')).to.eql(new Set());
  });
});

describe('coerceSizes', () => {
  // The server sends row heights and column widths as runs of equal sizes:
  // one size per row came to 5.6 MB for the largest sheet.
  it('spells out the runs the server sends', () => {
    expect(coerceSizes('{"runs":[15.0,3,30.0,1,15.0,2]}')).to.deep.equal([15, 15, 15, 30, 15, 15]);
  });

  it('spells out runs already parsed', () => {
    expect(coerceSizes({ runs: [50, 2, 120, 1] })).to.deep.equal([50, 50, 120]);
  });

  it('spells out a million rows from one run', () => {
    const sizes = coerceSizes('{"runs":[15.0,1048576]}');
    expect(sizes.length).to.equal(1_048_576);
    expect(sizes[0]).to.equal(15);
    expect(sizes[1_048_575]).to.equal(15);
  });

  it('keeps hidden rows, of no height, among the others', () => {
    expect(coerceSizes({ runs: [15, 1, 0, 2, 15, 1] })).to.deep.equal([15, 0, 0, 15]);
  });

  it('reads no runs as no sizes', () => {
    expect(coerceSizes('{"runs":[]}')).to.deep.equal([]);
  });

  it('passes a plain list through', () => {
    const input = [1, 2];
    expect(coerceSizes(input)).to.equal(input);
    expect(coerceSizes('[1,2.5]')).to.deep.equal([1, 2.5]);
    const typed = Float32Array.from([1.5]);
    expect(coerceSizes(typed)).to.equal(typed);
  });

  it('reads null, empty and malformed text as no sizes', () => {
    expect(coerceSizes(null)).to.deep.equal([]);
    expect(coerceSizes(undefined)).to.deep.equal([]);
    expect(coerceSizes('')).to.deep.equal([]);
    expect(coerceSizes('{"runs":[1,')).to.deep.equal([]);
    expect(coerceSizes('{"other":[1,2]}')).to.deep.equal([]);
    expect(coerceSizes('7')).to.deep.equal([]);
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
