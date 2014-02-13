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
package org.silverpeas.organizer.event.util;

import com.silverpeas.util.StringUtil;

import java.awt.*;

/**
 * User: Yohann Chastagnier
 * Date: 11/12/13
 */
public class EventUtil {

  /**
   * Gets the RGB string code composed by integer values of red, green and blue each one separated
   * by comma from a {@link Color} object.
   * @param color
   * @return the color as a simple RGB string.
   */
  public static String colorToRGBString(Color color) {
    if (color == null) {
      return null;
    }
    return String.format("%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * Gets the {@link Color} object from a RGB string composed by integer values of red, green and
   * blue each one separated by comma.
   * @param rgbColor
   * @return the {@link Color} object.
   */
  public static Color rgbStringToColor(String rgbColor) {
    Color color = null;
    if (StringUtil.isDefined(rgbColor)) {
      String rgb[] = rgbColor.split(",");
      if (rgb.length == 3) {
        color =
            new Color(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
      }
    }
    return color;
  }
}
