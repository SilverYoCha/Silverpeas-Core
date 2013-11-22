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

import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import org.silverpeas.event.constant.EventInfoType;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * User: Yohann Chastagnier
 * Date: 27/11/13
 */
@Entity
@Table(name = "st_event_infos")
public class EventInfo extends AbstractJpaEntity<EventInfo, UuidIdentifier> {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "eventId", referencedColumnName = "id", nullable = false)
  private Event event;

  @Column(name = "infoType", nullable = false)
  private String type;

  @Column(name = "content", nullable = false)
  private String content;

  /**
   * Adding to the given iCalendar event all data managed by the info entity.
   * @param iCalendarEvent
   * @return
   */
  protected EventInfo decorateICal4JEvent(VEvent iCalendarEvent) {
    if (EventInfoType.DESCRIPTION == getType()) {
      iCalendarEvent.getProperties().add(new Description(getContent()));
    }
    return this;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(final Event event) {
    this.event = event;
  }

  public EventInfoType getType() {
    return EventInfoType.valueOf(type);
  }

  public void setType(final EventInfoType type) {
    this.type = type.name();
  }

  public String getContent() {
    return content;
  }

  public void setContent(final String content) {
    this.content = content;
  }
}
