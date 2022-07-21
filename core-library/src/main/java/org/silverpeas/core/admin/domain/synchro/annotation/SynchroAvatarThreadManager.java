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

package org.silverpeas.core.admin.domain.synchro.annotation;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

/**
 * This manager allows to handle the avatar registering of user accounts from external identity
 * managers during a synchronization operation.
 * <p>
 *   When a synchronization between identity manager and Silverpeas is detected, the admin driver
 *   MUST put as many avatar process as it exists avatar data to register.
 * </p>
 * <p>
 *   Each avatar process contains the full logic to copy avatar data to register it for
 *   Silverpeas avatar services.
 * </p>
 * @author silveryocha
 */
@Service
@Singleton
public class SynchroAvatarThreadManager {

  private final ThreadLocal<Context> cache = new ThreadLocal<>();
  private final SilverLogger logger;

  SynchroAvatarThreadManager() {
    // hidden constructor
    logger = SilverLogger.getLogger(this);
  }

  public static SynchroAvatarThreadManager get() {
    return ServiceProvider.getService(SynchroAvatarThreadManager.class);
  }

  /**
   * Indicates that the manager is currently collecting avatar data.
   */
  public boolean isCollecting() {
    return cache.get() != null;
  }

  /**
   * Adds an {@link AvatarProcess} to handle.
   * <p>
   *   This method MUST be called unless {@link #isCollecting()} method returns true.
   * </p>
   * <p>
   *   If the given avatar process has same {@link AvatarProcess#getUniqueId()} value than a
   *   registered one, then it is ignored.
   * </p>
   * @param process an {@link AvatarProcess} instance.
   */
  public void addAvatarProcess(final AvatarProcess process) {
    cache.get().processes.add(process);
  }

  /**
   * Indicates that avatar data MUST be collected.
   */
  void startAvatarProcessCollect() {
    if (isCollecting()) {
      cache.get().count++;
      logger.debug(() -> format("Avatar process collect already started (thread call n°{0})", cache.get().count));
    } else {
      cache.set(new Context());
      logger.debug(() -> "Starting avatar process collect");
    }
  }

  /**
   * Processes collected {@link AvatarProcess}.
   */
  void endAvatarProcessCollect() {
    if (isCollecting()) {
      final Context context = cache.get();
      if (context.count > 1) {
        context.count--;
        return;
      }
      try {
        final Set<AvatarProcess> processes = context.processes;
        if (processes.isEmpty()) {
          logger.debug(() -> "No avatar process collected");
        } else {
          logger.debug(() -> format("{0} {0,choice, 1#avatar process has been| 1<avatar processes have been} collected", processes.size()));
          executeAvatarProcesses(processes);
        }
      } finally {
        cache.remove();
      }
    }
  }

  private void executeAvatarProcesses(final Set<AvatarProcess> processes) {
    final List<Callable<AvatarProcess>> tasks = processes.stream().map(p -> (Callable<AvatarProcess>) () -> {
      p.execute();
      return p;
    }).collect(Collectors.toList());
    try {
      ManagedThreadPool.getPool().invoke(tasks).forEach(f -> {
        try {
          f.get();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (Exception e) {
          logger.error(e);
        }
      });
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Definition of an avatar process.
   * <p>
   *   This process MUST ensure the avatar data save into Silverpeas's avatar service context.
   * </p>
   * <p>
   *   All potential exception MUST be handled by the process.
   * </p>
   */
  public abstract static class AvatarProcess {

    /**
     * Gets the unique identifier which allows to identify the process from the others.
     * <p>
     *   For example, if during collecting a user is collected more than one times then several
     *   processes will be created. But only one of them MUST be processed.
     * </p>
     * @return an unique string.
     */
    protected abstract String getUniqueId();

    /**
     * Performing the avatar processing.
     * <p>
     *   Any kind of exception MUST be handled by {@link AvatarProcess} implementation.
     * </p>
     */
    protected abstract void execute();

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final AvatarProcess context = (AvatarProcess) o;
      return Objects.equals(getUniqueId(), context.getUniqueId());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getUniqueId());
    }
  }

  static class Context {
    final Set<AvatarProcess> processes = new HashSet<>();
    int count = 1;
  }
}
