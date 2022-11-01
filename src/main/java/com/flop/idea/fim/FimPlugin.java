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
package com.flop.idea.fim;

import com.flop.idea.fim.config.FimState;
import com.flop.idea.fim.config.migration.ApplicationConfigurationMigrator;
import com.flop.idea.fim.extension.FimExtensionRegistrar;
import com.flop.idea.fim.group.*;
import com.flop.idea.fim.group.copy.PutGroup;
import com.flop.idea.fim.group.copy.YankGroup;
import com.flop.idea.fim.group.visual.VisualMotionGroup;
import com.flop.idea.fim.helper.MacKeyRepeat;
import com.flop.idea.fim.listener.FimListenerManager;
import com.flop.idea.fim.newapi.IjFimInjector;
import com.flop.idea.fim.ui.StatusBarIconFactory;
import com.flop.idea.fim.ui.ex.ExEntryPanel;
import com.flop.idea.fim.fimscript.services.*;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.keymap.impl.DefaultKeymap;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.flop.idea.fim.api.FimInjectorKt;
import com.flop.idea.fim.api.FimKeyGroup;
import com.flop.idea.fim.group.*;
import com.flop.idea.fim.fimscript.services.FunctionStorage;
import com.flop.idea.fim.fimscript.services.IjFimOptionService;
import com.flop.idea.fim.fimscript.services.OptionService;
import com.flop.idea.fim.fimscript.services.VariableService;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.flop.idea.fim.fimscript.services.FimRcService.executeIdeaFimRc;

/**
 * This plugin attempts to emulate the key binding and general functionality of Fim and gFim. See the supplied
 * documentation for a complete list of supported and unsupported Fim emulation. The code base contains some debugging
 * output that can be enabled in necessary.
 * <p/>
 * This is an application level plugin meaning that all open projects will share a common instance of the plugin.
 * Registers and marks are shared across open projects so you can copy and paste between files of different projects.
 */
@State(name = "FimSettings", storages = {@Storage("$APP_CONFIG$/fim_settings.xml")})
public class FimPlugin implements PersistentStateComponent<Element>, Disposable {

  static {
    FimInjectorKt.setInjector(new IjFimInjector());
  }

  private static final String IDEAVIM_PLUGIN_ID = "IdeaVIM";
  public static final int STATE_VERSION = 7;

  private int previousStateVersion = 0;
  private String previousKeyMap = "";

  // It is enabled by default to avoid any special configuration after plugin installation
  private boolean enabled = true;

  private static final Logger LOG = Logger.getInstance(FimPlugin.class);

  private final @NotNull FimState state = new FimState();

  public Disposable onOffDisposable;

  FimPlugin() {
    ApplicationConfigurationMigrator.getInstance().migrate();
  }

  public void initialize() {
    LOG.debug("initComponent");

    if (enabled) {
      Application application = ApplicationManager.getApplication();
      if (application.isUnitTestMode()) {
        application.invokeAndWait(this::turnOnPlugin);
      }
      else {
        application.invokeLater(this::turnOnPlugin);
      }
    }

    LOG.debug("done");
  }

  @Override
  public void dispose() {
    LOG.debug("disposeComponent");
    turnOffPlugin(false);
    LOG.debug("done");
  }

  /**
   * @return NotificationService as applicationService if project is null and projectService otherwise
   */
  public static @NotNull NotificationService getNotifications(@Nullable Project project) {
    if (project == null) {
      return ApplicationManager.getApplication().getService(NotificationService.class);
    }
    else {
      return project.getService(NotificationService.class);
    }
  }

  public static @NotNull FimState getFimState() {
    return getInstance().state;
  }


  public static @NotNull MotionGroup getMotion() {
    return ApplicationManager.getApplication().getService(MotionGroup.class);
  }

  public static @NotNull XMLGroup getXML() {
    return ApplicationManager.getApplication().getService(XMLGroup.class);
  }

  public static @NotNull ChangeGroup getChange() {
    return ((ChangeGroup)FimInjectorKt.getInjector().getChangeGroup());
  }

  public static @NotNull CommandGroup getCommand() {
    return ApplicationManager.getApplication().getService(CommandGroup.class);
  }

  public static @NotNull MarkGroup getMark() {
    return ((MarkGroup)FimInjectorKt.getInjector().getMarkGroup());
  }

  public static @NotNull RegisterGroup getRegister() {
    return ((RegisterGroup)FimInjectorKt.getInjector().getRegisterGroup());
  }

  public static @NotNull FileGroup getFile() {
    return (FileGroup)FimInjectorKt.getInjector().getFile();
  }

  public static @NotNull SearchGroup getSearch() {
    return ApplicationManager.getApplication().getService(SearchGroup.class);
  }

  public static @Nullable SearchGroup getSearchIfCreated() {
    return ApplicationManager.getApplication().getServiceIfCreated(SearchGroup.class);
  }

  public static @NotNull ProcessGroup getProcess() {
    return ((ProcessGroup)FimInjectorKt.getInjector().getProcessGroup());
  }

