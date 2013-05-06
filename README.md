# cljss

Cljss provides a DSL similar to
[css-gen](https://github.com/paraseba/cssgen/tree/0.3.0) in the 0.3.0 branch.
More precisely the rules syntax more or less the same syntax as css-gen with
somme tricks of my own.

## Rule syntax
### simple rules
Rules are represented with vectors, the first element being a selector,
the rest can be property declarations or nested rules.
For instance, the rules:

```clojure
	[:section :color :black]
		[:div :color :white]
```

will give the css:

```css
section {color: black;}
div {color: white;}
```

The properties can be a chain of a mix and match of key values,
maps or lists in any order.
The rule:
```clojure
[:#container :background-color :black
             (list :width "900px" :height "400px")
             :border ["1px" :solid :white]
             {:position :relative
              :top "30px"
              :left "30px"}
             :color :blue]
```

will give css similar to:

```css
  #container {
    background-color: black;
    width: 900px;
    height: 400px;
    border: 1px solid white;
    position: relative;
    top: 30px;
    left: 30px;
    color: blue;
  }
```

This way we can create mixins directly in clojure:

```clojure
(defn css-float [side]
  {:float side})

(def default-box
  '(:padding ["0px" "20px"]
    :margin-left "10px"))

[:#nav (css-float :left) default-box]
```

generates:

```css
#nav {
  margin-left: 10px;
  padding: 0px 20px;
  float: left;
}
```


### nested rules
We can also nest rule "à la" scss, stylus etc...

```clojure
[:#container
  :border "1px solid black"
  :padding-left "30px"

  [:a :color :green]

  [:section
    :font-size "1em"
    ["p::first-letter"
      :font-size "2em"]]]
```
gives something like:

```css
#container {
  padding-left: 30px;
  border: 1px solid black;
}
  #container a {
    color: green;
  }
  #container section {
    font-size: 1em;
  }
    #container section p::first-letter {
      font-size: 2em;
    }
```

## selectors
### simple selectors
String or key words are used for simple selectors:
```clojure
[:div.class1.class2 ...]
=> "div.class1.class2"

["div.class1.class2" ...]
=> "div.class1.class2"
```

### combining selectors
Css provide 4 ways to combine selectors
 - the descendant combinator, spaced selectors in css, vector of selectors in cljss

 ```clojure
 [[:div :a] ...] => "div a { ... }"
 ```

 - the children combinator, > character in css, function c-> in cljss

 ```clojure
 [(c-> :div :a) ...] => "div > a { ... }"
 ```

 - the siblings combinator, + character in css, function c-+ in cljss

 ```clojure
 [(c-+ :div :a) ...] => "div + a { ... }"
 ```

 - the general siblings combinator, ~ character in css, function c-g+ in cljss

 ```clojure
 [(c-g+:div :a) ...] => "div ~ a { ... }"
 ```

We can of course combine them:

```clojure
[[:section (c-> :div (c-+ :p :a)) :span] ...] => "section div > p + a span { ... }"
```

We can also use sets to represent list of selectors have the same properties:

```clojure
[[:.class1 (c-> #{:ul :ol} :li)] ...] => ".class1 ul > li, .class1 ol > li"
````


### pseudos & attribute selectors
Pseudo classes, pseudo elements and attribute selectors are implemented as functions that you can use
to enrich a selector. The pseudo class will appear as a suffix to the selector parameter:

```clojure
[(hover :a) ... ] => "a:hover { ... }"

[(first-letter :p) ... ] => "a::first-letter { ... }"

[(-> (c-> :ul :li) hover (nth-child "even")) ... ] => "ul > li:hover:nth-child(even)  { ... }"
[(-> :a (att-sel "href=\"http://...\"")) ...] =>  a[href="http://..."] { ... }

```


### parent selectors
The selector `&` is inspired by its namesake in [sass](http://sass-lang.com),
[stylus](http://learnboost.github.io/stylus/)...
However its semantic is different in cljss. As of now, inside nested rules,
cljss combines a parent selector with its child when the selector `&` is not used.
When it is, it just replaces the selector `&` with the selector of the parent rule.

 - When we don't use '&', here is the behaviour in sass:

```scss
 section {
 ...
   div { ... }
 }
 ```

 generates:

 ```css
 section { ... }
 section div { ... }
 ```

 Here the selectors "section" and "div" are combined in "section div" for the second rule.
 The same is true for cljss:

```clojure
 [:section ...
   [:div ...]]
   => "section {...} section div {}"
 ```

 - at first look cljss behaves like sass when '&' is used:

 ```scss
 a {...
   &:hover { ... }}
 ```

 generates:

 ```css
 a { ... }
 a:hover {...}
 ```

 and
 ```clojure
 [:a ...
   [(-> & hover) ...]]
 => "a {} a:hover { ... }"
 ```

 - it became diffenrent when sets are involved
   in sass:

 ```scss
 section { ...
   & , div {...}
 }
 ```

 generates:

 ```css
 section {...}
 section,
 section div { ... }
 ```

 while with cljss:

 ```clojure
 [:section ...
   [#{& :div} ...]]
 ```

 generates:

 ```css
 section {...}
 section, div { ... }
 ```

### Media queries
There is now a support for media queries similar the one in [sass](http://sass-lang.com).

```clojure
(css [#{:div :section}
         :background-color :blue
         :width "800px"

         [:p
           :font-size "12pt"

           (media "(max-width: 500px)"
                 :font-size "5pt"
                 [:a :color :green])]

        (media "(max-width: 400px)"
               :width "400px")])
````

generates:

```css
div, section {
  width: 800px;
  background-color: blue;
}
  section p, div p {
    font-size: 12pt;
  }
    @media (max-width: 500px) {
      div p a, section p a {
        color: green;
      }
      section p, div p {
        font-size: 5pt;
      }
    }
  @media (max-width: 400px) {
    div, section {
      width: 400px;
    }
  }

```


You can find the examples used this README in the namespace `cljss.examples`.

## Thanks
Obviousily thanks to [@paraseba](https://github.com/paraseba) for the ideas
I used from [css-gen](https://github.com/paraseba/cssgen/tree/0.3.0).

A thanks to [Kodowa](http://www.kodowa.com) too, I'm having a very good time
writing this code with Ligh Table !

## Todo
 - add utilities:
  - functions rules, group-rules, comment
 - ClojureScript version ?
 - Parser of selectors in order to use combinators with infix notation.
 (Could be an extension)
 - syntax checking / error reporting ?

## Changelog
### 0.2.1
 - FIX: Numbers can now be used as properties value.
 - FIX: Cljss behaves like hiccup in presence of lists or lazy seq.
 It basically functions as if the parenthesis weren't there.
 - FIX: The double newline in the output is fixed.


 - ADDED: Inline css in rules as a string.
 - ADDED: Compact style.
 - ADDED: Use of key words for pseudo classes args

### 0.2.0
 - ADDED: support for media queries.
 - ADDED: support for attributes selectors.


## License

Copyright © 2013 Jérémy Schoffen.

Distributed under the Eclipse Public License, the same as Clojure.
