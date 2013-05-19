# cljss

Cljss provides a DSL to write CSS as clojure data, similar to
[css-gen](https://github.com/paraseba/cssgen/tree/0.3.0) in the 0.3.0 branch.
More precisely the rules syntax more or less the same syntax as css-gen with
somme tricks of my own.

Quick example:

```clojure
(use 'cljss.core)

(css [[:section :div]
        :width :900px])
; => "section div { width: 900px; }"

```

## Installation
In `project.clj`:
```clojure
[jeremys/cljss-core "0.3.0"]
```

## Documentation
- [wiki](https://github.com/JeremS/cljss-core/wiki)


## Thanks
Obviousily thanks to [@paraseba](https://github.com/paraseba) for the ideas
I used from [css-gen](https://github.com/paraseba/cssgen/tree/0.3.0).

A thanks to [Kodowa](http://www.kodowa.com) too, I'm having a very good time
writing this code with Ligh Table !

## Todo
 - add optional indenting in the output.
 - ClojureScript adaptation ?
 - syntax checking / error reporting ?


## License

Copyright © 2013 Jérémy Schoffen.

Distributed under the Eclipse Public License, the same as Clojure.
