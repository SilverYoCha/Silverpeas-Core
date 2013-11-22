/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.event;

import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.MapUtil;
import org.silverpeas.event.model.Event;
import org.silverpeas.event.service.EventSearchCriteria;
import org.silverpeas.event.service.EventService;
import org.silverpeas.event.service.EventServiceFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles by a centralized way one or several schedules with its events.
 * <p/>
 * User: Yohann Chastagnier
 * Date: 02/12/13
 */
public class Schedule {

  // The criteria to complete before performing a load.
  private EventSearchCriteria eventCriteria = new EventSearchCriteria();

  // The container of schedule occurrences.
  private List<ScheduleEvent> scheduleEvents = new ArrayList<ScheduleEvent>();

  // Indexs to improve performance ...
  private Map<String, ScheduleEvent> scheduleEventsIndexedByOccurrenceId =
      new HashMap<String, ScheduleEvent>();
  private Map<Event, List<ScheduleEvent>> scheduleEventsIndexedByEvent =
      new HashMap<Event, List<ScheduleEvent>>();

  /**
   * Gets the event criteria container in order to set precisely the load desired.
   * @return
   */
  public EventSearchCriteria getEventCriteria() {
    return eventCriteria;
  }

  /**
   * Reload event occurrences if necessary.
   */
  public void reloadIfNecessary() {
    scheduleEvents.clear();
    scheduleEventsIndexedByOccurrenceId.clear();
    scheduleEventsIndexedByEvent.clear();
    List<Event> events = getEventService().getEventsOn(eventCriteria);
    for (Event event : events) {
      buildScheduleEvents(event);
    }
  }

  /**
   * Loads (or reloads) data of events and building occurence of them for the schedule period.
   */
  public void load() {
    scheduleEvents.clear();
    scheduleEventsIndexedByOccurrenceId.clear();
    scheduleEventsIndexedByEvent.clear();
    List<Event> events = getEventService().getEventsOn(eventCriteria);
    for (Event event : events) {
      buildScheduleEvents(event);
    }
  }

  /**
   * Reloads the event data from the given one and building its occurences for the schedule period.
   */
  public void reloadEvent(Event event) {
    if (event != null) {
      // Removing the event occurences from the schedule.
      List<ScheduleEvent> currentScheduleEvents = scheduleEventsIndexedByEvent.remove(event);
      if (currentScheduleEvents != null) {
        for (ScheduleEvent currentScheduleEvent : currentScheduleEvents) {
          scheduleEventsIndexedByOccurrenceId.remove(currentScheduleEvent.getOccurrenceId());
          scheduleEvents.remove(currentScheduleEvent);
        }
      }
      // Building the new event occurrences
      buildScheduleEvents(getEventService().getEventById(event.getId()));
    }
  }

  /**
   * Builds of schedule event occurrences on the specified period.
   * @param events
   */
  private void buildScheduleEvents(Event... events) {
    if (events != null) {
      buildScheduleEvents(CollectionUtil.asList(events));
    }
  }

  /**
   * Builds of schedule event occurrences on the specified period.
   * @param events
   */
  private void buildScheduleEvents(List<Event> events) {
    if (events == null) {
      return;
    }
    List<ScheduleEvent> scheduleEvents = ScheduleManagerFactory.getScheduleEventManager()
        .generateOccurrencesInPeriod(getEventCriteria().getPeriod(), events);
    for (ScheduleEvent scheduleEvent : scheduleEvents) {
      addOccurrence(scheduleEvent);
    }
  }

  /**
   * The only method to use to add an schedule event in associated containers.
   * @param scheduleEvent
   */
  private void addOccurrence(ScheduleEvent scheduleEvent) {
    scheduleEvents.add(scheduleEvent);
    scheduleEventsIndexedByOccurrenceId.put(scheduleEvent.getOccurrenceId(), scheduleEvent);
    MapUtil.putAddList(scheduleEventsIndexedByEvent, scheduleEvent.getEvent(), scheduleEvent);
  }

  /**
   * Gets the event service provider.
   * @return
   */
  private EventService getEventService() {
    return EventServiceFactory.getEventService();
  }
}
