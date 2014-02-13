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
package org.silverpeas.organizer.event.repository;

import com.stratelia.webactiv.util.DateUtil;
import org.silverpeas.date.Period;
import org.silverpeas.organizer.event.constant.EventAttendeeType;
import org.silverpeas.organizer.event.model.Event;
import org.silverpeas.organizer.event.service.EventSearchCriteria;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.jpa.AbstractJpaEntityRepository;
import org.silverpeas.persistence.repository.jpa.NamedParameters;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 02/12/13
 */
@Named
public class EventRepository extends AbstractJpaEntityRepository<Event, UuidIdentifier> {

  @Inject
  private EventInfoRepository eventInfoRepository;

  @Inject
  private EventParticipantRepository eventParticipantRepository;

  @Inject
  private EventPlanningRepository eventPlanningRepository;

  @Inject
  private EventRecurrenceRepository eventRecurrenceRepository;

  @Inject
  private EventRemindRepository eventRemindRepository;

  /**
   * Gets an event from its id.
   * @param eventId
   * @return
   */
  public Event getEventByIdEagerly(String eventId) {
    return loadEventEagerly(getById(eventId));
  }

  /**
   * Gets the event according to the given search criteria.
   * @param criteria
   * @return
   */
  public List<Event> getEventsEagerlyOn(EventSearchCriteria criteria) {
    List<Event> events = getEvents(criteria, null, false);
    for (Event event : events) {
      loadEventEagerly(event);
    }
    return events;
  }

  /**
   * Gets events created or updated according to the given search criteria and the given
   * reference date.
   * @param criteria
   * @param referenceDate
   * @return
   */
  public List<Event> getEventsSavedSince(final EventSearchCriteria criteria,
      final Date referenceDate) {
    List<Event> events = getEvents(criteria, referenceDate, false);
    for (Event event : events) {
      loadEventEagerly(event);
    }
    return events;
  }

  /**
   * Gets the list of potential events which have to be notified at the given date .
   * @param referenceDate
   * @return
   */
  public List<Event> getPotentialEventsToNotifyAt(final Date referenceDate) {
    List<Event> events = getEvents(
        new EventSearchCriteria().setPeriod(Period.from(referenceDate, DateUtil.MAXIMUM_DATE)),
        null, true);
    for (Event event : events) {
      loadEventEagerly(event);
    }
    return events;
  }

  /**
   * Gets the event according to the given search criteria and the eventual last update date
   * filter.
   * @param criteria
   * @return
   */
  private List<Event> getEvents(EventSearchCriteria criteria, final Date sinceLastDateUpdate,
      final boolean withRemindOnly) {
    NamedParameters parameters = initializeNamedParameters();
    StringBuilder jpqlQuery = new StringBuilder("select e from Event e");
    // The manadory period
    jpqlQuery.append(" where e.planning.beginDate < :");
    jpqlQuery.append(parameters.add("endDate", criteria.getPeriod().getEndDate()));
    jpqlQuery.append(" and e.planning.endDate > :");
    jpqlQuery.append(parameters.add("beginDate", criteria.getPeriod().getBeginDate()));
    // Exceptions : the clause eliminates only the events for which the given search period is
    // entirely included in the exception period
    jpqlQuery.append(" and (e.planning.recurrence.exceptions is empty");
    jpqlQuery.append(" or not (e.planning.recurrence.exceptions.beginDate < :endDate");
    jpqlQuery.append(" and e.planning.recurrence.exceptions.endDate > :beginDate))");
    // The component instance ids if any
    if (!criteria.getInstanceIds().isEmpty()) {
      jpqlQuery.append(" and e.target.instanceId in :");
      jpqlQuery.append(parameters.add("instanceIds", criteria.getInstanceIds()));
    }
    // The target resource unique ids if any
    if (!criteria.getTargetResourceUniqueIds().isEmpty()) {
      jpqlQuery.append(" and e.target.targetResourceUniqueId in :");
      jpqlQuery.append(parameters.add("targetIds", criteria.getTargetResourceUniqueIds()));
    }
    // The user or group ids if any
    if (!criteria.getUserIds().isEmpty() || !criteria.getGroupIds().isEmpty()) {
      StringBuilder userOrGroupIds = new StringBuilder();
      if (!criteria.getUserIds().isEmpty()) {
        userOrGroupIds.append(" e.createdBy in :");
        jpqlQuery.append(parameters.add("userIds", criteria.getUserIds()));
        userOrGroupIds.append(" or (e.participants.type in :");
        jpqlQuery.append(parameters.add("userType", EventAttendeeType.USER_PARTIPANT_TYPES));
        userOrGroupIds.append(" and e.participants.participantId in :userIds)");
      }
      if (!criteria.getGroupIds().isEmpty()) {
        if (userOrGroupIds.length() > 0) {
          userOrGroupIds.append(" or");
        }
        userOrGroupIds.append(" (e.participants.type in :");
        jpqlQuery.append(parameters.add("groupType", EventAttendeeType.GROUP_PARTIPANT_TYPES));
        userOrGroupIds.append(" and e.participants.participantId in :");
        userOrGroupIds.append(parameters.add("groupIds", criteria.getGroupIds()));
        jpqlQuery.append(" )");
      }
      jpqlQuery.append(" and (");
      jpqlQuery.append(userOrGroupIds.toString());
      jpqlQuery.append(" )");
    }
    // The remind filter
    if (withRemindOnly) {
      jpqlQuery.append(" and e.reminds is not empty");
    }
    // The last update date filter
    if (sinceLastDateUpdate != null) {
      jpqlQuery.append(" and (e.lastUpdateDate > :");
      jpqlQuery.append(parameters.add("lastUpdateDate", sinceLastDateUpdate));
      jpqlQuery.append(" or e.planning.lastUpdateDate > :lastUpdateDate");
      jpqlQuery.append(" or e.planning.recurrence.lastUpdateDate > :lastUpdateDate");
      jpqlQuery.append(" or e.planning.recurrence.exeception.lastUpdateDate > :lastUpdateDate");
      jpqlQuery.append(" or e.infos.lastUpdateDate > :lastUpdateDate");
      jpqlQuery.append(" or e.participants.lastUpdateDate > :lastUpdateDate");
      jpqlQuery.append(" or e.reminds.lastUpdateDate > :lastUpdateDate)");
    }
    List<Event> events = listFromJpqlString(jpqlQuery.toString(), parameters);
    for (Event event : events) {
      loadEventEagerly(event);
    }
    return events;
  }

  /**
   * Method to load all data of an event that are contained in a bag.
   * Normally foreign entities, those not contained in a bag, are fetched eagerly (see entity
   * annotations).
   * @param event
   */
  private Event loadEventEagerly(Event event) {
    if (event != null) {
      // Calling a method of the bag performs its load ...
      // Indeed, with JPA it is not possible to specify more than one bag with eager fetching
      // strategy in the context of a query execution.
      event.getInfos().isEmpty();
      event.getParticipants().isEmpty();
      event.getReminds().isEmpty();
    }
    return event;
  }
}
