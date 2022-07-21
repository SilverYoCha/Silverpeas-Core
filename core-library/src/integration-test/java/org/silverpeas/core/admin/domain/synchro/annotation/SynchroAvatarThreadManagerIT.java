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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.thread.ThreadTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.test.thread.ThreadTest.*;

/**
 * @author silveryocha
 */
@RunWith(Arquillian.class)
public class SynchroAvatarThreadManagerIT {

  private static final String IS_COLLECTING_KEY = "isCollecting";

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SynchroAvatarThreadManagerIT.class)
        .addFileRepositoryFeatures()
        .testFocusedOn((warBuilder) -> warBuilder.addClasses(SynchroAvatarThreadManager.class))
        .build();
  }

  @Test
  public void testThreadCacheIsolation() {
    final CountDownLatch step1 = new CountDownLatch(1);
    final CountDownLatch step2 = new CountDownLatch(1);
    final AvatarThreadTest process1 = new AvatarThreadTest(m -> {
      final SynchroAvatarThreadManager manager = m.getManager();
      manager.startAvatarProcessCollect();
      m.snapshots.put(IS_COLLECTING_KEY, manager.isCollecting());
      step1.countDown();
      step2.await();
    });
    final AvatarThreadTest process2 = new AvatarThreadTest(m -> {
      step1.await();
      m.snapshots.put(IS_COLLECTING_KEY, m.getManager().isCollecting());
      step2.countDown();
    });
    final Thread thread2 = process2.start();
    waitForFewMilliseconds();
    final Thread thread1 = process1.start();
    join(thread1, thread2);
    assertThat(process1.getManager(), sameInstance(process2.getManager()));
    assertThat(process1.snapshots.get(IS_COLLECTING_KEY), is(true));
    assertThat((Boolean) process2.snapshots.get(IS_COLLECTING_KEY), is(false));
    assertThat(process1.getManager().isCollecting(), is(false));
    assertThat(process2.getManager().isCollecting(), is(false));
  }

  @Test(expected = NullPointerException.class)
  public void testCallIsCollectingBeforeAddingProcess() {
    final SynchroAvatarThreadManager manager = SynchroAvatarThreadManager.get();
    assertThat(manager.isCollecting(), is(false));
    manager.addAvatarProcess(new TestAvatarProcess() {
      @Override
      protected void execute() {
      }
    });
  }

  @Test
  public void testOneCollectCall() {
    final SynchroAvatarThreadManager manager = SynchroAvatarThreadManager.get();
    final InstanceCtx ctx = new InstanceCtx(manager);
    assertThat(manager.isCollecting(), is(false));
    assertThat(ctx.getContext(), nullValue());
    manager.startAvatarProcessCollect();
    assertThat(manager.isCollecting(), is(true));
    final SynchroAvatarThreadManager.Context managerContext = ctx.getContext();
    assertThat(managerContext, notNullValue());
    assertThat(managerContext.processes, empty());
    assertThat(managerContext.count, is(1));
    final AtomicInteger avatarProcessCount = new AtomicInteger(0);
    final SynchroAvatarThreadManager.AvatarProcess avatarProcess = new TestAvatarProcess() {
      @Override
      protected void execute() {
        waitFor(1, TimeUnit.SECONDS);
        avatarProcessCount.incrementAndGet();
      }
    };
    manager.addAvatarProcess(avatarProcess);
    manager.addAvatarProcess(avatarProcess);
    assertThat(managerContext.processes, hasSize(1));
    assertThat(managerContext.count, is(1));
    assertThat(avatarProcessCount.get(), is(0));
    assertThat(manager.isCollecting(), is(true));
    manager.endAvatarProcessCollect();
    assertThat(manager.isCollecting(), is(false));
    assertThat(ctx.getContext(), nullValue());
    assertThat(avatarProcessCount.get(), is(1));
  }

  @Test
  public void testSeveralCollectCalls() {
    final SynchroAvatarThreadManager manager = SynchroAvatarThreadManager.get();
    final InstanceCtx ctx = new InstanceCtx(manager);
    assertThat(manager.isCollecting(), is(false));
    assertThat(ctx.getContext(), nullValue());
    manager.startAvatarProcessCollect();
    assertThat(manager.isCollecting(), is(true));
    final SynchroAvatarThreadManager.Context managerContext = ctx.getContext();
    assertThat(managerContext, notNullValue());
    assertThat(managerContext.processes, empty());
    assertThat(managerContext.count, is(1));
    final AtomicInteger avatarProcessCount = new AtomicInteger(0);
    final SynchroAvatarThreadManager.AvatarProcess avatarProcess = new TestAvatarProcess() {
      @Override
      protected void execute() {
        waitFor(1, TimeUnit.SECONDS);
        avatarProcessCount.incrementAndGet();
      }
    };
    manager.addAvatarProcess(avatarProcess);
    manager.addAvatarProcess(avatarProcess);
    manager.addAvatarProcess(avatarProcess);
    assertThat(managerContext.processes, hasSize(1));
    assertThat(managerContext.count, is(1));
    assertThat(avatarProcessCount.get(), is(0));
    assertThat(manager.isCollecting(), is(true));
    manager.startAvatarProcessCollect();
    manager.startAvatarProcessCollect();
    manager.addAvatarProcess(avatarProcess);
    manager.startAvatarProcessCollect();
    manager.startAvatarProcessCollect();
    assertThat(managerContext.processes, hasSize(1));
    assertThat(managerContext.count, is(5));
    assertThat(avatarProcessCount.get(), is(0));
    assertThat(manager.isCollecting(), is(true));
    manager.endAvatarProcessCollect();
    assertThat(managerContext.processes, hasSize(1));
    assertThat(managerContext.count, is(4));
    assertThat(avatarProcessCount.get(), is(0));
    assertThat(manager.isCollecting(), is(true));
    manager.endAvatarProcessCollect();
    manager.endAvatarProcessCollect();
    manager.endAvatarProcessCollect();
    assertThat(managerContext.processes, hasSize(1));
    assertThat(managerContext.count, is(1));
    assertThat(avatarProcessCount.get(), is(0));
    assertThat(manager.isCollecting(), is(true));
    manager.endAvatarProcessCollect();
    assertThat(manager.isCollecting(), is(false));
    assertThat(ctx.getContext(), nullValue());
    assertThat(avatarProcessCount.get(), is(1));
  }

  static abstract class TestAvatarProcess extends SynchroAvatarThreadManager.AvatarProcess {

    private final String uniqueId = UUID.randomUUID().toString();

    @Override
    protected String getUniqueId() {
      return uniqueId;
    }
  }

  static class AvatarThreadTest extends ThreadTest {

    private final Map<String, Object> snapshots = new HashMap<>();
    private SynchroAvatarThreadManager manager;

    AvatarThreadTest(final ThreadProcess<AvatarThreadTest> process) {
      super(process);
    }

    public SynchroAvatarThreadManager getManager() {
      if (manager == null) {
        manager = SynchroAvatarThreadManager.get();
      }
      return manager;
    }
  }

  private static class InstanceCtx {

    private final SynchroAvatarThreadManager rs;

    private InstanceCtx(final SynchroAvatarThreadManager rs) {
      this.rs = rs;
    }

    @SuppressWarnings("unchecked")
    private SynchroAvatarThreadManager.Context getContext() {
      try {
        final ThreadLocal<SynchroAvatarThreadManager.Context> cache =
            (ThreadLocal<SynchroAvatarThreadManager.Context>) readDeclaredField(
            rs, "cache", true);
        return cache.get();
      } catch (IllegalAccessException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
  }
}