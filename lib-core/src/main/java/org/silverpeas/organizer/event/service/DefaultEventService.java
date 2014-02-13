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
package org.silverpeas.organizer.event.service;

import com.silverpeas.annotation.Service;
import com.silverpeas.calendar.Date;
import com.stratelia.webactiv.util.DateUtil;
import org.silverpeas.organizer.event.model.Event;
import org.silverpeas.organizer.event.repository.EventRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 02/12/13
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class DefaultEventService implements EventService {

  @Inject
  private EventRepository eventRepository;

  @Override
  public Event getEventById(final String eventId) {
    return eventRepository.getEventByIdEagerly(eventId);
  }

  @Override
  public List<Event> getEventsOn(final EventSearchCriteria criteria) {
    return eventRepository.getEventsEagerlyOn(criteria);
  }

  @Override
  public List<Event> getEventsSavedSince(final EventSearchCriteria criteria,
      final Date referenceDate) {
    return eventRepository.getEventsSavedSince(criteria, referenceDate);
  }

  @Override
  public List<Event> getTodayPotentialEventsToNotify() {
    return eventRepository.getPotentialEventsToNotifyAt(DateUtil.getNow());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public Event saveEvent(final Event event) {
    return null;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void deleteEvent(final Event event) {
    eventRepository.delete(event);
  }
}
