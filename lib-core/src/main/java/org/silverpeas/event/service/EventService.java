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
package org.silverpeas.event.service;

import com.silverpeas.calendar.Date;
import org.silverpeas.event.model.Event;

import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 02/12/13
 */
public interface EventService {

  /**
   * Gets the event from its id.
   * @param eventId
   * @return
   */
  Event getEventById(String eventId);

  /**
   * Gets the event according to the given search criteria.
   * @param criteria
   * @return
   */
  List<Event> getEventsOn(EventSearchCriteria criteria);

  /**
   * Gets events created or updated according to the given search criteria and the given
   * reference date.
   * @param criteria
   * @param referenceDate
   * @return
   */
  List<Event> getEventsSavedSince(EventSearchCriteria criteria, Date referenceDate);

  /**
   * Gets the list of potential events which have to be today notified.
   * @return
   */
  List<Event> getTodayPotentialEventsToNotify();

  /**
   * Saves an event.
   * @return
   */
  Event saveEvent(Event event);

  /**
   * Deletes an event.
   * @return
   */
  void deleteEvent(Event event);
}
