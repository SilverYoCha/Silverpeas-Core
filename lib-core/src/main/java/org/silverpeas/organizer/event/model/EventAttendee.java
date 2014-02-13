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
package org.silverpeas.organizer.event.model;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.property.Attendee;
import org.apache.commons.lang.NotImplementedException;
import org.silverpeas.organizer.event.constant.EventParticipationStatus;
import org.silverpeas.organizer.event.constant.EventAttendeeType;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.net.URI;

/**
 * User: Yohann Chastagnier
 * Date: 27/11/13
 */
@Entity
@Table(name = "st_event_attendees")
public class EventAttendee extends AbstractJpaEntity<EventAttendee, UuidIdentifier> {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "eventId", referencedColumnName = "id", nullable = false)
  private Event event;

  @Column(name = "attendeeType", nullable = false)
  private String type = EventAttendeeType.USER_OPTIONAL.name();

  @Column(name = "attendeeId", nullable = false)
  private String attendeeId;

  @Column(name = "participationStatus", nullable = false)
  private String participationStatus = EventParticipationStatus.NOT_YET_ANSWERED.name();

  @Transient
  private Group group;

  @Transient
  private UserDetail user;

  /**
   * Adding to the given iCalendar event all data managed by the participant entity.
   * @param iCalendarEvent
   * @return
   */
  protected EventAttendee decorateICal4JEvent(VEvent iCalendarEvent) {
    if (getType().isUser()) {
      addUserICal4JAttendee(iCalendarEvent, getUser());
    } else if (getType().isGroup()) {
      Group group = getGroup();
      for (UserDetail user : group.getAllUsers()) {
        addUserICal4JAttendee(iCalendarEvent, user);
      }
    } else {
      throw new NotImplementedException();
    }
    return this;
  }

  /**
   * Add a user
   * @param user
   */
  private void addUserICal4JAttendee(VEvent iCalendarEvent, UserDetail user) {
    Attendee attendee = new Attendee(URI.create("mailto:" + user.geteMail()));
    attendee.getParameters().add(getType().toICal4JRoleParameter());
    attendee.getParameters().add(new Cn(user.getDisplayedName()));
    attendee.getParameters().add(getParticipationStatus().toICal4JParticipationStatusParameter());
    iCalendarEvent.getProperties().add(attendee);
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(final Event event) {
    this.event = event;
  }

  public EventAttendeeType getType() {
    return EventAttendeeType.valueOf(type);
  }

  public void setType(final EventAttendeeType type) {
    this.type = type.name();
  }

  public String getAttendeeId() {
    return attendeeId;
  }

  public void setAttendeeId(final String attendeeId) {
    this.attendeeId = attendeeId;
  }

  public Group getGroup() {
    if (getType().isGroup() && StringUtil.isDefined(attendeeId)) {
      if (user == null) {
        group = Group.getById(attendeeId);
      }
    } else {
      group = null;
    }
    return group;
  }

  public void setGroup(final Group group) {
    this.group = group;
    attendeeId = user.getId();
  }

  public UserDetail getUser() {
    if (getType().isUser() && StringUtil.isDefined(attendeeId)) {
      if (user == null) {
        user = UserDetail.getById(attendeeId);
      }
    } else {
      user = null;
    }
    return user;
  }

  public void setUser(final UserDetail user) {
    this.user = user;
    attendeeId = user.getId();
  }

  public EventParticipationStatus getParticipationStatus() {
    return EventParticipationStatus.valueOf(participationStatus);
  }

  public void setParticipationStatus(final EventParticipationStatus participationStatus) {
    this.participationStatus = participationStatus.name();
  }
}
