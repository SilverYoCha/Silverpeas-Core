/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.test.thread;

import org.awaitility.core.DurationFactory;
import org.silverpeas.kernel.SilverpeasException;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

/**
 * This thread test class allows to centralize some boilerplate code in order to get lighter code
 * in functional tests.
 * @author silveryocha
 */
public class ThreadTest implements Runnable {

  private final ThreadProcess process; //NOSONAR

  public ThreadTest(final ThreadProcess<? extends ThreadTest> process) {
    this.process = process;
  }

  @Override
  public void run() {
    try {
      executeProcess();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  public Thread start() {
    final Thread thread = new Thread(this);
    thread.start();
    return thread;
  }

  @SuppressWarnings("unchecked")
  private void executeProcess() throws SilverpeasException, InterruptedException {
    process.execute(this);
  }

  @FunctionalInterface
  public interface ThreadProcess<I extends ThreadTest> {
    void execute(I instance) throws SilverpeasException, InterruptedException;
  }

  public static void waitForFewMilliseconds() {
    waitFor(100, MILLISECONDS);
  }

  public static void waitFor(long delay, TimeUnit unit) {
    await().pollDelay(delay, unit).until(() -> true);
  }

  public static void join(Thread... threads) {
    join(10, SECONDS, threads);
  }

  public static void join(long timeout, TimeUnit unit, Thread... threads) {
    Stream.of(threads).forEach(thread -> {
      try {
        thread.join(DurationFactory.of(timeout, unit).toMillis());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      if (!thread.isInterrupted()) {
        thread.interrupt();
      }
    });
  }
}
