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

import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * User: Yohann Chastagnier
 * Date: 22/11/13
 */
@Entity
@Table(name = "st_event_calendars")
public class EventCalendar extends AbstractJpaEntity<EventCalendar, UuidIdentifier> {

  @OneToMany(mappedBy = "calendar", cascade = {CascadeType.REMOVE})
  private EventUserSetting userSetting;

  @OneToOne(mappedBy = "calendar", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
  private EventPlanning planning;

  @Column(name = "instanceId", columnDefinition = "VARCHAR(50)")
  private String instanceId;

  @Column(name = "targetResourceUniqueId", columnDefinition = "VARCHAR(128)")
  private String targetResourceUniqueId;

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
