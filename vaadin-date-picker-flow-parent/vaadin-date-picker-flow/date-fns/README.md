# Date picker date-fns custom bundle

This folder contains a rollup build to create a custom bundle of the [date-fns](https://date-fns.org/) library.
The custom bundle is used by the date picker's connector as part of the custom date format feature.
The custom bundle contains only the date-fns functions that are required for that feature, and exposes those as a global under `window.Vaadin.Flow.datePickerDateFns`.
The build automatically runs as part of the `generate-resources` Maven phase, and the resulting bundle is added as a resource to the date picker JAR file.

We use a custom bundle, instead of `@NpmPackage`, to support the compatibility mode in Vaadin 14.
The Vaadin 14 compatibility mode uses Bower and webjars instead of NPM, and does not support adding frontend dependencies as easily as the NPM mode does with `@NpmPackage`.
Instead, we add the date-fns functions that are required by the feature into a custom bundle, and add that bundle to the date pickers JAR file, which makes it easily accessible.
This workaround can be removed / replaced with `@NpmPackage` as soon as Vaadin 14 is not maintained anymore.