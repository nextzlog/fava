fava: Functional Programming Language on Scala
====

![image](https://img.shields.io/badge/Java-SE13-red.svg)
![image](https://img.shields.io/badge/Scala-2.13-orange.svg)
![image](https://img.shields.io/badge/Gradle-6-orange.svg)
![image](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg)

fava is a pure functional programming language written in Scala, which was designed for the educational purpose in computational theory.

## Features

- fava evaluates function arguments lazily.
- fava has no library functions but supports some basic data types, and arithmetic, logic, and relational operations.
- fava accepts no declaration of local variables and functions, and block statements are also prohibited.
- fava is Turing-complete, allowing recursive computation, and complex operation of structural data in theory.

## Packages

- `univ` is a Scala implementation of a universal Turing machine using two tapes.
- `regl` is a simple implementation of parser combinators for regular languages.
- `gram` is a simple implementation of parser combinators for parsing expression grammars (PEGs).
- `math` is an arithmetic calculator using a simple stack machine and an arithmetic parser based on PEG.
- `fava` is the implementation of the fava stack machine and compiler, including a read eval print loop (REPL).

## Documents

- [Scalaで自作するプログラミング言語処理系 (PDF)](https://pafelog.net/fava.pdf) [(HTML)](https://pafelog.net/fava.html)

## Usage

```sh
$ java -jar build/libs/fava.jar
which language do you use?
[0]: fava
[1]: math
select: 0
fava$
```

## Sample Codes

### lambda calculus

```Scala
fava$ ((x)=>(y)=>3*x+7*y)(2)(3)
27
```

### Church booleans

```Scala
fava$ ((l,r)=>l(r,(x,y)=>y))((x,y)=>x,(x,y)=>y)(true,false) // true & false
false
fava$ ((l,r)=>l((x,y)=>x,r))((x,y)=>x,(x,y)=>y)(true,false) // true | false
true
```

### Church numerics

```Scala
fava$ ((l,r)=>(f,x)=>l(f)(r(f)(x)))((f)=>(x)=>f(x),(f)=>(x)=>f(f(x)))((x)=>x+1,0) // 1 + 2
3
fava$ ((l,r)=>(f,x)=>l(r(f))(x))((f)=>(x)=>f(f(x)),(f)=>(x)=>f(f(x)))((x)=>x+1,0) // 2 * 2
4
```

### anonymous recursion

```Scala
fava$ ((f)=>((x)=>f(x(x)))((x)=>f(x(x))))((f)=>(n)=>(n==0)?1:n*f(n-1))(10)
3628800
```

## Build

```sh
$ gradle build
```

## Contribution

Feel free to contact [@nextzlog](https://twitter.com/nextzlog) on Twitter.

## License

### Author

[無線部開発班 (JOURNAL OF HAMRADIO INFORMATICS LETTERS)](https://pafelog.net)

### Clauses

[BSD 3-Clause License](LICENSE.md)
