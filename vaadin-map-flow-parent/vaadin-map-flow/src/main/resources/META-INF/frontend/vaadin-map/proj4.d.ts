// Declares the untyped proj4 module with the API the connector uses
declare module 'proj4' {
  interface Proj4 {
    /** Registers a projection definition, in the WKT format, under the given name */
    defs(name: string, definition: string): void;
  }
  const proj4: Proj4;
  export = proj4;
}
