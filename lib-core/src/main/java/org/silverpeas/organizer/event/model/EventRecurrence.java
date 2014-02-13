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

import com.silverpeas.calendar.DayOfWeek;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RRule;
import org.silverpeas.organizer.Recurrence;
import org.silverpeas.organizer.event.constant.EventRecurrenceMonthType;
import org.silverpeas.organizer.event.constant.EventRecurrenceType;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This entity handled the recurrence data of an event.
 * It provides a method to calculate the next begin date of an occurence from a date and also a
 * method to calculate the maximum date.
 * User: Yohann Chastagnier
 * Date: 27/11/13
 */
@Entity
@Table(name = "st_event_recurrences")
public class EventRecurrence extends AbstractJpaEntity<EventRecurrence, UuidIdentifier>
    implements Recurrence {

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "planningId", referencedColumnName = "id", nullable = false)
  private EventPlanning planning;

  @OneToMany(mappedBy = "recurrence", fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
  private List<EventRecurrenceException> exceptions;

  @Column(name = "recurrenceType", nullable = false)
  private String type;

  @Column(name = "every", nullable = false)
  private Integer every;

  @Column(name = "daysOfWeek", columnDefinition = "CHAR(7)")
  private char[] daysOfWeek = new char[]{0, 0, 0, 0, 0, 0, 0};

  @Column(name = "recurrenceMonthType")
  private String recurrenceMonthType;

  @Column(name = "endingAt")
  private Long endingAtAsLong;

  @Column(name = "endingAfterXoccurences")
  private Integer endingAfterXoccurences;

  @Transient
  private Date endingAt;

  /**
   * Default JPA entity constructor.
   */
  protected EventRecurrence() {
  }

  /**
   * Adding to the given iCalendar event all data managed by the recurrence entity.
   * @param iCalendarEvent
   * @return
   */
  protected EventRecurrence decorateICal4JEvent(VEvent iCalendarEvent) {
    final Recur recurrence = new Recur();

    // Frequency
    switch (getType()) {
      case EVERY_X_YEARS:
        recurrence.setFrequency(Recur.YEARLY);
        break;
      case EVERY_X_MONTHS:
        recurrence.setFrequency(Recur.MONTHLY);
        if (EventRecurrenceMonthType.DAY_OF_WEEK == getRecurrenceMonthType()) {
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(getPlanning().getPeriod().getBeginDatable());
          recurrence.getDayList().add(new WeekDay(
              DayOfWeek.fromDayOfWeekNumber(calendar.get(Calendar.DAY_OF_WEEK)).toICal4J(),
              (calendar.get(Calendar.WEEK_OF_MONTH) - 1)));
        }
        break;
      case EVERY_X_WEEKS:
        recurrence.setFrequency(Recur.WEEKLY);
        // Days of week
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
          if (isDaysOfWeek(dayOfWeek)) {
            recurrence.getDayList().add(dayOfWeek.toICal4J());
          }
        }
        break;
      case EVERY_X_DAYS:
      default:
        recurrence.setFrequency(Recur.DAILY);
    }

    // Interval
    recurrence.setInterval(getEvery());

    // End of recurrence (date or count)
    if (getEndingAt() != null) {
      recurrence.setUntil(new DateTime(getEndingAt()));
    } else if (getEndingAfterXoccurences() != null) {
      recurrence.setCount(getEndingAfterXoccurences());
    }

    // Setting the recurrence to the iCalendar event
    iCalendarEvent.getProperties().add(new RRule(recurrence));

    // Exceptions if any
    if (exceptions != null) {
      for (EventRecurrenceException exception : exceptions) {
        exception.decorateICal4JEvent(iCalendarEvent);
      }
    }

    return this;
  }

  protected EventPlanning getPlanning() {
    return planning;
  }

  protected void setPlanning(final EventPlanning planning) {
    this.planning = planning;
  }

  protected List<EventRecurrenceException> getExceptions() {
    return exceptions;
  }

  protected void setExceptions(final List<EventRecurrenceException> exceptions) {
    this.exceptions = exceptions;
  }

  protected EventRecurrenceType getType() {
    return EventRecurrenceType.valueOf(type);
  }

  protected void setType(final EventRecurrenceType type) {
    this.type = type.name();
  }

  protected Integer getEvery() {
    return every;
  }

  protected void setEvery(final Integer every) {
    this.every = every;
  }

  protected boolean isDaysOfWeek(DayOfWeek dayOfWeek) {
    return daysOfWeek[dayOfWeek.ordinal()] != ((char) 0);
  }

  protected void setDaysOfWeek(DayOfWeek dayOfWeek, boolean aimed) {
    this.daysOfWeek[dayOfWeek.ordinal()] = (char) ((aimed) ? 1 : 0);
  }

  protected EventRecurrenceMonthType getRecurrenceMonthType() {
    return EventRecurrenceMonthType.valueOf(recurrenceMonthType);
  }

  protected void setRecurrenceMonthType(final EventRecurrenceMonthType recurrenceMonthType) {
    this.recurrenceMonthType = recurrenceMonthType.name();
  }

  protected Date getEndingAt() {
    if (endingAt == null && endingAtAsLong != null) {
      endingAt = new Date(endingAtAsLong);
    }
    return endingAt;
  }

  protected void setEndingAt(final Date endingAt) {
    this.endingAt = endingAt;
    this.endingAtAsLong = (endingAt != null) ? endingAt.getTime() : null;
  }

  protected Integer getEndingAfterXoccurences() {
    return endingAfterXoccurences;
  }

  protected void setEndingAfterXoccurences(final Integer endingAfterXoccurences) {
    this.endingAfterXoccurences = endingAfterXoccurences;
  }
}