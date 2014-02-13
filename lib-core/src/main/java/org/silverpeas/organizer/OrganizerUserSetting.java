/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.organizer;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.organizer.event.util.EventUtil;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.awt.*;

/**
 * User: Yohann Chastagnier
 * Date: 27/11/13
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class OrganizerUserSetting<ENTITY extends OrganizerUserSetting<ENTITY>>
    extends AbstractJpaEntity<ENTITY, UuidIdentifier> implements OrganizerSettable {

  @Column(name = "rgbColor", columnDefinition = "VARCHAR(16)")
  private String rgbColor;

  @Transient
  private UserDetail user;

  @Transient
  private Color color;

  public UserDetail getUser() {
    if (user == null) {
      user = getCreator();
    }
    return user;
  }

  public void setUser(final UserDetail user) {
    this.user = user;
    setCreator(user);
  }

  public Color getRgbColor() {
    if (color == null) {
      color = EventUtil.rgbStringToColor(rgbColor);
    }
    return color;
  }

  public void setRgbColor(final Color color) {
    this.color = color;
    this.rgbColor = EventUtil.colorToRGBString(color);
  }
}
