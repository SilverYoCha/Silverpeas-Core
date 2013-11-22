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
package org.silverpeas.event.service;

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.util.comparator.AbstractComparator;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.event.constant.EventParticipationStatus;
import org.silverpeas.event.constant.EventInfoType;
import org.silverpeas.event.constant.EventPriority;
import org.silverpeas.event.constant.EventPrivacyType;
import org.silverpeas.event.constant.EventType;
import org.silverpeas.event.model.Event;
import org.silverpeas.event.model.EventAttendee;
import org.silverpeas.event.model.EventInfo;
import org.silverpeas.event.service.mock.OrganizationControllerMock;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.awt.*;
import java.util.Collections;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * User: Yohann Chastagnier
 * Date: 11/12/13
 */
public class EventServiceTest {

  private OrganizationControllerMock organizationControllerMock;
  private EventService eventService;

  private ClassPathXmlApplicationContext context;

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @AfterClass
  public static void tearDownClass() {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  @Before
  public void setUp() throws Exception {
    context = new ClassPathXmlApplicationContext("spring-event.xml",
        "spring-event-embedded-datasource.xml");

    // Beans
    final DataSource dataSource = (DataSource) context.getBean("jpaDataSource");
    eventService = context.getBean(EventService.class);
    organizationControllerMock =
        context.getBean("organizationController", OrganizationControllerMock.class);

    // Database
    InitialContext ic = new InitialContext();
    ic.bind(JNDINames.SUBSCRIBE_DATASOURCE, dataSource);
    DatabaseOperation.INSERT
        .execute(new DatabaseConnection(dataSource.getConnection()), getDataSet());
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @After
  public void tearDown() throws Exception {
    DBUtil.clearTestInstance();
    InitialContext ic = new InitialContext();
    ic.unbind(JNDINames.SUBSCRIBE_DATASOURCE);
    context.close();
  }

  protected IDataSet getDataSet() throws DataSetException {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        this.getClass().getClassLoader()
            .getResourceAsStream("org/silverpeas/event/service/event-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  /**
   * The not commonly suffix method name represent the number of unit test case.
   * @see org.silverpeas.event.service.EventServiceTest.TestCase
   */
  @Test
  public void testGetById_00() {
    setupOrganizationControllerUserIdAlwaysExists();

    // No event
    Event event = eventService.getEventById("eventIdThatDoesntExist");
    assertThat(event, nullValue());

    // PERSONAL_USER_SPACE_00
    event = eventService.getEventById(TestCase.PERSONAL_USER_SPACE_00.getEventId());
    assertThat(event, notNullValue());
    assertThat(event.getCalendar(), notNullValue());
    assertThat(event.getCalendar().getId(), is("personal_space_users"));
    assertThat(event.getCalendar().getInstanceId(), nullValue());
    assertThat(event.getCalendar().getTargetResourceUniqueId(), nullValue());
    assertThat(event.getCalendar().getRgbColor(), nullValue());
    assertThat(event.getPlanning(), notNullValue());
    assertThat(event.getPlanning().getId(), is(TestCase.PERSONAL_USER_SPACE_00.getPlanningId()));
    assertThat(event.getType(), is(EventType.INDIVIDUAL));
    assertThat(event.getPrivacyType(), is(EventPrivacyType.PRIVATE));
    assertThat(event.getPriority(), is(EventPriority.NONE));
    assertThat(event.getTitle(), is("Personal Event - 00"));
    assertThat(event.getDuration(), is(9000000L));
    assertThat(event.getRgbColor(), nullValue());
    assertThat(event.getPlanning().getEvent(), is(event));
    assertThat(event.getPlanning().getRecurrence(), nullValue());
    assertThat(event.getPlanning().getPeriod(), notNullValue());
    assertThat(
        event.getPlanning().getPeriod().getBeginDate().compareTo(date("2013-12-15 11:00:00.0")),
        is(0));
    assertThat(
        event.getPlanning().getPeriod().getEndDate().compareTo(date("2013-12-15 13:30:00.0")),
        is(0));
    assertThat(event.getInfos(), empty());
    assertThat(event.getParticipants(), empty());
    assertThat(event.getReminds(), empty());
    assertThat(event.getOrganizer(), notNullValue());
    assertThat(event.getOrganizer().getId(), is("1"));
  }

  /**
   * The not commonly suffix method name represent the number of unit test case.
   * @see org.silverpeas.event.service.EventServiceTest.TestCase
   */
  @Test
  public void testGetById_01() {
    setupOrganizationControllerUserIdAlwaysExists();

    // No event
    Event event = eventService.getEventById("eventIdThatDoesntExist");
    assertThat(event, nullValue());

    // PERSONAL_USER_SPACE_01
    event = eventService.getEventById(TestCase.PERSONAL_USER_SPACE_01.getEventId());
    assertThat(event, notNullValue());
    assertThat(event.getCalendar(), notNullValue());
    assertThat(event.getCalendar().getId(), is("personal_space_users"));
    assertThat(event.getCalendar().getInstanceId(), nullValue());
    assertThat(event.getCalendar().getTargetResourceUniqueId(), nullValue());
    assertThat(event.getCalendar().getRgbColor(), nullValue());
    assertThat(event.getPlanning(), notNullValue());
    assertThat(event.getPlanning().getId(), is(TestCase.PERSONAL_USER_SPACE_01.getPlanningId()));
    assertThat(event.getType(), is(EventType.INDIVIDUAL));
    assertThat(event.getPrivacyType(), is(EventPrivacyType.PUBLIC));
    assertThat(event.getPriority(), is(EventPriority.HIGH));
    assertThat(event.getTitle(), is("Personal Event - 01"));
    assertThat(event.getDuration(), is(101700000L));
    assertThat(event.getRgbColor(), is(Color.BLACK));
    assertThat(event.getPlanning().getEvent(), is(event));
    assertThat(event.getPlanning().getRecurrence(), nullValue());
    assertThat(event.getPlanning().getPeriod(), notNullValue());
    assertThat(
        event.getPlanning().getPeriod().getBeginDate().compareTo(date("2013-12-20 08:00:00.0")),
        is(0));
    assertThat(
        event.getPlanning().getPeriod().getEndDate().compareTo(date("2013-12-21 12:15:00.000")),
        is(0));
    assertThat(event.getInfos(), empty());
    assertThat(event.getParticipants(), empty());
    assertThat(event.getReminds(), empty());
    assertThat(event.getOrganizer(), notNullValue());
    assertThat(event.getOrganizer().getId(), is("2"));
  }

  /**
   * The not commonly suffix method name represent the number of unit test case.
   * @see org.silverpeas.event.service.EventServiceTest.TestCase
   */
  @Test
  public void testGetById_02() {
    setupOrganizationControllerUserIdAlwaysExists();

    // No event
    Event event = eventService.getEventById("eventIdThatDoesntExist");
    assertThat(event, nullValue());

    // RESOURCE_GLOBAL_02
    event = eventService.getEventById(TestCase.RESOURCE_GLOBAL_02.getEventId());
    assertThat(event, notNullValue());
    assertThat(event.getCalendar(), notNullValue());
    assertThat(event.getCalendar().getId(), is(TestCase.RESOURCE_GLOBAL_02.getTargetId()));
    assertThat(event.getCalendar().getInstanceId(), nullValue());
    assertThat(event.getCalendar().getTargetResourceUniqueId(),
        is(TestCase.RESOURCE_GLOBAL_02.getTargetId() + "_uniqueId"));
    assertThat(event.getCalendar().getRgbColor(), is(Color.GREEN));
    assertThat(event.getPlanning(), notNullValue());
    assertThat(event.getPlanning().getId(), is(TestCase.RESOURCE_GLOBAL_02.getPlanningId()));
    assertThat(event.getType(), is(EventType.RESOURCE));
    assertThat(event.getPrivacyType(), is(EventPrivacyType.ATTENDEE));
    assertThat(event.getPriority(), is(EventPriority.LOW));
    assertThat(event.getTitle(), is("Global Resource Event - 02"));
    assertThat(event.getDuration(), is(3000000L));
    assertThat(event.getRgbColor(), is(Color.RED));
    assertThat(event.getPlanning().getEvent(), is(event));
    assertThat(event.getPlanning().getRecurrence(), nullValue());
    assertThat(event.getPlanning().getPeriod(), notNullValue());
    assertThat(
        event.getPlanning().getPeriod().getBeginDate().compareTo(date("2013-12-26 23:30:00.0")),
        is(0));
    assertThat(
        event.getPlanning().getPeriod().getEndDate().compareTo(date("2013-12-27 0:20:00.000")),
        is(0));
    assertThat(event.getInfos(), hasSize(3));
    Collections.sort(event.getInfos(), new AbstractComparator<EventInfo>() {
      @Override
      public int compare(final EventInfo o1, final EventInfo o2) {
        return o1.getType().compareTo(o2.getType());
      }
    });
    assertThat(event.getInfos().get(0).getType(), is(EventInfoType.UNKNOWN));
    assertThat(event.getInfos().get(0).getContent(), is("Unknown info"));
    assertThat(event.getInfos().get(1).getType(), is(EventInfoType.DESCRIPTION));
    assertThat(event.getInfos().get(1).getContent(), is("Description info"));
    assertThat(event.getInfos().get(2).getType(), is(EventInfoType.TARGET_URL_ACCESS));
    assertThat(event.getInfos().get(2).getContent(), is("Target URL access info"));
    assertThat(event.getParticipants(), hasSize(2));
    Collections.sort(event.getParticipants(), new AbstractComparator<EventAttendee>() {
      @Override
      public int compare(final EventAttendee o1, final EventAttendee o2) {
        return o1.getCreatedBy().compareTo(o2.getCreatedBy());
      }
    });
    assertThat(event.getParticipants().get(0).getUser().getId(), is("26"));
    assertThat(event.getParticipants().get(0).getParticipationStatus(),
        is(EventParticipationStatus.NOT_YET_ANSWERED));
    assertThat(event.getParticipants().get(1).getUser().getId(), is("38"));
    assertThat(event.getParticipants().get(1).getParticipationStatus(), is(EventParticipationStatus.PARTICIPATE));
    assertThat(event.getReminds(), hasSize(1));
    assertThat(event.getOrganizer(), nullValue());
  }

  /**
   * Gets an {@link java.util.Date} instance from the given string.
   * @param date {@link java.sql.Date#valueOf(String)}.
   * @return a date instance.
   */
  private static Date date(String date) {
    return java.sql.Timestamp.valueOf(date);
  }

  /**
   * At test begin, call this method to always have a {@link UserDetail} instance behind a userId.
   */
  private void setupOrganizationControllerUserIdAlwaysExists() {
    when(organizationControllerMock.getMock().getUserDetail(anyString()))
        .thenAnswer(new Answer<UserDetail>() {
          @Override
          public UserDetail answer(final InvocationOnMock invocationOnMock) throws Throwable {
            UserDetail user = new UserDetail();
            user.setId((String) invocationOnMock.getArguments()[0]);
            return user;
          }
        });
  }

  /**
   * This Enum defines all test cases and provides some little tools around them.
   */
  private enum TestCase {

    /**
     * Event "Personal Event - 00" :
     * - personal user space
     * - on 15/12/2013 from 11AM to 1h30PM
     */
    PERSONAL_USER_SPACE_00,

    /**
     * Event "Personal Event - 01" :
     * - personal user space
     * - from 20/12/2013 8AM to 21/12/2013 12h15PM
     * - with specific color on event : BLACK
     * - with high prority : HIGH
     */
    PERSONAL_USER_SPACE_01,

    /**
     * Event "Resource Event - 02" :
     * - On a resource (but no component instance id)
     * - from 26/12/2013 23h30PM to 27/12/2013 0h20AM
     * - with specific color on target : GREEN
     * - with specific color on event : RED
     * - with high prority : LOW
     * - with privacy : PARTICIPANT
     * - with description info : "Description info"
     * - with target url access info : "Target URL access info"
     * - with unknown info : "Unknown info"
     * - with remind of 15 minutes
     * - with 2 participants :
     * ... userId 26
     * ... > status = NOT_YET_ANSWERED
     * ... userId 38
     * ... > status = PARTICIPATE
     */
    RESOURCE_GLOBAL_02;

    public String getTargetId() {
      return name().toLowerCase();
    }

    public String getEventId() {
      return name().toLowerCase() + "_event";
    }

    public String getPlanningId() {
      return name().toLowerCase() + "_planning";
    }
  }
}
