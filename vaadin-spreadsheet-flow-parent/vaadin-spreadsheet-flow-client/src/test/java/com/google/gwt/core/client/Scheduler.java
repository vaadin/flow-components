package com.google.gwt.core.client;

import com.google.gwt.core.client.impl.SchedulerImpl;

public class Scheduler {

    private static Scheduler _instance;

    public Scheduler() {
    }

    public static Scheduler get() {
        if (_instance == null)
            _instance = new Scheduler();
        return _instance;
    }

    public void scheduleDeferred(Scheduler.ScheduledCommand var1) {
        var1.execute();
    }

    public void scheduleEntry(Scheduler.RepeatingCommand var1) {

    }

    public void scheduleEntry(Scheduler.ScheduledCommand var1) {

    }

    public void scheduleFinally(Scheduler.RepeatingCommand var1) {

    }

    public void scheduleFinally(Scheduler.ScheduledCommand var1) {

    }

    public void scheduleFixedDelay(Scheduler.RepeatingCommand var1, int var2) {

    }

    public void scheduleFixedPeriod(Scheduler.RepeatingCommand var1, int var2) {

    }

    public void scheduleIncremental(Scheduler.RepeatingCommand var1) {

    }

    public interface ScheduledCommand {
        void execute();
    }

    public interface RepeatingCommand {
        boolean execute();
    }

}
