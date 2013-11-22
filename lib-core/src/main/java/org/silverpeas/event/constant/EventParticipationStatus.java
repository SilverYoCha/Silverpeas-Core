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
package org.silverpeas.event.constant;

import net.fortuna.ical4j.model.parameter.PartStat;

/**
 * User: Yohann Chastagnier
 * Date: 28/11/13
 */
public enum EventParticipationStatus {
  ANSWER_IS_NOT_POSSIBLE,
  NOT_YET_ANSWERED,
  PARTICIPATE,
  PARTICIPATE_POSSIBLY,
  DOES_NOT_PARTICIPATE;

  /**
   * Gets the corresponding ICal4J participation status parameter.
   * @return
   */
  public PartStat toICal4JParticipationStatusParameter() {
    switch (this) {
      case PARTICIPATE:
        return PartStat.ACCEPTED;
      case PARTICIPATE_POSSIBLY:
        return PartStat.TENTATIVE;
      case DOES_NOT_PARTICIPATE:
        return PartStat.DECLINED;
      default:
        return PartStat.NEEDS_ACTION;
    }
  }
}
