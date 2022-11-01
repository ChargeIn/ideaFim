<img src="src/main/resources/META-INF/pluginIcon.svg" width="80" height="80" alt="icon" align="left"/>

IdeaFim
===

![[Version]][plugin-version-svg]

IdeaFim is a spinoff of the origin [IdeaVim](https://github.com/JetBrains/ideavim) plugin from JetBrains.
Its aim is to support a more native shortcut support based around more common keybindings.

##### Resources:

* [Original Plugin (IdeaVim)](https://github.com/JetBrains/ideavim)
* [Changelog](CHANGES.md)

#### Compatibility

IntelliJ IDEA, PyCharm, CLion, PhpStorm, WebStorm, RubyMine, AppCode, DataGrip, GoLand, Rider, Cursive,
Android Studio and other IntelliJ platform based IDEs.

Setup
------------

- IdeaFim can be installed via `Settings | Plugins`.
See the [detailed instructions](https://www.jetbrains.com/help/idea/managing-plugins.html#).

- Use `Tools | Fim` in the menu to enable or disable fim.

- Use the `~/.ideafimrc` file as an analog of `~/.fimrc` ([learn more](#Files)). The XDG standard is supported, as well.

- Shortcut conflicts can be resolved by using:
     - On Linux & Windows: `File | Settings | Editor | Fim` & `File | Settings | Keymap`,
     - On macOS: `Preferences | Editor | Fim` & `Preferences | Keymap`,
     - Regular Vim mappings in the  `~/.ideafimrc` file.


Summary of Supported Vim Features
---------------------------------

Here are some examples of supported vim features and commands:

* Normal / insert / visual / select / etc. modes
* Motion / deletion / change / window / etc. commands
* Key mappings
* Marks / Macros / Digraphs / Registers
* Some [set commands](https://github.com/JetBrains/ideavim/wiki/%22set%22-commands)
* Full Vim regexps for search and search/replace
* Vim web help
* `~/.ideavimrc` configuration file

[IdeaVim plugins](https://github.com/JetBrains/ideavim/wiki/Emulated-plugins):

* vim-easymotion
* NERDTree
* vim-surround
* vim-multiple-cursors
* vim-commentary
* argtextobj.vim
* vim-textobj-entire
* ReplaceWithRegister
* vim-exchange
* vim-highlightedyank
* vim-paragraph-motion
* vim-indent-object
* match.it

See also:

* [The list of all supported commands](src/main/java/com/flop/idea/vim/package-info.java)
* [Top feature requests and bugs](https://youtrack.jetbrains.com/issues/VIM?q=%23Unresolved+sort+by%3A+votes)
* [Vimscript support roadmap](vimscript-info/VIMSCRIPT_ROADMAP.md)
* [List of supported in-build functions](vimscript-info/FUNCTIONS_INFO.MD)

Files
-----

* `~/.ideafimrc`
    * Your IdeaFim-specific Vim initialization commands
    
<details>
<summary><strong>Example</strong> (click to see)</summary>

```vim
""" Map leader to space ---------------------
let mapleader=" "

""" Plugins  --------------------------------
set surround
set multiple-cursors
set commentary
set argtextobj
set easymotion
set textobj-entire
set ReplaceWithRegister

""" Plugin settings -------------------------
let g:argtextobj_pairs="[:],(:),<:>"

""" Common settings -------------------------
set showmode
set so=5
set incsearch
set nu

""" Idea specific settings ------------------
set ideajoin
set ideastatusicon=gray
set idearefactormode=keep

""" Mappings --------------------------------
map <leader>f <Plug>(easymotion-s)
map <leader>e <Plug>(easymotion-f)

map <leader>d <Action>(Debug)
map <leader>r <Action>(RenameElement)
map <leader>c <Action>(Stop)
map <leader>z <Action>(ToggleDistractionFreeMode)

map <leader>s <Action>(SelectInProjectView)
map <leader>a <Action>(Annotate)
map <leader>h <Action>(Vcs.ShowTabbedFileHistory)
map <S-Space> <Action>(GotoNextError)

map <leader>b <Action>(ToggleLineBreakpoint)
map <leader>o <Action>(FileStructurePopup)
```
</details>

<details>
<summary><strong>Suggested options</strong> (click to see)</summary>

Here is also a list of the suggested options from [defaults.vim](https://github.com/vim/vim/blob/master/runtime/defaults.vim)

```vim
" Show a few lines of context around the cursor. Note that this makes the
" text scroll if you mouse-click near the start or end of the window.
set scrolloff=5

" Do incremental searching.
set incsearch

" Don't use Ex mode, use Q for formatting.
map Q gq
```
</details>


You can read your `~/.fimrc` file from `~/.ideafimrc` with this command:

    source ~/.fimrc

Also note that if you have overridden the `user.home` JVM option, this
will affect where IdeaVim looks for your `.ideafimrc` file. For example, if you
have `-Duser.home=/my/alternate/home` then IdeaVim will source
`/my/alternate/home/.ideavimrc` instead of `~/.ideafimrc`.

Alternatively, you can set up initialization commands using [XDG](https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html) standard.
Put your settings to `$XDG_CONFIG_HOME/ideafim/ideafimrc` file.


IdeaVim Plugins
--------------------

See [doc/emulated-plugins.md](https://github.com/JetBrains/ideavim/wiki/Emulated-plugins)

Executing IDE Actions
---------------------

IdeaVim adds various commands for listing and executing arbitrary IDE actions as
Ex commands or via `:map` command mappings:

### Executing actions:
* `:action {action_id}`
    * Execute an action by `{action_id}`. Works from Ex command line.
    * Please don't use `:action` in mappings. Use `<Action>` instead.
* `<Action>({action_id})`
    * For the mappings you can use a special `<Action>` keyword. Don't forget the parentheses.
    * E.g. `map gh <Action>(ShowErrorDescription)`  <- execute hover on `gh`.
    * :warning: Mappings to `<Action>` don't work with `noremap`. 
      If you know the case when it's needed, please the original IdeaVim creator [know](https://github.com/JetBrains/ideavim#contact-maintainers).

### Finding action ids:

* IJ provides `IdeaFim: track action Ids` command to show the id of the executed actions.
  This command can be found in "Search everywhere" (double `shift`).

    <details>
        <summary><strong>"Track action Ids" Details</strong> (click to see)</summary>
        <picture>
            <source media="(prefers-color-scheme: dark)" srcset="assets/readme/track_action_dark.gif">
            <img src="assets/readme/track_action_light.gif" alt="track action ids"/>
        </picture>
    </details>


* `:actionlist [pattern]`
    * Find IDE actions by id or keymap pattern (E.g. `:actionlist extract`, `:actionlist <C-D`)

##### Examples:

```vim
" Map \r to the Reformat Code action
:map \r <Action>(ReformatCode)

" Map <leader>d to start debug
:map <leader>d <Action>(Debug)

" Map \b to toggle the breakpoint on the current line
:map \b <Action>(ToggleLineBreakpoint)
```

##### Some popular actions:

```
QuickJavaDoc - Quick Documentation (not only for java, all languages)
ShowErrorDescription - Show description of the error under the caret (cursor hovering)
QuickImplementations - Quick Definition
```

Vim Script
------------

IdeaFim can execute custom scripts that are written with Vim Script.
At the moment we support all language features, but not all of the built-in functions and options are supported.

Additionally, you may be interested in the
[Vim Script Discussion](https://github.com/JetBrains/ideavim/discussions/357) or
[Vim Script Roadmap](https://github.com/JetBrains/ideavim/blob/master/vimscript-info/VIMSCRIPT_ROADMAP.md).


### IDE specific options

You can evaluate the `has('ide')` function call and get `1` if it was called with IdeaVim or `0` if the function was called from Vim/NeoVim.  
The option `&ide` contains the name and edition of your IDE, for example, "IntelliJ IDEA Ultimate Edition".  
To see its value for the current IDE you are using, execute the `:echo &ide` command.  
To write an IDE-specific configuration, use Vim's regexp match operators `=~?` (case-insensitive) / `=~#`  (case-sensitive)

**Example config:**

```vim
" options and mappings that are supported by both Vim and IdeaVim
set nu
set relativenumber

if has('ide')
  " mappings and options that exist only in IdeaVim
  map <leader>f <Action>(GotoFile)
  map <leader>g <Action>(FindInPath)
  map <leader>b <Action>(Switcher)

  if &ide =~? 'intellij idea'
    if &ide =~? 'community'
      " some mappings and options for IntelliJ IDEA Community Edition
    elseif &ide =~? 'ultimate'
      " some mappings and options for IntelliJ IDEA Ultimate Edition
    endif
  elseif &ide =~? 'pycharm'
    " PyCharm specific mappings and options
  endif
else
  " some mappings for Vim/Neovim
  nnoremap <leader>f <cmd>Telescope find_files<cr>
endif
```

:gem: Contributing
------------

The power of contributing drives IdeaVim :muscle:. Even small contributions matter!

See [CONTRIBUTING.md](CONTRIBUTING.md) to start bringing your value to the project.

Authors
-------

See [AUTHORS.md](AUTHORS.md)
for a list of authors and contributors.

IdeaFim tips and tricks
-------

- Use the power of IJ and Vim:
    - `set ideajoin` to enable join via the IDE. See the [examples](https://jb.gg/f9zji9).
    - Make sure `ideaput` is enabled for `clipboard` to enable native IJ insertion in Vim.
    - Sync IJ bookmarks and Vim marks: `set ideamarks`
    - Check out more [ex commands](https://github.com/JetBrains/ideavim/wiki/%22set%22-commands).

- Use your vim settings with IdeaVim. Put `source ~/.fimrc` in `~/.ideafimrc`.
- Control the status bar icon via the [`ideastatusicon` option](https://github.com/JetBrains/ideavim/wiki/%22set%22-commands).
- Not familiar with the default behaviour during a refactoring? See the [`idearefactormode` option](https://github.com/JetBrains/ideavim/wiki/%22set%22-commands).

Some facts about Vim
-------

Let’s relax and have some fun now! Here are a few things we've found interesting during development
and would like to share with you.

- There are no such commands as `dd`, `yy`, or `cc`. For example, `dd` is not a separate command for deleting the line,
but a `d` command with a `d` motion.  
Wait, but there isn't a `d` motion in Vim! That’s right, and that’s why Vim has a dedicated set of commands
for which it checks whether the 
[command equals to motion](https://github.com/vim/vim/blob/759d81549c1340185f0d92524c563bb37697ea88/src/normal.c#L6468)
and if so, it executes `_` motion instead.  
`_` is an interesting motion that isn't even documented in vi, and it refers to the current line.
So, commands like `dd`, `yy`, and similar ones are simply translated to `d_`, `y_`, etc.
[Here](https://github.com/vim/vim/blob/759d81549c1340185f0d92524c563bb37697ea88/src/normal.c#L6502)
is the source of this knowledge.

- `x`, `D`, and `&` are not separate commands either. They are synonyms of `dl`, `d$`, and `:s\r`, respectively.
[Here](https://github.com/vim/vim/blob/759d81549c1340185f0d92524c563bb37697ea88/src/normal.c#L5365)
is the full list of synonyms.

- Have you ever used `U` after `dd`? [Don't even try](https://github.com/vim/vim/blob/759d81549c1340185f0d92524c563bb37697ea88/src/ops.c#L874).

- A lot of variables that refers to visual mode start with two uppercase letters, e.g. `VIsual_active`. [Some examples](https://github.com/vim/vim/blob/master/src/normal.c#L17).

- Other [strange things](https://github.com/vim/vim/blob/759d81549c1340185f0d92524c563bb37697ea88/src/ex_docmd.c#L1845) from vi:
    * ":3"       jumps to line 3
    * ":3|..."   prints line 3
    * ":|"       prints current line

- Vim script doesn't skip white space before comma. `F(a ,b)` => E475.

License
-------
Most of the files belong to the original IdeaVim project and are licensed under the terms of the GNU Public License v2.


<!-- Badges -->
[plugin-version-svg]: https://img.shields.io/badge/version-0.1.0-green.svg
