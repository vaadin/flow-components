---
name: migrate-junit6
description: Migrate JUnit 4 tests to JUnit 6 in this project. Use when asked to convert, migrate, or update tests from JUnit 4 to JUnit 6.
argument-hint: "[file or module path]"
---

Migrate the specified JUnit 4 test file(s) or module to JUnit 6. Read each file first, apply all rules below, and edit it. Do NOT change anything else (no reformatting, no logic changes).

## Import replacements

| JUnit 4 | JUnit 6 |
|---|---|
| `org.junit.Test` | `org.junit.jupiter.api.Test` |
| `org.junit.Assert` | `org.junit.jupiter.api.Assertions` |
| `org.junit.Before` | `org.junit.jupiter.api.BeforeEach` |
| `org.junit.After` | `org.junit.jupiter.api.AfterEach` |
| `org.junit.BeforeClass` | `org.junit.jupiter.api.BeforeAll` |
| `org.junit.AfterClass` | `org.junit.jupiter.api.AfterAll` |
| `org.junit.Ignore` | `org.junit.jupiter.api.Disabled` |
| `org.junit.Rule` | `org.junit.jupiter.api.extension.RegisterExtension` |

Also handle **static imports**: `static org.junit.Assert.*` becomes `static org.junit.jupiter.api.Assertions.*`.

## MockUIRule -> MockUIExtension

```java
// Before
@Rule
public MockUIRule ui = new MockUIRule();

// After
@RegisterExtension
MockUIExtension ui = new MockUIExtension();
```

Import: `com.vaadin.tests.MockUIRule` -> `com.vaadin.tests.MockUIExtension`

## EnableFeatureFlagRule -> EnableFeatureFlagExtension

```java
// Before
@Rule
public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
        MyFeatureFlagProvider.MY_FEATURE);

// After
@RegisterExtension
EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
        MyFeatureFlagProvider.MY_FEATURE);
```

Import: `com.vaadin.tests.EnableFeatureFlagRule` -> `com.vaadin.tests.EnableFeatureFlagExtension`

## Assertions

Replace all `Assert.` with `Assertions.`. Method signatures are the same, except for **message-carrying overloads** where the message moves to the **last** parameter:

```java
// Before
Assert.assertEquals("message", expected, actual);

// After
Assertions.assertEquals(expected, actual, "message");
```

Check every assertion call for a String message as the first argument and move it to the last position.

## Expected exceptions

Replace `@Test(expected = ...)` with `Assertions.assertThrows`:

```java
// Before
@Test(expected = IllegalStateException.class)
public void throwsException() {
    doSomething();
}

// After
@Test
public void throwsException() {
    Assertions.assertThrows(IllegalStateException.class, () -> doSomething());
}
```

## @Ignore -> @Disabled

```java
// Before
@Ignore("reason")

// After
@Disabled("reason")
```

## Visibility

JUnit 6 test classes and methods should use default (package) visibility, not `public`. Remove `public` from:
- The test class declaration
- All `@Test`, `@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@AfterAll` methods

Do NOT change visibility of helper methods, inner classes, or fields.

```java
// Before
public class FooTest {
    @Test
    public void someTest() {

// After
class FooTest {
    @Test
    void someTest() {
```

## Base classes

JUnit 6 does **not** process JUnit 4 `@Rule` annotations from parent classes. Tests will compile but fail at runtime (e.g. `UI.getCurrent()` returns null). Switch to the JUnit 6 base class:

| JUnit 4 | JUnit 6 |
|---|---|
| `AbstractSignalsUnitTest` | `AbstractSignalsJUnit6Test` |

## After editing

After converting all files, run the unit tests for the affected module to verify the conversion:

```
mvn test -pl <module-path>
```
