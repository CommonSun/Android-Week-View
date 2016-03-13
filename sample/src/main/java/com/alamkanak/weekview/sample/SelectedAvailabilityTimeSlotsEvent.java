package com.alamkanak.weekview.sample;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.List;

/**
 * Created by BX on 3/13/2016.
 */
public class SelectedAvailabilityTimeSlotsEvent {
    List<WeekViewEvent> availableEvents;
    public SelectedAvailabilityTimeSlotsEvent(List<WeekViewEvent> events) {
        availableEvents = events;
    }
}
