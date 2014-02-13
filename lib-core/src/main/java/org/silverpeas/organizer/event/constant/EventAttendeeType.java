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
package org.silverpeas.organizer.event.constant;

import net.fortuna.ical4j.model.parameter.Role;

import java.util.EnumSet;
import java.util.Set;

/**
 * User: Yohann Chastagnier
 * Date: 28/11/13
 */
public enum EventAttendeeType {
  USER_LEADER,
  USER_OPTIONAL,
  USER_REQUIRED,
  GROUP_OPTIONAL,
  GROUP_REQUIRED;

  public static Set<EventAttendeeType> USER_PARTIPANT_TYPES =
      EnumSet.of(USER_LEADER, USER_OPTIONAL, USER_REQUIRED);

  public static Set<EventAttendeeType> GROUP_PARTIPANT_TYPES =
      EnumSet.of(GROUP_OPTIONAL, GROUP_REQUIRED);

  /**
   * Gets the corresponding {@link Role}.
   * @return
   */
  public Role toICal4JRoleParameter() {
    switch (this) {
      case USER_LEADER:
        return Role.CHAIR;
      case USER_OPTIONAL:
      case GROUP_OPTIONAL:
        return Role.OPT_PARTICIPANT;
      default:
        return Role.REQ_PARTICIPANT;
    }
  }

  /**
   * Indicates id the current instance is a USER one.
   * @return
   */
  public boolean isUser() {
    return this == USER_LEADER || this == USER_OPTIONAL || this == USER_REQUIRED;
  }

  /**
   * Indicates id the current instance is a GROUP one.
   * @return
   */
  public boolean isGroup() {
    return this == GROUP_OPTIONAL || this == GROUP_REQUIRED;
  }
}
