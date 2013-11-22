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
package org.silverpeas.event;

import com.silverpeas.util.CollectionUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.tika.io.IOUtils;
import org.silverpeas.event.model.Event;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * A generator of event occurrences built on the iCal4J library.
 */
public abstract class AbstractScheduleEventManager implements ScheduleEventManager {

  @Override
  public final File generateICalendarFile(final Event... events) {
    return generateICalendarFile(CollectionUtil.asList(events));
  }

  @Override
  public final File generateICalendarFile(final List<Event> events) {
    long milliTime = System.currentTimeMillis();
    long nanoTime = System.nanoTime();
    File calendarFile = FileUtils.getFile(FileRepositoryManager.getTemporaryPath(),
        "Calendar_" + FastDateFormat.getInstance("yyyyMMdd-HHmm").format(milliTime) + "_" +
            nanoTime + ".ics");
    FileOutputStream calendarOutputStream = null;
    try {
      // Creating the file and opening its stream buffer
      calendarOutputStream = FileUtils.openOutputStream(calendarFile);
      // Write the ICS content
      generateICalendarFile(events, calendarOutputStream);
    } catch (IOException e) {
      calendarFile = null;
      SilverTrace.error("Event", "AbstractScheduleEventManager.generateICalendarFile",
          "EX_IMPOSSIBLE_TO_CREATE_ICS_FILE", e);
    } finally {
      // Closing the calendar stream buffer.
      if (calendarOutputStream != null) {
        IOUtils.closeQuietly(calendarOutputStream);
      }
    }
    return calendarFile;
  }

  /**
   * Generates the ICS calendar file in the tempory repository of Silverpeas Server.
   * @param events events to put in the ICS calendar file.
   */
  protected abstract void generateICalendarFile(final List<Event> events,
      final OutputStream calendarOutputStream) throws IOException;
}
