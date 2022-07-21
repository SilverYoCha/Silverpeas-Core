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

package org.silverpeas.core.admin.domain.driver;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.admin.domain.DomainType;
import org.silverpeas.core.admin.domain.synchro.annotation.SynchroAvatarThreadManager;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.io.media.image.ImageTool;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.silverpeas.core.util.ImageUtil.JPEG_IMAGE_EXTENSION;
import static org.silverpeas.core.util.ImageUtil.JPG_IMAGE_EXTENSION;
import static org.silverpeas.core.util.file.FileRepositoryManager.getAvatarPath;
import static org.silverpeas.core.util.file.FileRepositoryManager.getTemporaryPath;

/**
 * Centralization of the code handling the avatar copy during domain data synchronization.
 * @author silveryocha
 */
public abstract class AbstractAvatarSynchroProcess extends SynchroAvatarThreadManager.AvatarProcess {

  private final String uniqueId;
  private final UserDetail spUser;

  /**
   * Unique constructor.
   * @param domainId the domain id supplier.
   * @param domainType the type of domain implementing the process.
   * @param userSpecificId supplier of the unique id into context of identity manager.
   * @param spUser instance of {@link UserDetail} enough loaded to read avatar file path data.
   */
  protected AbstractAvatarSynchroProcess(final IntSupplier domainId,
      final DomainType domainType, Supplier<String> userSpecificId, final UserDetail spUser) {
    this.uniqueId = String.format("sync-%s-avatar-%s-%s", domainType.name().toLowerCase(),
        domainId.getAsInt(), FilenameUtils.normalize(userSpecificId.get().replace("\\", "")));
    this.spUser = spUser;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  @Override
  public void execute() {
    final Path tmpPhoto = Paths.get(getTemporaryPath(), getUniqueId());
    final Path tmpJpgAvatar = Paths.get(tmpPhoto + "." + JPG_IMAGE_EXTENSION);
    try {
      loadPhotoIntoPath(tmpPhoto);
      final File avatarFile = new File(getAvatarPath(), spUser.getAvatarFileName());
      final String mimeType = FileUtil.getMimeType(tmpPhoto.toString())
          .replace(JPEG_IMAGE_EXTENSION, JPG_IMAGE_EXTENSION);
      final File tmpJpgAvatarFile = tmpJpgAvatar.toFile();
      if (mimeType.endsWith(getExtension(avatarFile.toString()))) {
        FileUtil.moveFile(tmpPhoto.toFile(), tmpJpgAvatarFile);
      } else {
        ImageTool.get().convert(tmpPhoto.toFile(), tmpJpgAvatarFile);
      }
      if (!avatarFile.exists() || tmpJpgAvatarFile.length() != avatarFile.length()) {
        FileUtil.moveFile(tmpJpgAvatarFile, avatarFile);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    } finally {
      try {
        Files.deleteIfExists(tmpPhoto);
        Files.deleteIfExists(tmpJpgAvatar);
      } catch (IOException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  /**
   * Load the photo data of the user concerned by the current process into the path specified by
   * given parameter.
   * @param photoPath the path into which data MUST be loaded (copied in other terms).
   * @throws IOException on technical IO error.
   */
  protected abstract void loadPhotoIntoPath(final Path photoPath) throws IOException;

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
