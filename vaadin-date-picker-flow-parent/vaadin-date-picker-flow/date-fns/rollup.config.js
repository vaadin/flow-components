/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
import resolve from '@rollup/plugin-node-resolve';
import {terser} from 'rollup-plugin-terser';

export default {
    input: 'index.js',
    output: {
        file: 'output/date-picker-datefns.js',
        // Allows loading the bundle as a regular script file
        format: 'iife',
    },
    plugins: [
        // Support for resolving dependencies in node_modules
        resolve({browser: true}),
        // Minify output
        terser(),
    ],
};