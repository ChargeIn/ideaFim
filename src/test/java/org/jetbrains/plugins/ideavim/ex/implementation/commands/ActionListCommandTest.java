/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2022 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.jetbrains.plugins.ideafim.ex.implementation.commands;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.util.ArrayUtil;
import com.flop.idea.fim.ex.ExOutputModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ideafim.FimTestCase;

import java.util.List;

/**
 * @author Naoto Ikeno
 */
public class ActionListCommandTest extends FimTestCase {
  public void testListAllActions() {
    configureByText("\n");
    typeText(commandToKeys("actionlist"));

    String output = ExOutputModel.getInstance(myFixture.getEditor()).getText();
    assertNotNull(output);

    // Header line
    String[] displayedLines = output.split("\n");
    assertEquals("--- Actions ---", displayedLines[0]);

    // Action lines
    int displayedActionNum = displayedLines.length - 1;
    List<@NonNls String> actionIds = ActionManager.getInstance().getActionIdList("");
    assertEquals(displayedActionNum, actionIds.size());
  }

  public void testSearchByActionName() {
    configureByText("\n");
    typeText(commandToKeys("actionlist quickimpl"));

    String[] displayedLines = parseActionListOutput();
    for (int i = 0; i < displayedLines.length; i++) {
      String line = displayedLines[i];
      if (i == 0) {
        assertEquals("--- Actions ---", line);
      }
      else {
        assertTrue(line.toLowerCase().contains("quickimpl"));
      }
    }
  }

  public void testSearchByAssignedShortcutKey() {
    configureByText("\n");
    typeText(commandToKeys("actionlist <M-S-"));

    String[] displayedLines = parseActionListOutput();
    for (int i = 0; i < displayedLines.length; i++) {
      String line = displayedLines[i];
      if (i == 0) {
        assertEquals("--- Actions ---", line);
      }
      else {
        assertTrue(line.toLowerCase().contains("<m-s-"));
      }
    }
  }

  private String[] parseActionListOutput() {
    String output = ExOutputModel.getInstance(myFixture.getEditor()).getText();
    return output == null ? ArrayUtil.EMPTY_STRING_ARRAY : output.split("\n");
  }
}