  public static @NotNull MacroGroup getMacro() {
    return (MacroGroup)FimInjectorKt.getInjector().getMacro();
  }

  public static @NotNull DigraphGroup getDigraph() {
    return (DigraphGroup)FimInjectorKt.getInjector().getDigraphGroup();
  }

  public static @NotNull HistoryGroup getHistory() {
    return ApplicationManager.getApplication().getService(HistoryGroup.class);
  }

  public static @NotNull KeyGroup getKey() {
    return ((KeyGroup)FimInjectorKt.getInjector().getKeyGroup());
  }

  public static @Nullable KeyGroup getKeyIfCreated() {
    return ((KeyGroup)ApplicationManager.getApplication().getServiceIfCreated(FimKeyGroup.class));
  }

  public static @NotNull WindowGroup getWindow() {
    return ((WindowGroup)FimInjectorKt.getInjector().getWindow());
  }

  public static @NotNull TabService getTabService() {
    return ApplicationManager.getApplication().getService(TabService.class);
  }

  public static @NotNull EditorGroup getEditor() {
    return ApplicationManager.getApplication().getService(EditorGroup.class);
  }

  public static @Nullable EditorGroup getEditorIfCreated() {
    return ApplicationManager.getApplication().getServiceIfCreated(EditorGroup.class);
  }

  public static @NotNull VisualMotionGroup getVisualMotion() {
    return (VisualMotionGroup)FimInjectorKt.getInjector().getVisualMotionGroup();
  }

  public static @NotNull YankGroup getYank() {
    return (YankGroup)FimInjectorKt.getInjector().getYank();
  }

  public static @NotNull PutGroup getPut() {
    return (PutGroup)FimInjectorKt.getInjector().getPut();
  }

  public static @NotNull VariableService getVariableService() {
    return ApplicationManager.getApplication().getService(VariableService.class);
  }

  public static @NotNull OptionService getOptionService() {
    return FimInjectorKt.getInjector().getOptionService();
  }

  public static @NotNull IjFimOptionService getOptionServiceImpl() {
    return (IjFimOptionService)FimInjectorKt.getInjector().getOptionService();
  }

  private static @NotNull NotificationService getNotifications() {
    return getNotifications(null);
  }

  private boolean ideafimrcRegistered = false;

