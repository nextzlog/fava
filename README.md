fava: Functional Programming Language on Scala
====

![image](https://img.shields.io/badge/Gradle-6-red.svg)
![image](https://img.shields.io/badge/Java-SE13-red.svg)
![image](https://img.shields.io/badge/Scala-2.13-orange.svg)
![image](https://img.shields.io/badge/license-BSD%203--Clause-darkblue.svg)

fava is a Turing complete, pure functional programming language, which was designed for the educational purpose in computational theory.

## Features

- The `univ` package contains a universal Turing machine using 2 tapes.
- The `regl` package contains parser combinators for regular languages.
- The `gram` package contains parser combinators for parsing expression grammars (PEGs).
- The `math` package contains an arithmetic calculator using a simple stack machine and PEG.
- The `fava` package contains a fava stack machine and compiler including a REPL.
- The `lisp` package contains a LISP including a REPL.

## Documents

- [Scalaで自作するプログラミング言語処理系 (PDF)](https://pafelog.net/fava.pdf) [(HTML)](https://pafelog.net/fava.html)

## Usage

```sh
$ gradle build
$ java -jar build/libs/fava.jar
which language do you use?
[0]: fava
[1]: math
[2]: univ
[3]: regl
[4]: lisp
select: 0
fava$
```

## Fava Examples

### basic operation

- fava supports some arithmetic, logic, and relational operations.

```Scala
fava$ 114 + 514
628
fava$ 3 > 1? "LOOSE": "WIN"
LOOSE
```

### lambda calculus

- fava does not accept local variable and function declarations, and block statements are also prohibited.

```Scala
fava$ ((x)=>(y)=>3*x+7*y)(2)(3)
27
```

### Church booleans

- fava is Turing complete and can theoretically define boolean operations without using system functions.

```Scala
fava$ ((l,r)=>l(r,(x,y)=>y))((x,y)=>x,(x,y)=>y)(true,false) // true & false
false
fava$ ((l,r)=>l((x,y)=>x,r))((x,y)=>x,(x,y)=>y)(true,false) // true | false
true
```

### Church numerics

- fava is Turing complete and can theoretically define numeric operations without using system functions.

```Scala
fava$ ((l,r)=>(f,x)=>l(f)(r(f)(x)))((f)=>(x)=>f(x),(f)=>(x)=>f(f(x)))((x)=>x+1,0) // 1 + 2
3
fava$ ((l,r)=>(f,x)=>l(r(f))(x))((f)=>(x)=>f(f(x)),(f)=>(x)=>f(f(x)))((x)=>x+1,0) // 2 * 2
4
```

### anonymous recursion

- fava evaluates function arguments lazily.

```Scala
fava$ ((f)=>((x)=>f(x(x)))((x)=>f(x(x))))((f)=>(n)=>(n==0)?1:n*f(n-1))(10)
3628800
```

## Lisp Examples

### name space

- Functions and variables are declared in the same namespace.

```lisp
elva$ (set ’function-in-variable list)
#<native form list>
elva$ (function-in-variable 1 2 3 4 5)
(1 2 3 4 5)
```

### lambda definition

- Use the `define-lambda` macro to define a function.

```lisp
elva$ (define-lambda fact (x) (if (eq x 1) x (* x (fact (- x 1)))))
(lambda (x) (if (eq x 1) x (* x (fact (- x 1)))))
```

### syntax definition

- Use the `define-syntax` macro to define a macro or syntax.

```lisp
elva$ define-lambda
(syntax (name pars body) (list (quote setq) name (list (quote lambda) pars body)))
elva$ define-syntax
(syntax (name pars body) (list (quote setq) name (list (quote syntax) pars body)))
```

## Contribution

Feel free to contact [@nextzlog](https://twitter.com/nextzlog) on Twitter.

## License

### Author

[無線部開発班 (JOURNAL OF HAMRADIO INFORMATICS LETTERS)](https://pafelog.net)

### Clauses

[BSD 3-Clause License](LICENSE.md)
