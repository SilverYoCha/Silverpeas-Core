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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import org.silverpeas.event.model.Event;
import org.silverpeas.notification.message.MessageManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class give the possibility to methods of classes which handle ICalendar to return a lot of
 * calendar data.
 */
public class ICal4JCalendarContainer {
  private final Calendar calendar;

  private final Map<Event, VEvent> eventToICalendarEvent = new LinkedHashMap<Event, VEvent>();

  /**
   * Creates a new instace of ICalendar container.
   * @return
   */
  public static ICal4JCalendarContainer createEmpty() {
    return new ICal4JCalendarContainer();
  }

  /**
   * Creates a new instace of ICalendar container and fill it with the given events.
   * @param events
   * @return
   */
  public static ICal4JCalendarContainer from(List<Event> events) {
    ICal4JCalendarContainer calendarContainer = createEmpty();
    for (Event event : events) {
      calendarContainer.add(event);
    }
    return calendarContainer;
  }

  /**
   * Default hidden construtor
   */
  public ICal4JCalendarContainer() {
    calendar = new Calendar();
    calendar.getProperties().add(CalScale.GREGORIAN);
    calendar.getProperties()
        .add(new ProdId("-//Silverpeas//Calendar//" + MessageManager.getLanguage().toUpperCase()));
    calendar.getProperties().add(Version.VERSION_2_0);
  }

  /**
   * Adds an event to the contained ICalendar.
   * @param event
   * @return the ICalendarContainer itself.
   */
  public ICal4JCalendarContainer add(Event event) {
    VEvent iCalendarEvent = event.toICal4jEvent();
    calendar.getComponents().add(iCalendarEvent);
    eventToICalendarEvent.put(event, iCalendarEvent);
    return this;
  }

  /**
   * Gets the ICal4J calendar.
   * @return the ICal4J calendar.
   */
  public Calendar getCalendar() {
    return calendar;
  }

  /**
   * Gets the mapping between events persisted on Silverpeas side, and event reprsented in
   * ICalendar specification in an other side.
   * The order of the mapping keymap is the same as the one of the list from which the mapping has
   * been initialized.
   * @return
   */
  public Map<Event, VEvent> getEventToICalendarEvent() {
    return eventToICalendarEvent;
  }
}
