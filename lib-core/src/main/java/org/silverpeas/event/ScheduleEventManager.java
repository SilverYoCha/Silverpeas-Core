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
import org.silverpeas.date.Period;
import org.silverpeas.event.model.Event;

import java.io.File;
import java.util.List;

/**
 * This interface provides signatures to manage events.
 */
public interface ScheduleEventManager {

  /**
   * Generates the ICS calendar file in the tempory repository of Silverpeas Server.
   * The name of the file is structured as the following rules :
   * - part 1 : "Calendar"
   * - part 2 : the date and hour of the generation starting (yyyyMMdd-HHMM)
   * - part 3 : the time in nanoseconds
   * - part 4 : the ".ics" file extension
   * Part 1 to 3 are separated by dash character.
   * @param events events to put in the ICS calendar file.
   * @return the reference to the generated file.
   */
  File generateICalendarFile(final Event... events);

  /**
   * Generates the ICS calendar file in the tempory repository of Silverpeas Server.
   * The name of the file is structured as the following rules :
   * - part 1 : "Calendar"
   * - part 2 : the date and hour of the generation starting (yyyyMMdd-HHmm)
   * - part 3 : the time in nanoseconds
   * - part 4 : the ".ics" file extension
   * Part 1 to 3 are separated by underscore character.
   * @param events events to put in the ICS calendar file.
   * @return the reference to the generated file.
   */
  File generateICalendarFile(final List<Event> events);

  /**
   * Generates the occurrences of events that occur in the specified period.
   * @param period the period in which occurrences of events occur.
   * @param events the events for which the occurrences have to be generated.
   * @return a list of occurrences occuring in the specified period, where an occurrence is
   *         represented by {@link ScheduleEvent}
   */
  List<ScheduleEvent> generateOccurrencesInPeriod(final Period period, final Event... events);

  /**
   * Generates the occurrences of events that occur in the specified period.
   * @param period the period in which occurrences of events occur.
   * @param events the events for which the occurrences have to be generated.
   * @return a list of occurrences occuring in the specified period, where an occurrence is
   *         represented by {@link ScheduleEvent}
   */
  List<ScheduleEvent> generateOccurrencesInPeriod(final Period period, final List<Event> events);

  /**
   * Generates the occurrences of the specified events that occur from the specified date with no
   * limit in the future.
   * @param date the inclusive date from which the event occurrences occur.
   * @param events the events for which the occurrences have to be generated.
   * @return a list of occurrences occuring from the specified date, where an occurrence is
   *         represented by {@link ScheduleEvent}
   */
  List<ScheduleEvent> generateOccurrencesFrom(final Datable date, final Event... events);

  /**
   * Generates the occurrences of the specified events that occur from the specified date with no
   * limit in the future.
   * @param date the inclusive date from which the event occurrences occur.
   * @param events the events for which the occurrences have to be generated.
   * @return a list of occurrences occuring from the specified date, where an occurrence is
   *         represented by {@link ScheduleEvent}
   */
  List<ScheduleEvent> generateOccurrencesFrom(final Datable date, final List<Event> events);
}
