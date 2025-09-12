/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.stepper;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link Stepper}.
 */
public class StepperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createStepperInDefaultState() {
        Stepper stepper = new Stepper();

        Assert.assertEquals("Initial step count is invalid", 0,
                stepper.getStepCount());
        Assert.assertEquals("Tag name is invalid", "vaadin-stepper",
                stepper.getElement().getTag());
        Assert.assertEquals("Default orientation is invalid",
                Stepper.Orientation.VERTICAL, stepper.getOrientation());
    }

    @Test
    public void createStepperWithSteps() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step step3 = new Step("Step 3");
        Stepper stepper = new Stepper(step1, step2, step3);

        Assert.assertEquals("Initial step count is invalid", 3,
                stepper.getStepCount());
        Assert.assertEquals("First step is invalid", step1,
                stepper.getStepAt(0));
        Assert.assertEquals("Second step is invalid", step2,
                stepper.getStepAt(1));
        Assert.assertEquals("Third step is invalid", step3,
                stepper.getStepAt(2));
    }

    @Test
    public void setOrientation() {
        Stepper stepper = new Stepper();

        stepper.setOrientation(Stepper.Orientation.HORIZONTAL);
        Assert.assertEquals("Orientation is invalid",
                Stepper.Orientation.HORIZONTAL, stepper.getOrientation());

        stepper.setOrientation(Stepper.Orientation.VERTICAL);
        Assert.assertEquals("Orientation is invalid",
                Stepper.Orientation.VERTICAL, stepper.getOrientation());
    }

    @Test
    public void setOrientationNull_throwsException() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Orientation cannot be null");

        Stepper stepper = new Stepper();
        stepper.setOrientation(null);
    }

    @Test
    public void addSteps() {
        Stepper stepper = new Stepper();
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");

        stepper.add(step1, step2);

        Assert.assertEquals("Step count after add is invalid", 2,
                stepper.getStepCount());
        Assert.assertEquals("First step is invalid", step1,
                stepper.getStepAt(0));
        Assert.assertEquals("Second step is invalid", step2,
                stepper.getStepAt(1));
    }

    @Test
    public void addNullSteps_throwsException() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Steps to add cannot be null");

        Stepper stepper = new Stepper();
        stepper.add((Step[]) null);
    }

    @Test
    public void addStepWithNull_throwsException() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Individual step to add cannot be null");

        Stepper stepper = new Stepper();
        stepper.add(new Step("Step 1"), null);
    }

    @Test
    public void removeSteps() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step step3 = new Step("Step 3");
        Stepper stepper = new Stepper(step1, step2, step3);

        stepper.remove(step2);

        Assert.assertEquals("Step count after remove is invalid", 2,
                stepper.getStepCount());
        Assert.assertEquals("First step is invalid", step1,
                stepper.getStepAt(0));
        Assert.assertEquals("Second step is invalid", step3,
                stepper.getStepAt(1));
    }

    @Test
    public void removeAll() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Stepper stepper = new Stepper(step1, step2);

        stepper.removeAll();

        Assert.assertEquals("Step count after removeAll is invalid", 0,
                stepper.getStepCount());
    }

    @Test
    public void getStepAt_invalidIndex_throwsException() {
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage("Index: 0, Size: 0");

        Stepper stepper = new Stepper();
        stepper.getStepAt(0);
    }

    @Test
    public void getStepAt_negativeIndex_throwsException() {
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage("Index: -1, Size: 0");

        Stepper stepper = new Stepper();
        stepper.getStepAt(-1);
    }

    @Test
    public void indexOf_returnsCorrectIndex() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step step3 = new Step("Step 3");
        Stepper stepper = new Stepper(step1, step2, step3);

        Assert.assertEquals("Index of step1 is invalid", 0,
                stepper.indexOf(step1));
        Assert.assertEquals("Index of step2 is invalid", 1,
                stepper.indexOf(step2));
        Assert.assertEquals("Index of step3 is invalid", 2,
                stepper.indexOf(step3));
    }

    @Test
    public void indexOf_stepNotFound_returnsNegativeOne() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step stepNotAdded = new Step("Step 3");
        Stepper stepper = new Stepper(step1, step2);

        Assert.assertEquals("Index of not added step should be -1", -1,
                stepper.indexOf(stepNotAdded));
    }

    @Test
    public void indexOf_nullStep_returnsNegativeOne() {
        Step step1 = new Step("Step 1");
        Stepper stepper = new Stepper(step1);

        Assert.assertEquals("Index of null should be -1", -1,
                stepper.indexOf(null));
    }

    @Test
    public void replace_validIndex() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step step3 = new Step("Step 3");
        Step newStep = new Step("New Step");
        Stepper stepper = new Stepper(step1, step2, step3);

        stepper.replace(1, newStep);

        Assert.assertEquals("Step count after replace is invalid", 3,
                stepper.getStepCount());
        Assert.assertEquals("First step is invalid", step1,
                stepper.getStepAt(0));
        Assert.assertEquals("Replaced step is invalid", newStep,
                stepper.getStepAt(1));
        Assert.assertEquals("Third step is invalid", step3,
                stepper.getStepAt(2));
    }

    @Test
    public void replace_invalidIndex_throwsException() {
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage("Index: 3, Size: 2");

        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step newStep = new Step("New Step");
        Stepper stepper = new Stepper(step1, step2);

        stepper.replace(3, newStep);
    }

    @Test
    public void replace_nullStep_throwsException() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("New step cannot be null");

        Step step1 = new Step("Step 1");
        Stepper stepper = new Stepper(step1);

        stepper.replace(0, null);
    }

    @Test
    public void getSteps_returnsAllSteps() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step step3 = new Step("Step 3");
        Stepper stepper = new Stepper(step1, step2, step3);

        Step[] steps = stepper.getSteps().toArray(Step[]::new);

        Assert.assertEquals("Step count is invalid", 3, steps.length);
        Assert.assertEquals("First step is invalid", step1, steps[0]);
        Assert.assertEquals("Second step is invalid", step2, steps[1]);
        Assert.assertEquals("Third step is invalid", step3, steps[2]);
    }

    @Test
    public void getActiveStep() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step step3 = new Step("Step 3");
        Stepper stepper = new Stepper(step1, step2, step3);

        Assert.assertNull("Initially no step should be active",
                stepper.getActiveStep());

        step2.setState(Step.State.ACTIVE);
        Assert.assertEquals("Active step should be step2", step2,
                stepper.getActiveStep());
    }

    @Test
    public void setStepState() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Stepper stepper = new Stepper(step1, step2);

        stepper.setStepState(Step.State.COMPLETED, 0);
        Assert.assertEquals("Step state should be completed",
                Step.State.COMPLETED, step1.getState());

        stepper.setStepState(Step.State.ACTIVE, 1);
        Assert.assertEquals("Step state should be active", Step.State.ACTIVE,
                step2.getState());
    }

    @Test
    public void setStepState_nullState_throwsException() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("State cannot be null");

        Step step1 = new Step("Step 1");
        Stepper stepper = new Stepper(step1);

        stepper.setStepState(null, 0);
    }

    @Test
    public void setStepState_invalidIndex_ignored() {
        Step step1 = new Step("Step 1");
        Stepper stepper = new Stepper(step1);

        // Should not throw exception, just ignore
        stepper.setStepState(Step.State.COMPLETED, 5);
        stepper.setStepState(Step.State.COMPLETED, -1);

        Assert.assertEquals("Step state should remain inactive",
                Step.State.INACTIVE, step1.getState());
    }

    @Test
    public void completeStepsUntil() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step step3 = new Step("Step 3");
        Stepper stepper = new Stepper(step1, step2, step3);

        stepper.completeStepsUntil(1);

        Assert.assertEquals("Step 1 should be completed", Step.State.COMPLETED,
                step1.getState());
        Assert.assertEquals("Step 2 should be completed", Step.State.COMPLETED,
                step2.getState());
        Assert.assertEquals("Step 3 should remain inactive",
                Step.State.INACTIVE, step3.getState());
    }

    @Test
    public void completeStepsUntil_negativeIndex_ignored() {
        Step step1 = new Step("Step 1");
        Stepper stepper = new Stepper(step1);

        stepper.completeStepsUntil(-1);

        Assert.assertEquals("Step should remain inactive", Step.State.INACTIVE,
                step1.getState());
    }

    @Test
    public void completeStepsUntil_indexBeyondRange() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Stepper stepper = new Stepper(step1, step2);

        stepper.completeStepsUntil(5);

        Assert.assertEquals("Step 1 should be completed", Step.State.COMPLETED,
                step1.getState());
        Assert.assertEquals("Step 2 should be completed", Step.State.COMPLETED,
                step2.getState());
    }

    @Test
    public void reset() {
        Step step1 = new Step("Step 1");
        Step step2 = new Step("Step 2");
        Step step3 = new Step("Step 3");
        Stepper stepper = new Stepper(step1, step2, step3);

        step1.setState(Step.State.COMPLETED);
        step2.setState(Step.State.ACTIVE);
        step3.setState(Step.State.ERROR);

        stepper.reset();

        Assert.assertEquals("Step 1 should be inactive", Step.State.INACTIVE,
                step1.getState());
        Assert.assertEquals("Step 2 should be inactive", Step.State.INACTIVE,
                step2.getState());
        Assert.assertEquals("Step 3 should be inactive", Step.State.INACTIVE,
                step3.getState());
    }

    @Test
    public void themeVariants() {
        Stepper stepper = new Stepper();

        stepper.addThemeVariants(StepperVariant.LUMO_SMALL);
        Assert.assertTrue("Should have small variant",
                stepper.getThemeNames().contains("small"));

        stepper.removeThemeVariants(StepperVariant.LUMO_SMALL);
        Assert.assertFalse("Should not have small variant",
                stepper.getThemeNames().contains("small"));
    }
}