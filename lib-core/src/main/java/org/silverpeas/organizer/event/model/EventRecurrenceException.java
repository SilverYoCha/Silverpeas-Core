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

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ExDate;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractPeriodDateAsLongJpaEntity;
import org.silverpeas.util.time.TimeUnit;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The exception in recurrence is reprented by a period (begin and end dates).
 * <p/>
 * User: Yohann Chastagnier
 * Date: 02/12/13
 */
@Entity
@Table(name = "st_event_exceptions")
public class EventRecurrenceException
    extends AbstractPeriodDateAsLongJpaEntity<EventRecurrenceException, UuidIdentifier> {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "recurrenceId", referencedColumnName = "id", nullable = false)
  private EventRecurrence recurrence;

  /**
   * Adding to the given iCalendar event all data managed by the recurrence exception entity.
   * @param iCalendarEvent
   * @return
   */
  protected EventRecurrenceException decorateICal4JEvent(VEvent iCalendarEvent) {
    ExDate exceptions = (ExDate) iCalendarEvent.getProperties().getProperty(Property.EXDATE);
    if (exceptions == null) {
      exceptions = new ExDate();
      iCalendarEvent.getProperties().add(exceptions);
    }

    // Begin date
    exceptions.getDates().add(new DateTime(getPeriod().getBeginDatable()));

    // Each date covered partially or fully by the exception is set
    int limit = getPeriod().getCoveredDaysTimeData().getTimeConverted(TimeUnit.DAY).intValue();
    for (int i = 0; i < limit; i++) {
      exceptions.getDates().add(new DateTime(getPeriod().getBeginDatable().addDays(i)));
    }

    return this;
  }
}
