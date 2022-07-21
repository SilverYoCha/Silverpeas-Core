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
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.test.WarBuilder4LibCore;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author silveryocha
 */
@RunWith(Arquillian.class)
public class UserSynchroProcessAnnotationInterceptorIT {

  @Inject
  private TestService service;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SynchroAvatarThreadManagerIT.class)
        .addFileRepositoryFeatures()
        .testFocusedOn((warBuilder) -> warBuilder.addPackages(false, "org.silverpeas.core.admin.domain.synchro.annotation"))
        .build();
  }

  @Test
  public void testNoInterception() {
    final TestAvatarProcess process = new TestAvatarProcess();
    service.verifyNoInterceptor(process);
    assertThat(process.isProcessed(), is(false));
  }

  @Test
  public void testInterception() {
    final TestAvatarProcess process = new TestAvatarProcess();
    service.verifyInterceptor(process);
    assertThat(process.isProcessed(), is(true));
    testNoInterception();
  }

  @Service
  public static class TestService {

    @Inject
    private SynchroAvatarThreadManager avatarManager;

    public void verifyNoInterceptor(TestAvatarProcess process) {
      assertThat(avatarManager.isCollecting(), is(false));
      register(process);
      assertThat(process.isProcessed(), is(false));
    }

    @UserSynchroProcess
    public void verifyInterceptor(TestAvatarProcess process) {
      assertThat(avatarManager.isCollecting(), is(true));
      register(process);
      assertThat(process.isProcessed(), is(false));
    }

    private void register(final TestAvatarProcess process) {
      if (avatarManager.isCollecting()) {
        avatarManager.addAvatarProcess(process);
      }
    }
  }

  public static class TestAvatarProcess extends SynchroAvatarThreadManager.AvatarProcess {

    private boolean processed = false;

    @Override
    public String getUniqueId() {
      return "1";
    }

    @Override
    public void execute() {
      processed = true;
    }

    public boolean isProcessed() {
      return processed;
    }
  }
}