  private void registerIdeafimrc() {
    if (ideafimrcRegistered) return;
    ideafimrcRegistered = true;

    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      FimRcService.executeIdeaFimRc();
    }
  }

  public static @NotNull PluginId getPluginId() {
    return PluginId.getId(IDEAVIM_PLUGIN_ID);
  }

  public static @NotNull String getVersion() {
    final IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(getPluginId());
    if (!ApplicationManager.getApplication().isInternal()) {
      return plugin != null ? plugin.getVersion() : "SNAPSHOT";
    }
    else {
      return "INTERNAL" + (plugin != null ? " - " + plugin.getVersion() : "");
    }
  }

  public static boolean isEnabled() {
    return getInstance().enabled;
  }

  public static void setEnabled(final boolean enabled) {
    if (isEnabled() == enabled) return;

    if (!enabled) {
      getInstance().turnOffPlugin(true);
    }

    getInstance().enabled = enabled;

    if (enabled) {
      getInstance().turnOnPlugin();
    }

    StatusBarIconFactory.Companion.updateIcon();
  }

  public static boolean isError() {
    return FimInjectorKt.getInjector().getMessages().isError();
  }

  public static String getMessage() {
    return FimInjectorKt.getInjector().getMessages().getStatusBarMessage();
  }

  /**
   * Indicate to the user that an error has occurred. Just beep.
   */
  public static void indicateError() {
    FimInjectorKt.getInjector().getMessages().indicateError();
  }

  public static void clearError() {
    FimInjectorKt.getInjector().getMessages().clearError();
  }

  public static void showMode(String msg) {
    FimInjectorKt.getInjector().getMessages().showMode(msg);
  }

  public static void showMessage(@Nls(capitalization = Nls.Capitalization.Sentence) @Nullable String msg) {
    FimInjectorKt.getInjector().getMessages().showStatusBarMessage(msg);
  }

  public static @NotNull FimPlugin getInstance() {
    return ApplicationManager.getApplication().getService(FimPlugin.class);
  }

  /**
   * IdeaFim plugin initialization.
   * This is an important operation and some commands ordering should be preserved.
   * Please make sure that the documentation of this function is in sync with the code
   *
   * 1) Update state
   *    This schedules a state update. In most cases it just shows some dialogs to the user. As I know, there are
   *      no special reasons to keep this command as a first line, so it seems safe to move it.
   * 2) Command registration
   *    This block should be located BEFORE ~/.ideafimrc execution. Without it the commands won't be registered
   *      and initialized, but ~/.ideafimrc file may refer or execute some commands or functions.
   *    This block DOES NOT initialize extensions, but only registers the available ones.
   * 3) ~/.ideafimrc execution
   *    3.1 executes commands from the .ideafimrc file and 3.2 initializes extensions.
   *    3.1 MUST BE BEFORE 3.2. This is a flow of fim/IdeaFim initialization, firstly .ideafimrc is executed and then
   *      the extensions are initialized.
   * 4) Components initialization
   *    This should happen after ideafimrc execution because FimListenerManager accesses `number` option
   *      to init line numbers and guicaret to initialize carets.
   *    However, there is a question about listeners attaching. Listeners registration happens after the .ideafimrc
   *      execution, what theoretically may cause bugs (e.g. VIM-2540)
   */
  private void turnOnPlugin() {
    onOffDisposable = Disposer.newDisposable(this, "IdeaFimOnOffDisposer");

    // 1) Update state
    ApplicationManager.getApplication().invokeLater(this::updateState);

    // 2) Command registration
    // 2.1) Register fim actions in command mode
    RegisterActions.registerActions();

    // 2.2) Register extensions
    FimExtensionRegistrar.registerExtensions();

    // 2.3) Register functions
    FunctionStorage.INSTANCE.registerHandlers();

    // 3) ~/.ideafimrc execution
    // 3.1) Execute ~/.ideafimrc
    registerIdeafimrc();

    // 3.2) Initialize extensions. Always after 3.1
    FimExtensionRegistrar.enableDelayedExtensions();

    // Turing on should be performed after all commands registration
    getSearch().turnOn();
    FimListenerManager.INSTANCE.turnOn();
  }

  private void turnOffPlugin(boolean unsubscribe) {
    SearchGroup searchGroup = getSearchIfCreated();
    if (searchGroup != null) {
      searchGroup.turnOff();
    }
    if (unsubscribe) {
      FimListenerManager.INSTANCE.turnOff();
    }
    ExEntryPanel.fullReset();

    // Unregister fim actions in command mode
    RegisterActions.unregisterActions();

    Disposer.dispose(onOffDisposable);
  }

  private boolean stateUpdated = false;

  private void updateState() {
    if (stateUpdated) return;
    if (isEnabled() && !ApplicationManager.getApplication().isUnitTestMode()) {
      stateUpdated = true;
      if (SystemInfo.isMac) {
        final MacKeyRepeat keyRepeat = MacKeyRepeat.getInstance();
        final Boolean enabled = keyRepeat.isEnabled();
        final Boolean isKeyRepeat = getEditor().isKeyRepeat();
        if ((enabled == null || !enabled) && (isKeyRepeat == null || isKeyRepeat)) {
          // This system property is used in IJ ui robot to hide the startup tips
          boolean showNotification = Boolean.getBoolean("ide.show.tips.on.startup.default.value");
          if (showNotification && FimPlugin.getNotifications().enableRepeatingMode() == Messages.YES) {
            getEditor().setKeyRepeat(true);
            keyRepeat.setEnabled(true);
          }
          else {
            getEditor().setKeyRepeat(false);
          }
        }
      }
      if (previousStateVersion > 0 && previousStateVersion < 3) {
        final KeymapManagerEx manager = KeymapManagerEx.getInstanceEx();
        Keymap keymap = null;
        if (previousKeyMap != null) {
          keymap = manager.getKeymap(previousKeyMap);
        }
        if (keymap == null) {
          keymap = manager.getKeymap(DefaultKeymap.getInstance().getDefaultKeymapName());
        }
        assert keymap != null : "Default keymap not found";
        manager.setActiveKeymap(keymap);
      }
      if (previousStateVersion > 0 && previousStateVersion < 4) {
        FimPlugin.getNotifications().noFimrcAsDefault();
      }
    }
  }

  @Override
  public void loadState(final @NotNull Element element) {
    LOG.debug("Loading state");

    // Restore whether the plugin is enabled or not
    Element state = element.getChild("state");
    if (state != null) {
      try {
        previousStateVersion = Integer.parseInt(state.getAttributeValue("version"));
      }
      catch (NumberFormatException ignored) {
      }
      enabled = Boolean.parseBoolean(state.getAttributeValue("enabled"));
      previousKeyMap = state.getAttributeValue("keymap");
    }

    legacyStateLoading(element);
    this.state.readData(element);
  }

  @Override
  public Element getState() {
    LOG.debug("Saving state");

    final Element element = new Element("ideafim");
    // Save whether the plugin is enabled or not
    final Element state = new Element("state");
    state.setAttribute("version", Integer.toString(STATE_VERSION));
    state.setAttribute("enabled", Boolean.toString(enabled));
    element.addContent(state);

    this.state.saveData(element);

    return element;
  }

  private void legacyStateLoading(@NotNull Element element) {
    if (previousStateVersion > 0 && previousStateVersion < 5) {
      // Migrate settings from 4 to 5 version
      getMark().readData(element);
      getRegister().readData(element);
      getSearch().readData(element);
      getHistory().readData(element);
    }
    if (element.getChild(KeyGroup.SHORTCUT_CONFLICTS_ELEMENT) != null) {
      getKey().readData(element);
    }
    if (element.getChild(EditorGroup.EDITOR_STORE_ELEMENT) != null) {
      getEditor().readData(element);
    }
  }
}
