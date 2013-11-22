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

import com.silverpeas.calendar.Datable;
import com.silverpeas.util.CollectionUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import org.silverpeas.event.model.Event;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A generator of event occurrences built on the iCal4J library.
 */
public class ICal4jScheduleEventManager extends AbstractScheduleEventManager {

  @Override
  protected void generateICalendarFile(final List<Event> events,
      final OutputStream calendarOutputStream) throws IOException {
    CalendarOutputter calendarOutputter = new CalendarOutputter();
    try {
      calendarOutputter
          .output(ICal4JCalendarContainer.from(events).getCalendar(), calendarOutputStream);
    } catch (ValidationException e) {
      SilverTrace.error("Event", "ICal4jScheduleEventManager.generateICalendarFile",
          "EX_IMPOSSIBLE_TO_POPULATE_ICS_FILE", e);
    }
  }

  @Override
  public List<ScheduleEvent> generateOccurrencesInPeriod(final org.silverpeas.date.Period period,
      final Event... events) {
    return generateOccurrencesInPeriod(period, CollectionUtil.asList(events));
  }

  @Override
  public List<ScheduleEvent> generateOccurrencesInPeriod(final org.silverpeas.date.Period period,
      final List<Event> events) {
    return generateOccurrencesOf(events, period);
  }

  @Override
  public List<ScheduleEvent> generateOccurrencesFrom(final Datable date, final Event... events) {
    return generateOccurrencesFrom(date, CollectionUtil.asList(events));
  }

  @Override
  public List<ScheduleEvent> generateOccurrencesFrom(final Datable date, final List<Event> events) {
    return generateOccurrencesInPeriod(
        org.silverpeas.date.Period.from(date.asDate(), DateUtil.MAXIMUM_DATE), events);
  }

  /**
   * Generates the occurrences of the specified events that occur in the specified period.
   * @param events the events for which the occurrences has to be generated.
   * @param inPeriod the period.
   * @return a list of event occurrences that occur in the specified period.
   */
  private List<ScheduleEvent> generateOccurrencesOf(final List<Event> events,
      final org.silverpeas.date.Period inPeriod) {
    List<ScheduleEvent> scheduleEvents = new ArrayList<ScheduleEvent>();
    ICal4JCalendarContainer iCal4JCalendarContainer = ICal4JCalendarContainer.from(events);
    for (Map.Entry<Event, VEvent> entry : iCal4JCalendarContainer.getEventToICalendarEvent()
        .entrySet()) {
      Event event = entry.getKey();
      VEvent iCalendarEvent = entry.getValue();
      PeriodList periodList = iCalendarEvent.calculateRecurrenceSet(occuringIn(inPeriod));
      for (Object recurrencePeriodObject : periodList) {
        Period recurrencePeriod = (Period) recurrencePeriodObject;
        ScheduleEvent occurrence = ScheduleEvent.anOccurrence(event, org.silverpeas.date.Period
            .from(recurrencePeriod.getStart(), recurrencePeriod.getEnd()));
        scheduleEvents.add(occurrence);
      }
    }
    Collections.sort(scheduleEvents);
    return scheduleEvents;
  }

  /**
   * Transforms a Silverpeas period in a ICal4J one.
   * @param period
   * @return
   */
  private static Period occuringIn(final org.silverpeas.date.Period period) {
    return new Period(new DateTime(period.getBeginDatable()), new DateTime(period.getEndDatable()));
  }
}
