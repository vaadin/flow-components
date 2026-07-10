> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — both halves of this DX ticket are still open: the javadocs do not document how to remove min/max limits, and the step-basis pitfall predicted in the discussion is real on IntegerField (the agreed "magic value" handling was only implemented for NumberField's infinity)
- **Hypothesis tested:** The bug is that "removing" the min limit with `setMin(Integer.MIN_VALUE)` silently changes step validation, triggered by combining it with a non-default step, observable as value 0/10 becoming invalid and value 2 becoming valid with step=10.
- **Regression?:** not a regression (DX gap since the API shipped; discussion dates to 2019)
- **Flavor:** Flow
- **Branch:** `repro/1633` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ main (25.3-SNAPSHOT, Flow 25.3-SNAPSHOT)
- **Present on main?:** yes
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** ![same value 10: default-min field valid, Integer.MIN_VALUE field invalid](https://raw.githubusercontent.com/vaadin/flow-components/repro-1633/repro-1633.png)

## Observed behavior

Two IntegerFields, both `setStep(10)` with step buttons; one with the default min, one with `setMin(Integer.MIN_VALUE)` (the issue's suggested way to "remove" the limit). Values entered and blurred:

| Value | default min | `setMin(Integer.MIN_VALUE)` |
| --- | --- | --- |
| 0 | valid | **invalid** |
| 2 | invalid | **valid** |
| 10 | valid | **invalid** |

Exactly the step shift Legioth predicted in the discussion: with `min = -2147483648` as the step basis, valid values become (…, -8, 2, 18, …) instead of (…, -10, 0, 10, …).

## Root cause (suspected)

1. `setMin` always marks the min as user-set and applies it as the step basis; the guard in step validation only excludes **infinite** values, which covers `NumberField.setMin(Double.NEGATIVE_INFINITY)` but not `IntegerField.setMin(Integer.MIN_VALUE)`:

https://github.com/vaadin/flow-components/blob/6c2510dd66085d0b26ea4d127f857f0a9ff3074f/vaadin-text-field-flow-parent/vaadin-text-field-flow/src/main/java/com/vaadin/flow/component/textfield/AbstractNumberField.java#L448-L465

2. The `IntegerField.setMin`/`setMax` javadocs (and `NumberField`'s) still say nothing about how to remove a limit — the original ask of this ticket:

https://github.com/vaadin/flow-components/blob/6c2510dd66085d0b26ea4d127f857f0a9ff3074f/vaadin-text-field-flow-parent/vaadin-text-field-flow/src/main/java/com/vaadin/flow/component/textfield/IntegerField.java#L205-L216

## Steps to reproduce

1. Open `http://localhost:8080/repro-1633`.
2. Type `0` into both fields and blur: the default-min field is valid, the `Integer.MIN_VALUE` field shows a step error.
3. Type `2`: the validity flips.

## Reproduction

How to run: start the server (`mvn package jetty:run -am -pl vaadin-text-field-flow-parent/vaadin-text-field-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1633`
- **Scaffold:** `vaadin-text-field-flow-parent/vaadin-text-field-flow-integration-tests/src/main/java/com/vaadin/flow/component/textfield/tests/Repro1633View.java`

## Notes

- The discussion in this ticket (pekam, Legioth, 2019) already converged on option 2 — treat `Integer.MIN_VALUE` as a magic value never used as the step basis. That is implemented for `NumberField` (via the `Double.isInfinite` check) but not for `IntegerField`.
- Remaining work for this ticket: (a) the javadoc documentation for removing limits on both fields, and (b) extending the magic-value handling to `IntegerField.setMin(Integer.MIN_VALUE)` / `setMax(Integer.MAX_VALUE)` for step-basis purposes.
