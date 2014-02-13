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
import com.stratelia.webactiv.beans.admin.UserDetail;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import org.silverpeas.organizer.Schedulable;
import org.silverpeas.organizer.event.constant.EventPriority;
import org.silverpeas.organizer.event.constant.EventPrivacyType;
import org.silverpeas.organizer.event.constant.EventType;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.net.URI;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 22/11/13
 */
@Entity
@Table(name = "st_events")
public class Event extends AbstractJpaEntity<Event, UuidIdentifier> implements Schedulable {

  @Column(name = "eventType", columnDefinition = "VARCHAR(40)", nullable = false)
  private String type = EventType.INDIVIDUAL.name();

  @Column(name = "privacyType", columnDefinition = "VARCHAR(40)", nullable = false)
  private String privacyType = EventPrivacyType.PRIVATE.name();

  @Column(name = "eventPriority", nullable = false)
  private Integer priority = EventPriority.NONE.ordinal();

  @Column(name = "title", columnDefinition = "VARCHAR(255)", nullable = false)
  private String title;

  @OneToMany(mappedBy = "event", cascade = {CascadeType.REMOVE})
  private EventUserSetting userSetting;

  @OneToOne(mappedBy = "event", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
  private EventPlanning planning;

  @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
  private List<EventInfo> infos;

  @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
  private List<EventRemind> reminds;

  @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
  private List<EventAttendee> participants;

  @Column(name = "duration", nullable = false)
  private Long duration = 0L;

  @Transient
  private UserDetail organizer;

  /**
   * Indicates if the event is a personal one, in other words, if it corresponds to the personal
   * schedule event of a user.
   * @return
   */
  public boolean isPersonal() {
    return StringUtil.isNotDefined(getCalendar().getInstanceId()) &&
        StringUtil.isNotDefined(getCalendar().getTargetResourceUniqueId());
  }

  /**
   * Gets the user who is the author of the event.
   * @return
   */
  public UserDetail getOrganizer() {
    if (getType().isIndividual() && organizer == null) {
      // For now, the organizer is the user passed to the technical entity createdBy information.
      organizer = getCreator();
    }
    return organizer;
  }

  /**
   * Creates an ICalendar event with all required data.
   * @return
   */
  public VEvent toICal4jEvent() {
    VEvent iCalendarEvent = new VEvent();
    // Unique identifier is the same as the Silverpeas Event (because it is unique too !)
    iCalendarEvent.getProperties().add(new Uid(getId()));
    // The summary (title)
    iCalendarEvent.getProperties().add(new Summary(getTitle()));
    // Planning decoration
    getPlanning().decorateICal4JEvent(iCalendarEvent);
    // Organizer if any
    if (getOrganizer() != null) {
      Organizer organizer = new Organizer(URI.create("mailto:" + getOrganizer().geteMail()));
      organizer.getParameters().add(new Cn(getOrganizer().getDisplayedName()));
      iCalendarEvent.getProperties().add(organizer);
    }
    // Infos
    if (infos != null) {
      for (EventInfo info : infos) {
        info.decorateICal4JEvent(iCalendarEvent);
      }
    }
    // Reminds
    if (reminds != null) {
      for (EventRemind remind : reminds) {
        remind.decorateICal4JEvent(iCalendarEvent);
      }
    }
    // Participants
    if (participants != null) {
      for (EventAttendee participant : participants) {
        participant.decorateICal4JEvent(iCalendarEvent);
      }
    }
    // Returning the event
    return iCalendarEvent;
  }

  public EventCalendar getCalendar() {
    return getPlanning().getCalendar();
  }

  public EventType getType() {
    return EventType.valueOf(type);
  }

  public void setType(final EventType type) {
    this.type = type.name();
    // The below information is in relation with the type of the event. It has to be cleared...
    this.organizer = null;
  }

  public EventPrivacyType getPrivacyType() {
    return EventPrivacyType.valueOf(privacyType);
  }

  public void setPrivacyType(final EventPrivacyType privacyType) {
    this.privacyType = privacyType.name();
  }

  public EventPriority getPriority() {
    return EventPriority.values()[priority];
  }

  public void setPriority(final EventPriority priority) {
    this.priority = priority.ordinal();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public EventUserSetting getUserSetting() {
    return userSetting;
  }

  public void setUserSetting(final EventUserSetting userSetting) {
    this.userSetting = userSetting;
  }

  public EventPlanning getPlanning() {
    return planning;
  }

  public void setPlanning(final EventPlanning planning) {
    this.planning = planning;
  }

  public List<EventInfo> getInfos() {
    return infos;
  }

  public void setInfos(final List<EventInfo> infos) {
    this.infos = infos;
  }

  public List<EventRemind> getReminds() {
    return reminds;
  }

  public void setReminds(final List<EventRemind> reminds) {
    this.reminds = reminds;
  }

  public List<EventAttendee> getParticipants() {
    return participants;
  }

  public void setParticipants(final List<EventAttendee> participants) {
    this.participants = participants;
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration(final Long duration) {
    this.duration = duration;
  }
}
