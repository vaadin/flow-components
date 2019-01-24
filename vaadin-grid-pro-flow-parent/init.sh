#!/bin/bash
defaultName="GridPro"
defaultNameDashed="pro-grid"
defaultPackage="gridpro"

print_usage_and_exit() {
   echo $1
   echo
   echo "USAGE: $0 <ElementName>"
   exit 1
}

if [ -z "$1" ]
then
	print_usage_and_exit "Element name not specified"
fi

if ! echo "$1" | grep -qE '^[A-Z]+[a-zA-Z]*$'
then
	print_usage_and_exit "Element name should be in camel case starting with a capital letter"
fi
name="$1"
nameDashed=$(echo "$name" | sed 's/\(.\)\([A-Z]\)/\1-\2/g' | tr '[:upper:]' '[:lower:]')
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
mv $testbench/src/main/java/com/vaadin/flow/component/$package/testbench/${defaultName}Element.java $testbench/src/main/java/com/vaadin/flow/component/$package/testbench/${name}Element.java
mv $demo/src/main/java/com/vaadin/flow/component/$package/vaadincom/${defaultName}View.java $demo/src/main/java/com/vaadin/flow/component/$package/vaadincom/${name}View.java
mv $addon/src/main/java/com/vaadin/flow/component/$package/$defaultName.java $addon/src/main/java/com/vaadin/flow/component/$package/$name.java
mv $addon/src/test/java/com/vaadin/flow/component/$package/${defaultName}Test.java $addon/src/test/java/com/vaadin/flow/component/$package/${name}Test.java
mv $addon/src/test/java/com/vaadin/flow/component/$package/${defaultName}SerializableTest.java $addon/src/test/java/com/vaadin/flow/component/$package/${name}SerializableTest.java

echo Replacing strings
replace_in_files() {
	find LICENSE pom.xml README.md .gitignore .travis.yml "$addon" "$demo" "$it" "$testbench" -type f -execdir perl -pi -e "$1" {} \;
}

replace_in_files "s,$defaultName,$name,g"
replace_in_files "s,vaadin-$defaultNameDashed,vaadin-$nameDashed,g"
replace_in_files "s,$defaultPackage,$package,g"

echo Done!
