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

import org.silverpeas.date.Period;
import org.silverpeas.event.model.Event;
import org.silverpeas.event.service.EventServiceFactory;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * This class represents an event occurence positioned into the time.
 * User: Yohann Chastagnier
 * Date: 02/12/13
 */
public class ScheduleEvent implements Comparable<ScheduleEvent> {

  // The occurence id is build with :
  // - 1 : the begin date of the event occurrence
  // - 2 : the end date of the event occurrence
  // - 3 : the id of the event
  // By this way of structure, ScheduleEvent can be ordered direcly by their occurrence ids !
  private final String occurrenceId;
  private final Event event;
  private final Period timeSlot;

  /**
   * Creates an Schedule event from its occurence id (as occurrence id is structured).
   * @param occurrenceId
   * @return
   */
  public static ScheduleEvent anOccurrenceFromItsId(String occurrenceId) {
    StringTokenizer occurenceIdStructure = new StringTokenizer(occurrenceId, "@@@");
    String[] longTimeAsString = occurenceIdStructure.nextToken().split("-");
    Period timeSlot = Period.from(new Date(Long.valueOf(longTimeAsString[0])),
        new Date(Long.valueOf(longTimeAsString[1])));
    return anOccurrence(
        EventServiceFactory.getEventService().getEventById(occurenceIdStructure.nextToken()),
        timeSlot);
  }

  /**
   * Creates an schedule event (an event occurrence in other words).
   * @param event
   * @param timeSlot
   * @return
   */
  public static ScheduleEvent anOccurrence(Event event, Period timeSlot) {
    return new ScheduleEvent(event, timeSlot);
  }

  /**
   * Default hidden constructor.
   * @param event
   * @param timeSlot
   */
  private ScheduleEvent(Event event, Period timeSlot) {
    this.event = event;
    this.timeSlot = timeSlot.clone();
    this.occurrenceId =
        timeSlot.getBeginDate().getTime() + "-" + timeSlot.getEndDate() + "@@@" + event.getId();
  }

  /**
   * Gets an occurrence identifier.
   * @return
   */
  public String getOccurrenceId() {
    return occurrenceId;
  }

  /**
   * Gets an event.
   * @return
   */
  public Event getEvent() {
    return event;
  }

  /**
   * Gets the time slot of the occurrence.
   * @return
   */
  public Period getTimeSlot() {
    return timeSlot;
  }

  @Override
  public int compareTo(final ScheduleEvent otherScheduleEvent) {
    return getOccurrenceId().compareTo(otherScheduleEvent.getOccurrenceId());
  }
}
