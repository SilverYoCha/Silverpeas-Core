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

import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;
import org.silverpeas.util.UnitUtil;
import org.silverpeas.util.time.TimeUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * User: Yohann Chastagnier
 * Date: 27/11/13
 */
@Entity
@Table(name = "st_event_reminds")
public class EventRemind extends AbstractJpaEntity<EventRemind, UuidIdentifier> {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "eventId", referencedColumnName = "id", nullable = false)
  private Event event;

  @Column(name = "timeBeforeStart", nullable = false)
  private Long timeBeforeStart = 0L;

  /**
   * Adding to the given iCalendar event all data managed by the remind entity.
   * @param iCalendarEvent
   * @return
   */
  protected EventRemind decorateICal4JEvent(VEvent iCalendarEvent) {
    VAlarm alarm = new VAlarm(new Dur(0, 0, 0,
        UnitUtil.getTimeData(timeBeforeStart * -1).getTimeConverted(TimeUnit.SEC).intValue()));
    iCalendarEvent.getAlarms().add(alarm);
    return this;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(final Event event) {
    this.event = event;
  }

  public Long getTimeBeforeStart() {
    return timeBeforeStart;
  }

  public void setTimeBeforeStart(final Long timeBeforeStart) {
    this.timeBeforeStart = timeBeforeStart;
  }
}
