# Configuring conflicting keys via .ideafimrc

IdeaFim allows defining handlers for the shortcuts that exist for both IDE and Fim (e.g. `<C-C>`).

```fim
" Use ctrl-c as an ide shortcut in normal and visual modes
sethandler <C-C> n-v:ide i:fim
```

This option consist of an optional shortcut and a list of space separated list of handlers:
`mode-list:handler mode-list:handler ...`  
The `mode-list` is a dash separated list of modes that is similar to `guicursor` notation
and defines the following modes:
 - n - normal mode
 - i - insert mode
 - x - visual mode
 - v - visual and select modes
 - a - all modes

The `handler` is an argument that may accept the following values:
 - ide - use IDE handler
 - fim - use Fim handler

Examples:
 - `n:ide` - use IDE handler in normal mode
 - `i-v:fim` - use Fim handler in normal, visual, and select modes
 - `a:ide` - use IDE handler in all modes

By using `sethandler` you can define handlers:
 - For a single shortcut: `sethandler <C-A> n:fim i-x:ide` - use Fim handler in normal mode and IDE handler in insert and visual modes,
 - For all shortcuts: `sethandler n:fim i:ide` - use Fim handlers in normal mode and IDE handlers in insert mode.

If the definition of the handler is missing for some mode, it defaults to `fim`:
`sethandler <C-X> i:ide` - use IDE handler in insert mode and Fim handler in all other modes.
