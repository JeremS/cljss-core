## Changelog
### 0.3.0
 - CHANGED: Suppression of the combinators function, the use of infix combinators
 is more natural.
 - internal refactoring.
 - removed inline css inside rules, the behaviour isn't well thought right now.

### 0.2.1
 - FIXED: Numbers can now be used as properties value.
 - FIXED: Cljss behaves like hiccup in presence of lists or lazy seq.
 It basically functions as if the parenthesis weren't there.
 - FIXED: The double newline in the output is fixed.
 - FIXED: empty rules are no more part of the compiled output.

 - ADDED: More generic descendant selector: we can now use any sequential
 instead of just vectors.
 - ADDED: More generic property values, sequentials give space separated values,
 sets give comma separated ones.
 - ADDED: Support for CSS 2 & 3 functions like `url` or `matrix3d`.
 - ADDED: Rules like selectors for media queries.
 - ADDED: Now the keywords `:> :+ :~` can be used as combinators
 inside a vector of selectors instead of the `c-> c-+ c-g+` functions.
 - ADDED: Compact style.
 - ADDED: Use of keywords for pseudo classes args.
 - ADDED: Possible break lines in the set selectors output.
 - ADDED: Helpers to have a more fluent API.
 - ADDED: We can now use inline css or css comments. There
 is a compilation option that allows the comments to be in the
 compiled output or not.

### 0.2.0
 - ADDED: support for media queries.
 - ADDED: support for attributes selectors.