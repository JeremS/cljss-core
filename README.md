# cljss

Cljss provides some kind of dsl similar to css-gen in order to write with
clojure data structure css like rules. As previously quoted this library is 
heavyly inspired by css-gen for the syntax.

## Usage

### Rule syntax
#### simple rules
A rules a represented with vectors, the first element being a selector,
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

The properties can be a chaining of a mix and match of key values, 
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
  width: "900px"; 
  height: "400px";
  border: 1px solid white;
  position: relative;
  top: "30px";
  left: "30px";
  color: blue;
}
```

This way we can create mixins directly in clojure:

```clojure
(defn float [side]
  {:float side})

(def default-box
  '(:padding ["0px" "20px"]
    :margin-left "10px"))
```

and a rule
```clojure
[:#nav (float :left) default-box]
```


#### nested rules
We can also nest rule "à la" scss etc...

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
  border: "1px solid black";
  padding-left: "30px";
}

#container a { 
  color: green;
}

#container section {
  font-size: 1em;
}

#container a section p::first-letter{
  :font-size: 2em;
}
```

### selectors
#### simple selectors
String or key words are used for simple selectors:
```clojure
[:div.class1.class2 ...]
=> "div.class1.class2"

["div.class1.class2" ...]
=> "div.class1.class2"
```

#### combining selectors
Css provide 4 way that I know of to combine selectors
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
[[:section (c-> :div (c-+ :p :a)) :span] ...] => "section div > p + a > span { ... }"
```


We can also use sets to represent list of selectors have the same properties:

```clojure
[[:.class1 (c-> #{:ul :ol} :li)] ...] => ".class1 ul > li, .class1 ol > li" 
````


#### pseudos 
Pseudo classes and pseudo elements are implemented as function that you can use
to enrich a selector. The pseudo class will appear as a suffix

```clojure
[(hover :a) ... ] => "a:hover { ... }"

[(first-letter :p) ... ] => "a::first-letter { ... }"

[(-> (c-> :ul :li) hover (nth-child "even")) ... ] => "ul > li:hover:nth-child(even)  { ... }"

```


#### parent selectors
The selector `&` is inspired by its namesake in sass, stylus... 
However its semantic is different in cljss. For now, inside nested rules, 
cljss combines a parent selector with its child when the selector `&` is not used. 
When it is, it just replaces the selector `&` with the selector of the parent rule.

 - When we don't use it here is the behaviour in sass:
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
 
 - at first look cljss behaves like sass
 ```scss
 a {...
   &:hover { ... }}
 ```
 generates
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
 
 while with cljss
 ```clojure
 [:section ...
   [#{& :div} ...]]
 ```
 generates:
 ```css
 section {...}
 section, div { ... }
 ```

FIXME

## Todo 
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