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
package org.silverpeas.organizer.event;

/**
 * A factory of ScheduleManager instances. It manages the life-cycle of the managers and
 * abstracts the concrete implementation used to manage schedules.
 */
public class ScheduleManagerFactory {

  private static final ScheduleManagerFactory instance = new ScheduleManagerFactory();

  private final ScheduleEventManager generator = new ICal4jScheduleEventManager();

  /**
   * Gets a factory of scedule manager.
   * The returned factory abstracts the concrete implementation of the ScheduleManager interface.
   * @return an ScheduleManagerFactory instance.
   */
  private static ScheduleManagerFactory getFactory() {
    return instance;
  }

  /**
   * Gets a manager of schedules.
   * @return an instance of the ScheduleManager interface.
   */
  public static ScheduleEventManager getScheduleEventManager() {
    return getFactory().generator;
  }
}
