#!/bin/bash
defaultName="Button"
defaultNameDashed="vaadin-button"
defaultPackage="button"

name=$1
nameDashed=$2
package=$(echo "$name" | tr '[:upper:]' '[:lower:]')

echo Class name: $name
echo Module name: $nameDashed
echo Package name: $package

addon=vaadin-$nameDashed-flow
demo=vaadin-$nameDashed-flow-demo
it=vaadin-$nameDashed-flow-integration-tests
testbench=vaadin-$nameDashed-flow-testbench

echo Moving folders
mv vaadin-$defaultNameDashed-flow $addon
mv vaadin-$defaultNameDashed-flow-demo $demo
mv vaadin-$defaultNameDashed-flow-integration-tests $it
mv vaadin-$defaultNameDashed-flow-testbench $testbench

mv $it/src/main/java/com/vaadin/flow/component/$defaultPackage/ $it/src/main/java/com/vaadin/flow/component/$package/
mv $it/src/test/java/com/vaadin/flow/component/$defaultPackage/ $it/src/test/java/com/vaadin/flow/component/$package/
mv $testbench/src/main/java/com/vaadin/flow/component/$defaultPackage/ $testbench/src/main/java/com/vaadin/flow/component/$package/
mv $demo/src/main/java/com/vaadin/flow/component/$defaultPackage/ $demo/src/main/java/com/vaadin/flow/component/$package/
mv $addon/src/main/java/com/vaadin/flow/component/$defaultPackage/ $addon/src/main/java/com/vaadin/flow/component/$package/
mv $addon/src/test/java/com/vaadin/flow/component/$defaultPackage/ $addon/src/test/java/com/vaadin/flow/component/$package/

echo Renaming files
mv $testbench/src/main/java/com/vaadin/flow/component/$package/testbench/"$defaultName"Element.java $testbench/src/main/java/com/vaadin/flow/component/$package/testbench/"$name"Element.java
mv $demo/src/main/java/com/vaadin/flow/component/$package/vaadincom/"$defaultName"View.java $demo/src/main/java/com/vaadin/flow/component/$package/vaadincom/"$name"View.java
mv $addon/src/main/java/com/vaadin/flow/component/$package/$defaultName.java $addon/src/main/java/com/vaadin/flow/component/$package/$name.java
mv $addon/src/test/java/com/vaadin/flow/component/$package/"$defaultName"Test.java $addon/src/test/java/com/vaadin/flow/component/$package/"$name"Test.java
mv $addon/src/test/java/com/vaadin/flow/component/$package/"$defaultName"SerializableTest.java $addon/src/test/java/com/vaadin/flow/component/$package/"$name"SerializableTest.java

echo Replacing strings
perl -pi -e 's,'"$defaultName"','"$name"',g' **/*.*
perl -pi -e 's,vaadin-'"$defaultNameDashed"',vaadin-'"$nameDashed"',g' **/*.* .travis.yml .gitignore
perl -pi -e 's,'"$defaultPackage"','"$package"',g' **/*.*

echo Done!
