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
package org.silverpeas.event.util;

import org.junit.Test;

import java.awt.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * User: Yohann Chastagnier
 * Date: 16/12/13
 */
public class EventUtilTest {

  @Test
  public void colorToRGBString() {
    assertThat(EventUtil.colorToRGBString(null), nullValue());
    assertThat(EventUtil.colorToRGBString(Color.BLACK), is("0,0,0"));
    assertThat(EventUtil.colorToRGBString(Color.WHITE), is("255,255,255"));
    assertThat(EventUtil.colorToRGBString(Color.RED), is("255,0,0"));
    assertThat(EventUtil.colorToRGBString(Color.GREEN), is("0,255,0"));
    assertThat(EventUtil.colorToRGBString(Color.BLUE), is("0,0,255"));
    assertThat(EventUtil.colorToRGBString(new Color(120, 236, 21)), is("120,236,21"));
  }

  @Test(expected = NumberFormatException.class)
  public void rgbStringToColorFormatException1() {
    EventUtil.rgbStringToColor(",0,0");
  }

  @Test(expected = NumberFormatException.class)
  public void rgbStringToColorFormatException2() {
    EventUtil.rgbStringToColor(" ,0,0");
  }

  @Test(expected = NumberFormatException.class)
  public void rgbStringToColorFormatException3() {
    EventUtil.rgbStringToColor("0,a,0");
  }

  @Test(expected = IllegalArgumentException.class)
  public void rgbStringToColorFormatException4() {
    EventUtil.rgbStringToColor("0,300,0");
  }

  @Test
  public void rgbStringToColor() {
    assertThat(EventUtil.rgbStringToColor(null), nullValue());
    assertThat(EventUtil.rgbStringToColor(""), nullValue());
    assertThat(EventUtil.rgbStringToColor("0,0,0"), is(Color.BLACK));
    assertThat(EventUtil.rgbStringToColor("255,255,255"), is(Color.WHITE));
    assertThat(EventUtil.rgbStringToColor("255,0,0"), is(Color.RED));
    assertThat(EventUtil.rgbStringToColor("0,255,0"), is(Color.GREEN));
    assertThat(EventUtil.rgbStringToColor("0,0,255"), is(Color.BLUE));
    assertThat(EventUtil.rgbStringToColor("120,236,21"), is(new Color(120, 236, 21)));
  }
}
