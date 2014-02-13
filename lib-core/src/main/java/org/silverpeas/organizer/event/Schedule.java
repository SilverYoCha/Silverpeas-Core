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
package org.silverpeas.organizer.event;

import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.MapUtil;
import org.silverpeas.organizer.event.model.Event;
import org.silverpeas.organizer.event.service.EventSearchCriteria;
import org.silverpeas.organizer.event.service.EventService;
import org.silverpeas.organizer.event.service.EventServiceFactory;

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
  private List<EventOccurence> eventOccurences = new ArrayList<EventOccurence>();

  // Indexs to improve performance ...
  private Map<String, EventOccurence> scheduleEventsIndexedByOccurrenceId =
      new HashMap<String, EventOccurence>();
  private Map<Event, List<EventOccurence>> scheduleEventsIndexedByEvent =
      new HashMap<Event, List<EventOccurence>>();

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
    eventOccurences.clear();
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
    eventOccurences.clear();
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
      List<EventOccurence> currentEventOccurences = scheduleEventsIndexedByEvent.remove(event);
      if (currentEventOccurences != null) {
        for (EventOccurence currentEventOccurence : currentEventOccurences) {
          scheduleEventsIndexedByOccurrenceId.remove(currentEventOccurence.getOccurrenceId());
          eventOccurences.remove(currentEventOccurence);
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
    List<EventOccurence> eventOccurences = ScheduleManagerFactory.getScheduleEventManager()
        .generateOccurrencesInPeriod(getEventCriteria().getPeriod(), events);
    for (EventOccurence eventOccurence : eventOccurences) {
      addOccurrence(eventOccurence);
    }
  }

  /**
   * The only method to use to add an schedule event in associated containers.
   * @param eventOccurence
   */
  private void addOccurrence(EventOccurence eventOccurence) {
    eventOccurences.add(eventOccurence);
    scheduleEventsIndexedByOccurrenceId.put(eventOccurence.getOccurrenceId(), eventOccurence);
    MapUtil.putAddList(scheduleEventsIndexedByEvent, eventOccurence.getEvent(), eventOccurence);
  }

  /**
   * Gets the event service provider.
   * @return
   */
  private EventService getEventService() {
    return EventServiceFactory.getEventService();
  }
}
