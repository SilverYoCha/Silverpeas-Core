/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silverstatistics.access.dao;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.silverstatistics.access.model.StatisticRuntimeException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryObjectDAO {

  private static final String HISTORY_TABLE_NAME = "SB_Statistic_History";

  private static final String QUERY_STATISTIC_INSERT = "INSERT INTO SB_Statistic_History " +
      "(dateStat, heureStat, userId, resourceId, componentId, actionType, resourceType) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?)";

  private static final String QUERY_STATISTIC_DELETE_BY_RESOURCE =
      "DELETE FROM SB_Statistic_History WHERE resourceId = ? AND componentId = ? AND resourceType" +
          " = ?";

  private static final String QUERY_STATISTIC_DELETE_BY_COMPONENT =
      "DELETE FROM SB_Statistic_History WHERE componentId = ?";

  private static final String QUERY_STATISTIC_COUNT =
      "SELECT COUNT(resourceId) FROM SB_Statistic_History WHERE resourceId=? AND ComponentId =? " +
          "AND resourceType = ?";

  private static final String QUERY_STATISTIC_COUNT_BY_PERIOD =
      "SELECT COUNT(resourceId) FROM SB_Statistic_History WHERE resourceId=? AND ComponentId =? " +
          "AND resourceType = ? AND datestat >= ? AND datestat <= ?";

  private static final String QUERY_STATISTIC_COUNT_BY_PERIOD_AND_USER =
      "SELECT COUNT(resourceId) FROM SB_Statistic_History WHERE resourceId=? AND ComponentId =? " +
          "AND resourceType = ? AND datestat >= ? AND datestat <= ? AND userid = ?";

  private HistoryObjectDAO() {
  }

  /**
   * @param rs
   * @param componentName
   * @return
   * @throws SQLException
   */
  private static Collection<HistoryObjectDetail> getHistoryDetails(ResultSet rs,
      String componentName) throws SQLException {
    List<HistoryObjectDetail> list = new ArrayList<>();
    Date date;
    String userId;
    String foreignId;

    while (rs.next()) {
      try {
        // First the date of the day is parsed
        date = DateUtil.parse(rs.getString(1));
        // Then the hour is set
        date = DateUtil.getDate(date, rs.getString(2));
      } catch (java.text.ParseException e) {
        throw new StatisticRuntimeException(e);
      }
      userId = rs.getString(3);
      foreignId = rs.getString(4);
      ResourceReference resourceReference = new ResourceReference(foreignId, componentName);
      HistoryObjectDetail detail = new HistoryObjectDetail(date, userId, resourceReference);

      list.add(detail);
    }
    return list;
  }

  /**
   * @param con the database connection
   * @param userId the user identifier
   * @param resourceReference
   * @param actionType
   * @param objectType
   * @throws SQLException
   */
  public static void add(Connection con, String userId, ResourceReference resourceReference, int actionType,
      String objectType) throws SQLException {

    PreparedStatement prepStmt = null;

    try {
      Date now = new Date();
      prepStmt = con.prepareStatement(QUERY_STATISTIC_INSERT);
      prepStmt.setString(1, DateUtil.date2SQLDate(now));
      prepStmt.setString(2, DateUtil.formatTime(now));
      prepStmt.setString(3, userId);
      prepStmt.setString(4, resourceReference.getId());
      prepStmt.setString(5, resourceReference.getInstanceId());
      prepStmt.setInt(6, actionType);
      prepStmt.setString(7, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param con the database connection
   * @param resourceReference
   * @param objectType
   * @return
   * @throws SQLException
   */
  public static Collection<HistoryObjectDetail> getHistoryDetailByObject(Connection con,
      ResourceReference resourceReference, String objectType) throws SQLException {

    String componentName = resourceReference.getComponentName();
    String selectStatement =
        "select dateStat, heureStat, userId, resourceId, componentId, actionType, resourceType " +
        "from " + HISTORY_TABLE_NAME
        + " where resourceId=? and componentId=? and resourceType=?"
        + " order by datestat desc, heurestat desc";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(selectStatement);
      stmt.setString(1, resourceReference.getId());
      stmt.setString(2, resourceReference.getInstanceId());
      stmt.setString(3, objectType);

      rs = stmt.executeQuery();
      return getHistoryDetails(rs, componentName);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static Collection<HistoryObjectDetail> getHistoryDetailByObjectAndUser(Connection con,
      ResourceReference resourceReference, String objectType, String userId) throws SQLException {

    String componentName = resourceReference.getComponentName();
    String selectStatement =
        "select * from " + HISTORY_TABLE_NAME + " where resourceId='" + resourceReference.getId() +
            "' and componentId='" + resourceReference.getInstanceId() + "'" + " and resourceType='" +
            objectType + "'" + " and userId ='" + userId + "'" +
            " order by dateStat desc, heureStat desc";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      return getHistoryDetails(rs, componentName);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * @param con the database connection
   * @param resourceReference
   * @param objectType
   * @throws SQLException
   */
  public static void deleteHistoryByObject(Connection con, ResourceReference resourceReference, String objectType)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_DELETE_BY_RESOURCE);
      prepStmt.setString(1, resourceReference.getId());
      prepStmt.setString(2, resourceReference.getInstanceId());
      prepStmt.setString(3, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteStatsOfComponent(Connection con, String componentId)
      throws SQLException {
    SilverTrace
        .info("statistic", "HistoryObjectDAO.deleteStatsOfComponent", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_DELETE_BY_COMPONENT);
      prepStmt.setString(1, componentId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static int getCount(Connection con, Collection<ResourceReference> resourceReferences, String objectType)
      throws SQLException {
    int nb = 0;
    for (ResourceReference pk : resourceReferences) {
      nb = nb + getCount(con, pk, objectType);
    }
    return nb;
  }

  public static int getCount(Connection con, ResourceReference resourceReference, String objectType)
      throws SQLException {
    int nb = 0;

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_COUNT);
      prepStmt.setString(1, resourceReference.getId());
      prepStmt.setString(2, resourceReference.getInstanceId());
      prepStmt.setString(3, objectType);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nb = rs.getInt(1);
      }
      return nb;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static int getCountByPeriod(Connection con, WAPrimaryKey primaryKey, String objectType,
      Date startDate, Date endDate) throws SQLException {
    int nb = 0;

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_COUNT_BY_PERIOD);
      prepStmt.setString(1, primaryKey.getId());
      prepStmt.setString(2, primaryKey.getInstanceId());
      prepStmt.setString(3, objectType);
      prepStmt.setString(4, DateUtil.date2SQLDate(startDate));
      prepStmt.setString(5, DateUtil.date2SQLDate(endDate));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nb = rs.getInt(1);
      }
      return nb;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static int getCountByPeriodAndUser(Connection con, WAPrimaryKey primaryKey,
      String objectType, Date startDate, Date endDate, String userId) throws SQLException {
    int nb = 0;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_COUNT_BY_PERIOD_AND_USER);
      prepStmt.setString(1, primaryKey.getId());
      prepStmt.setString(2, primaryKey.getInstanceId());
      prepStmt.setString(3, objectType);
      if (startDate != null) {
        prepStmt.setString(4, DateUtil.date2SQLDate(startDate));
      } else {
        prepStmt.setString(4, DateUtil.date2SQLDate(DateUtil.MINIMUM_DATE));
      }
      if (endDate != null) {
        prepStmt.setString(5, DateUtil.date2SQLDate(endDate));
      } else {
        prepStmt.setString(5, DateUtil.date2SQLDate(DateUtil.MAXIMUM_DATE));
      }
      prepStmt.setString(6, userId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nb = rs.getInt(1);
      }
      return nb;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static void move(Connection con, ResourceReference toResourceReference, int actionType, String objectType)
      throws SQLException {


    String insertStatement = "update " + HISTORY_TABLE_NAME +
        " set componentId = ? where resourceId = ? and actionType = ? and resourceType = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, toResourceReference.getInstanceId());
      prepStmt.setString(2, toResourceReference.getId());
      prepStmt.setInt(3, actionType);
      prepStmt.setString(4, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static List<String> getListObjectAccessByPeriod(Connection con,
      List<WAPrimaryKey> primaryKeys, String objectType, Date startDate, Date endDate)
      throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append(
        "SELECT resourceId FROM SB_Statistic_History WHERE ComponentId =? AND resourceType = ? " +
            "AND datestat >= ? AND datestat <= ? ");
    String instanceId = null;
    if (primaryKeys != null && !primaryKeys.isEmpty()) {
      query.append("AND resourceId IN (");
      for (WAPrimaryKey pk : primaryKeys) {
        if (primaryKeys.indexOf(pk) != 0) {
          query.append(",");
        }
        query.append("'").append(pk.getId()).append("'");
      }
      query.append(")");
      instanceId = primaryKeys.get(0).getInstanceId();
    }

    List<String> results = new ArrayList<>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query.toString());
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, objectType);
      prepStmt.setString(3, DateUtil.date2SQLDate(startDate));
      prepStmt.setString(4, DateUtil.date2SQLDate(endDate));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        results.add(rs.getString(1));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return results;
  }

  public static List<String> getListObjectAccessByPeriodAndUser(Connection con,
      List<WAPrimaryKey> primaryKeys, String objectType, Date startDate, Date endDate,
      String userId) throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append(
        "SELECT resourceId FROM SB_Statistic_History WHERE ComponentId =? AND resourceType = ? " +
            "AND datestat >= ? AND datestat <= ? ");
    String instanceId = null;
    if (CollectionUtil.isNotEmpty(primaryKeys)) {
      query.append("AND resourceId IN (");
      for (WAPrimaryKey pk : primaryKeys) {
        if (primaryKeys.indexOf(pk) != 0) {
          query.append(",");
        }
        query.append("'").append(pk.getId()).append("'");
      }
      query.append(")");
      instanceId = primaryKeys.get(0).getInstanceId();
    }
    if (StringUtil.isDefined(userId)) {
      query.append(" AND userId = ?");
    }

    List<String> results = new ArrayList<>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query.toString());
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, objectType);
      prepStmt.setString(3, DateUtil.date2SQLDate(startDate));
      prepStmt.setString(4, DateUtil.date2SQLDate(endDate));
      prepStmt.setString(5, userId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        results.add(rs.getString(1));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return results;
  }

  /**
   * Gets the last history detail of each object associated to a user. The result is sorted on the
   * datetime from the youngest to the oldest
   * @param con
   * @param userId
   * @param actionType
   * @param objectType
   * @param nbObjects
   * @return
   * @throws SQLException
   */
  public static Collection<HistoryObjectDetail> getLastHistoryDetailOfObjectsForUser(Connection con,
      String userId, int actionType, String objectType, int nbObjects) throws SQLException {


    String selectStatement =
        "select componentId, resourceId, datestat, heurestat" + " from SB_Statistic_History" +
            " where userId='" + userId + "'" + " and actionType=" + actionType +
            " and resourceType='" + objectType + "'" + " order by datestat desc, heurestat desc";

    Statement stmt = null;
    ResultSet rs = null;
    List<HistoryObjectDetail> result = new ArrayList<>();
    Set<ResourceReference> performedIds = new HashSet<>(nbObjects * 2);
    Date date;

    try {
      stmt = con.createStatement();
      // Setting a cursor to avoid performance problems
      stmt.setFetchSize(50);
      rs = stmt.executeQuery(selectStatement);

      while (rs.next() && performedIds.size() < nbObjects) {

        // Id
        String componentId = rs.getString(1);
        String foreignId = rs.getString(2);
        ResourceReference resourceReference = new ResourceReference(foreignId, componentId);

        // If id is already performed, then it is skiped
        if (performedIds.add(resourceReference)) {
          try {
            // First the date of the day is parsed
            date = DateUtil.parse(rs.getString(3));
            // Then the hour is set
            date = DateUtil.getDate(date, rs.getString(4));
          } catch (java.text.ParseException e) {
            throw new StatisticRuntimeException(e);
          }
          result.add(new HistoryObjectDetail(date, userId, resourceReference));
        }
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return result;
  }
}
