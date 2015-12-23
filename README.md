# async-agent

Provides asynchronous [agents](http://clojure.org/agents) using 
[Manifold](https://github.com/ztellman/manifold).

![Clojars Project](http://clojars.org/com.joshuagriffith/async-agent/latest-version.svg)

## Usage

Create an asynchronous agent:

```clj
(require '[com.joshuagriffith.async-agent :as a])
(def val (a/async-agent 42))
```

Asynchronous agents return a 
[Manifold deferred](https://github.com/ztellman/manifold/blob/master/docs/deferred.md) 
representing the value of the agent after the operation has been completed:

```clj
(a/async-send val inc)
;; => << 43 >>

@val
;; => 43
```

If an exception is thrown, the agent value will not be updated and the 
exception will be available in the deferred:

```clj
@(a/async-send val / 0)
;; => ArithmeticException Divide by zero  clojure.lang.Numbers.divide (Numbers.java:158)

@val
;; => 43
```

The function provided to `async-send` can return any type that can be coerced
by Manifold:

```clj
@(a/async-send val #(future (Thread/sleep 1000) (inc %)))
;; => 44
```

## License

Copyright © 2015 Joshua Griffith.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
