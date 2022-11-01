Emulated Fim Plugins
--------------------

IdeaFim extensions emulate plugins of the original Fim. In order to use
IdeaFim extensions, you have to enable them via this command in your `~/.ideafimrc`:

```
Plug '<extension-github-reference>'
```

If you reuse your existing `.fimrc` file using `source ~/.fimrc`, IdeaFim can parse and enable plugins that are defined
using [fim-plug](https://github.com/junegunn/fim-plug) or [vundle](https://github.com/VundleFim/Vundle.fim).
No additional set commands in `~/.ideafimrc` are required.
If you'd like to disable some plugin that's enabled in `.fimrc`, you can use `set no<extension-name>`
in `~/.ideafimrc`. E.g. `set nosurround`.

Available extensions:

## easymotion

* Setup:
    * Install [IdeaFim-EasyMotion](https://plugins.jetbrains.com/plugin/13360-ideafim-easymotion/)
      and [AceJump](https://plugins.jetbrains.com/plugin/7086-acejump/) plugins.
    * `Plug 'easymotion/fim-easymotion'`
    * <details>
      <summary>Alternative syntax</summary>
      <code>Plug 'https://github.com/easymotion/fim-easymotion'</code>
      <br/>
      <code>Plug 'fim-easymotion'</code>
      <br/>
      <code>set easymotion</code>
      </details>
* Emulates [fim-easymotion](https://github.com/easymotion/fim-easymotion)
* Commands: All commands with the mappings are supported. See the [full list of supported commands](https://github.com/AlexPl292/IdeaFim-EasyMotion#supported-commands).

## NERDTree
* Setup: `Plug 'preserfim/nerdtree'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/preserfim/nerdtree'</code>
      <br/>
      <code>Plug 'nerdtree'</code>
      <br/>
      <code>set NERDTree</code>
      </details>
* Emulates [NERDTree](https://github.com/preserfim/nerdtree)
* Commands: [[see here|NERDTree-support]]

## surround

* Setup: `Plug 'tpope/fim-surround'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/tpope/fim-surround'</code>
      <br/>
      <code>Plug 'fim-surround'</code>
      <br/>
      <code>Plug 'https://www.fim.org/scripts/script.php?script_id=1697'</code>
      <br/>
      <code>set surround</code>
      </details>
* Emulates [fim-surround](https://github.com/tpope/fim-surround)
* Commands: `ys`, `cs`, `ds`, `S`

## multiple-cursors

* Setup: `Plug 'terryma/fim-multiple-cursors'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/terryma/fim-multiple-cursors'</code>
      <br/>
      <code>Plug 'fim-multiple-cursors'</code>
      <br/>
      <code>set multiple-cursors</code>
      </details>
* Emulates [fim-multiple-cursors](https://github.com/terryma/fim-multiple-cursors)
* Commands: `<A-n>`, `<A-x>`, `<A-p>`, `g<A-n>`

## commentary

* Setup: `Plug 'tpope/fim-commentary'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/tpope/fim-commentary'</code>
      <br/>
      <code>Plug 'fim-commentary'</code>
      <br/>
      <code>Plug 'https://www.fim.org/scripts/script.php?script_id=3695'</code>
      <br/>
      <code>Plug 'tomtom/tcomment_fim'</code>
      <br/>
      <code>Plug 'tcomment_fim'</code>
      <br/>
      <code>Plug 'https://www.fim.org/scripts/script.php?script_id=1173'</code>
      <br/>
      <code>set commentary</code>
      </details>
* Emulates [commentary.fim](https://github.com/tpope/fim-commentary)
* Commands: `gcc`, `gc + motion`, `v_gc`
* By [Daniel Leong](https://github.com/dhleong)

## ReplaceWithRegister

* Setup: `Plug 'fim-scripts/ReplaceWithRegister'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/fim-scripts/ReplaceWithRegister'</code>
      <br/>
      <code>Plug 'ReplaceWithRegister'</code>
      <br/>
      <code>Plug 'https://github.com/inkarkat/fim-ReplaceWithRegister'</code>
      <br/>
      <code>Plug 'inkarkat/fim-ReplaceWithRegister'</code>
      <br/>
      <code>Plug 'fim-ReplaceWithRegister'</code>
      <br/>
      <code>Plug 'https://www.fim.org/scripts/script.php?script_id=2703'</code>
      <br/>
      <code>set ReplaceWithRegister</code>
      </details>
* Emulates [ReplaceWithRegister](https://github.com/fim-scripts/ReplaceWithRegister)
* Commands: `gr`, `grr`
* By [igrekster](https://github.com/igrekster)

## argtextobj

* Setup:
    * `Plug 'fim-scripts/argtextobj.fim'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/fim-scripts/argtextobj.fim'</code>
      <br/>
      <code>Plug 'argtextobj.fim'</code>
      <br/>
      <code>Plug 'https://www.fim.org/scripts/script.php?script_id=2699'</code>
      <br/>
      <code>set argtextobj</code>
      </details>
    * By default, only the arguments inside parenthesis are considered. To extend the functionality
      to other types of brackets, set `g:argtextobj_pairs` variable to a comma-separated
      list of colon-separated pairs (same as VIM's `matchpairs` option), like
      `let g:argtextobj_pairs="(:),{:},<:>"`. The order of pairs matters when
      handling symbols that can also be operators: `func(x << 5, 20) >> 17`. To handle
      this syntax parenthesis, must come before angle brackets in the list.
* Emulates [argtextobj.fim](https://www.fim.org/scripts/script.php?script_id=2699)
* Additional text objects: `aa`, `ia`

## exchange

* Setup: `Plug 'tommcdo/fim-exchange'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/tommcdo/fim-exchange'</code>
      <br/>
      <code>Plug 'fim-exchange'</code>
      <br/>
      <code>set exchange</code>
      </details>
* Emulates [fim-exchange](https://github.com/tommcdo/fim-exchange)
* Commands: `cx`, `cxx`, `X`, `cxc`
* By [fan-tom](https://github.com/fan-tom)

## textobj-entire

* Setup: `Plug 'kana/fim-textobj-entire'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/kana/fim-textobj-entire'</code>
      <br/>
      <code>Plug 'fim-textobj-entire'</code>
      <br/>
      <code>Plug 'https://www.fim.org/scripts/script.php?script_id=2610'</code>
      <br/>
      <code>set textobj-entire</code>
      </details>
* Emulates [fim-textobj-entire](https://github.com/kana/fim-textobj-entire)
* Additional text objects: `ae`, `ie`
* By [Alexandre Grison](https://github.com/agrison)

## highlightedyank

* Setup:
    * `Plug 'machakann/fim-highlightedyank'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/machakann/fim-highlightedyank'</code>
      <br/>
      <code>Plug 'fim-highlightedyank'</code>
      <br/>
      <code>set highlightedyank</code>
      </details>
    * if you want to optimize highlight duration, assign a time in milliseconds:  
      `let g:highlightedyank_highlight_duration = "1000"`  
      A negative number makes the highlight persistent.  
      `let g:highlightedyank_highlight_duration = "-1"`
    * if you want to change background color of highlight you can provide the rgba of the color you want e.g.  
      `let g:highlightedyank_highlight_color = "rgba(160, 160, 160, 155)"`
* Emulates [fim-highlightedyank](https://github.com/machakann/fim-highlightedyank)
* By [KostkaBrukowa](https://github.com/KostkaBrukowa)

## fim-paragraph-motion

* Setup: `Plug 'dbakker/fim-paragraph-motion'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/dbakker/fim-paragraph-motion'</code>
      <br/>
      <code>Plug 'fim-paragraph-motion'</code>
      <br/>
      <code>Plug 'https://github.com/fim-scripts/Improved-paragraph-motion'</code>
      <br/>
      <code>Plug 'fim-scripts/Improved-paragraph-motion'</code>
      <br/>
      <code>Plug 'Improved-paragraph-motion'</code>
      <br/>
      <code>set fim-paragraph-motion</code>
      </details>
* Emulates [fim-paragraph-motion](https://github.com/dbakker/fim-paragraph-motion)

## fim-indent-object

* Setup: `Plug 'michaeljsmith/fim-indent-object'`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'https://github.com/michaeljsmith/fim-indent-object'</code>
      <br/>
      <code>Plug 'fim-indent-object'</code>
      <br/>
      <code>set textobj-indent</code>
      </details>
* Emulates [fim-indent-object](https://github.com/michaeljsmith/fim-indent-object)
* Additional text objects: `ai`, `ii`, `aI`
* By [Shrikant Sharat Kandula](https://github.com/sharat87)

## matchit.fim

* Setup: `packadd matchit`
    * <details>
      <summary>Alternative fim-plug / vundle syntax</summary>
      <code>Plug 'fim-matchit'</code>
      <br/>
      <code>Plug 'chrisbra/matchit'</code>
      <br/>
      <code>set matchit</code>
      </details>
* Emulates [matchit.fim](https://github.com/chrisbra/matchit)
* Currently works for HTML/XML and ruby
* By [Martin Yzeiri](https://github.com/myzeiri)
