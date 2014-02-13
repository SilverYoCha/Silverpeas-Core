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
package org.silverpeas.organizer.event.service;

import com.stratelia.webactiv.util.DateUtil;
import org.silverpeas.date.Period;
import org.silverpeas.date.PeriodType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Criterion container to improve the event search mechanism.
 * <p/>
 * User: Yohann Chastagnier
 * Date: 02/12/13
 */
public class EventSearchCriteria {

  // By default, a month period guessed from the current date of the day.
  private Period period = Period.from(DateUtil.getNow(), PeriodType.month);
  private Set<String> instanceIds = new HashSet<String>();
  private Set<String> targetResourceUniqueIds = new HashSet<String>();
  private Set<String> userIds = new HashSet<String>();
  private Set<String> groupIds = new HashSet<String>();


  public void reset() {
    reset(null);
  }

  public void reset(Period defaultPeriod) {
    period =
        (defaultPeriod == null) ? Period.from(DateUtil.getNow(), PeriodType.month) : defaultPeriod;
    instanceIds.clear();
    targetResourceUniqueIds.clear();
    userIds.clear();
    groupIds.clear();
  }

  /**
   * Sets the period aimed by the search
   * @param period
   * @return
   */
  public EventSearchCriteria setPeriod(Period period) {
    this.period = period;
    return this;
  }

  /**
   * Gets the period aimed by the search.
   * @return
   */
  public Period getPeriod() {
    return period;
  }

  /**
   * Setting the component instance ids aimed by the search.
   * @param componentInstanceIds
   * @return
   */
  public EventSearchCriteria andComponentInstanceId(String... componentInstanceIds) {
    Collections.addAll(instanceIds, componentInstanceIds);
    return this;
  }

  /**
   * Gets the unique list of instance id
   * @return
   */
  public Set<String> getInstanceIds() {
    return instanceIds;
  }

  /**
   * Setting the target ids aimed by the search.
   * @param targetResourceUniqueIds
   * @return
   */
  public EventSearchCriteria andTargetResourceUniqueId(String... targetResourceUniqueIds) {
    Collections.addAll(this.targetResourceUniqueIds, targetResourceUniqueIds);
    return this;
  }

  /**
   * Gets the unique list of target id.
   * @return
   */
  public Set<String> getTargetResourceUniqueIds() {
    return targetResourceUniqueIds;
  }

  /**
   * Setting the user ids aimed by the search.
   * An operator OR is performed with {@link #onGroupId(String...)}.
   * @param userIds
   * @return
   */
  public EventSearchCriteria onUserId(String... userIds) {
    Collections.addAll(this.userIds, userIds);
    return this;
  }

  /**
   * Gets the unique list of user id.
   * @return
   */
  public Set<String> getUserIds() {
    return userIds;
  }

  /**
   * Setting the group ids aimed by the search.
   * An operator OR is performed with {@link #onUserId(String...)}.
   * @param groupIds
   * @return
   */
  public EventSearchCriteria onGroupId(String... groupIds) {
    Collections.addAll(this.groupIds, groupIds);
    return this;
  }

  /**
   * Gets the unique list of group id.
   * @return
   */
  public Set<String> getGroupIds() {
    return groupIds;
  }
}
