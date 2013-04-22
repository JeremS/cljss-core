# cljss

Cljss provides some kind of dsl similar to css-gen in order to write with
clojure data structure css like rules. As previously quoted this library is 
heavyly inspired by css-gen for the syntax.

## Usage

### Rule syntax
#### simple rule
#### nested rules
### selectors
#### simple selectors
#### combining selectors
#### pseudos 
#### parent selectors

FIXME

## Todo 
 - change the not and empty pseudo classes so they dont conflict with clojure.core functions
 - only one namespace with the protocols
  - create a cljss.protocols
  - moves the implementation of compilation protocols away from compilation.clj
   - move the tests accordingly
 - remove the special case of descendant type of selectors
  - it provides allows for a uniform simplfication of combined selectors
    (think combination of the selectors inside c-> for instance which is not currently done 
    during simplification of this kind of combinators)

 - potemkin to create the api in cljss.core
  - default decorator : chaining of decorator used in precompilation
  - compilation -> some kind of css macro that compiles rules
  - require the protocols, implementation
  - a way to compile
  - constructor of the differents part of the dsl
 - attibute selectors
 - media queries
 - move the implementations of colors and length units in their own libraries
   and just keep the protocols implementations.

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.