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
package org.silverpeas.event.model;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractPeriodDateAsLongJpaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * The planning of the event.
 * The begin date of the provided period represents the end date of the event if no recurrence
 * exists, or the end date of the last occurence of the event if a recurrence exists.
 * The end date of the provided period represents the end date of the event if no recurrence
 * exists, or the end date of the last occurence of the event if a recurrence exists.
 * User: Yohann Chastagnier
 * Date: 27/11/13
 */
@Entity
@Table(name = "st_event_planning")
public class EventPlanning
    extends AbstractPeriodDateAsLongJpaEntity<EventPlanning, UuidIdentifier> {

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "eventId", referencedColumnName = "id", nullable = false)
  private Event event;

  @OneToOne(mappedBy = "planning", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
  private EventRecurrence recurrence;

  /**
   * Adding to the given iCalendar event all data managed by the planning entity.
   * @param iCalendarEvent
   * @return
   */
  protected EventPlanning decorateICal4JEvent(VEvent iCalendarEvent) {
    if (isAllDay()) {
      // The date of event.
      iCalendarEvent.getProperties().add(new DtStart(new Date(getPeriod().getBeginDatable())));
    } else {
      // The begin and the end dates of the event.
      iCalendarEvent.getProperties().add(new DtStart(new DateTime(getPeriod().getBeginDatable())));
      iCalendarEvent.getProperties().add(new DtEnd(
          new DateTime(getPeriod().getBeginDatable().getTime() + getEvent().getDuration())));
    }
    // Recurrence if any
    if (getRecurrence() != null) {
      getRecurrence().decorateICal4JEvent(iCalendarEvent);
    }
    return this;
  }

  /**
   * Indicates if the event is covering all a day.
   * @return true if the event cover all a day.
   */
  public boolean isAllDay() {
    return getPeriod().getPeriodType().isDay();
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(final Event event) {
    this.event = event;
  }

  public EventRecurrence getRecurrence() {
    return recurrence;
  }

  public void setRecurrence(final EventRecurrence recurrence) {
    this.recurrence = recurrence;
  }
}
