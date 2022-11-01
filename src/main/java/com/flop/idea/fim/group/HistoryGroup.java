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

package com.flop.idea.fim.group;

import com.flop.idea.fim.history.HistoryBlock;
import com.flop.idea.fim.history.HistoryConstants;
import com.flop.idea.fim.history.HistoryEntry;
import com.flop.idea.fim.history.FimHistoryBase;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.history.HistoryBlock;
import com.flop.idea.fim.history.HistoryEntry;
import com.flop.idea.fim.history.FimHistoryBase;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

import static com.flop.idea.fim.history.HistoryConstants.*;

@State(name = "FimHistorySettings", storages = {
  @Storage(value = "$APP_CONFIG$/fim_settings_local.xml", roamingType = RoamingType.DISABLED)
})
public class HistoryGroup extends FimHistoryBase implements PersistentStateComponent<Element> {

  public void saveData(@NotNull Element element) {
    logger.debug("saveData");
    Element hist = new Element("history");

    saveData(hist, HistoryConstants.SEARCH);
    saveData(hist, HistoryConstants.COMMAND);
    saveData(hist, HistoryConstants.EXPRESSION);
    saveData(hist, HistoryConstants.INPUT);

    element.addContent(hist);
  }

  private void saveData(@NotNull Element element, String key) {
    final HistoryBlock block = getHistories().get(key);
    if (block == null) {
      return;
    }

    final Element root = new Element("history-" + key);

    for (HistoryEntry entry : block.getEntries()) {
      final Element entryElement = new Element("entry");
      FimPlugin.getXML().setSafeXmlText(entryElement, entry.getEntry());
      root.addContent(entryElement);
    }

    element.addContent(root);
  }

  public void readData(@NotNull Element element) {
    logger.debug("readData");
    Element hist = element.getChild("history");
    if (hist == null) {
      return;
    }

    readData(hist, HistoryConstants.SEARCH);
    readData(hist, HistoryConstants.COMMAND);
    readData(hist, HistoryConstants.EXPRESSION);
    readData(hist, HistoryConstants.INPUT);
  }

  private void readData(@NotNull Element element, String key) {
    HistoryBlock block = getHistories().get(key);
    if (block != null) {
      return;
    }

    block = new HistoryBlock();
    getHistories().put(key, block);

    final Element root = element.getChild("history-" + key);
    if (root != null) {
      List<Element> items = root.getChildren("entry");
      for (Element item : items) {
        final String text = FimPlugin.getXML().getSafeXmlText(item);
        if (text != null) {
          block.addEntry(text);
        }
      }
    }
  }

  @Nullable
  @Override
  public Element getState() {
    Element element = new Element("history");
    saveData(element);
    return element;
  }

  @Override
  public void loadState(@NotNull Element state) {
    readData(state);
  }

  @TestOnly
  public void clear() {
    getHistories().clear();
  }

  private static final Logger logger = Logger.getInstance(HistoryGroup.class.getName());
}
