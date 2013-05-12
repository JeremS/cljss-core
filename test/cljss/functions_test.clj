(ns cljss.functions-test
  (:require cljss.properties)
  (:use cljss.functions
        cljss.protocols
        midje.sweet))

(fact "We can use css function as property values"
  ; exapmle from http://www.suburban-glory.com/blog?page=130
  (compile-as-property-value (counter :paras :decimal))
   => "counter(paras, decimal)"

   ; examples from https://developer.mozilla.org/en-US/docs/Web/CSS/transform?redirectlocale=en-US&redirectslug=CSS%2Ftransform
   (compile-as-property-value (matrix 1.0, 2.0, 3.0, 4.0, 5.0, 6.0))
   => "matrix(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)"

   (compile-as-property-value (translate :12px, :50%))
   => "translate(12px, 50%)"

   (compile-as-property-value (translateX :2em))
   => "translateX(2em)"

   (compile-as-property-value (translateY :3in))
   => "translateY(3in)"

   (compile-as-property-value (scale 2, 0.5))
   => "scale(2, 0.5)"

   (compile-as-property-value (scaleX 2))
   => "scaleX(2)"

   (compile-as-property-value (scaleY 0.5))
   => "scaleY(0.5)"

   (compile-as-property-value (rotate :0.5turn))
   => "rotate(0.5turn)"

   (compile-as-property-value (skewX :30deg))
   => "skewX(30deg)"

   (compile-as-property-value (skewY :1.07rad))
   => "skewY(1.07rad)"

   (compile-as-property-value (matrix3d 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0))
   => "matrix3d(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0)"

   (compile-as-property-value (translate3d :12px :50% :3em))
   => "translate3d(12px, 50%, 3em)"

   (compile-as-property-value (translateZ :2px))
   => "translateZ(2px)"

   (compile-as-property-value (scale3d 2.5 1.2 0.3))
   => "scale3d(2.5, 1.2, 0.3)"

   (compile-as-property-value (scaleZ 0.3))
   => "scaleZ(0.3)"

   (compile-as-property-value (rotate3d 1 2.0 3.0 :10deg))
   => "rotate3d(1, 2.0, 3.0, 10deg)"

   (compile-as-property-value (rotateX :10deg))
   => "rotateX(10deg)"

   (compile-as-property-value (rotateY :10deg))
   => "rotateY(10deg)"

   (compile-as-property-value (rotateZ :10deg))
   => "rotateZ(10deg)"

   (compile-as-property-value (perspective :17px))
   => "perspective(17px)")


