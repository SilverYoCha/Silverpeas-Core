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

import org.silverpeas.event.util.EventUtil;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.awt.*;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 22/11/13
 */
@Entity
@Table(name = "st_event_calendars")
public class EventCalendar extends AbstractJpaEntity<EventCalendar, UuidIdentifier> {

  @OneToMany(mappedBy = "target", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
  private List<Event> events;

  @Column(name = "instanceId", columnDefinition = "VARCHAR(50)")
  private String instanceId;

  @Column(name = "targetResourceUniqueId", columnDefinition = "VARCHAR(128)")
  private String targetResourceUniqueId;

  public List<Event> getEvents() {
    return events;
  }

  public void setEvents(final List<Event> events) {
    this.events = events;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(final String instanceId) {
    this.instanceId = instanceId;
  }

  public String getTargetResourceUniqueId() {
    return targetResourceUniqueId;
  }

  public void setTargetResourceUniqueId(final String targetResourceUniqueId) {
    this.targetResourceUniqueId = targetResourceUniqueId;
  }
}
