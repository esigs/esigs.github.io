{:title "TIL: Basically getting TDD for free with Clojure"
 :description "Running my dev tooling in the REPL has bsaically given me free tests"
 :date #inst "2025-12-17"
 :layout "post"}

I feel like I'm probably a bit late to the party with this, but I just learned yet another reason why I love Clojure.

In most languages, Test-Driven Development (TDD) feels like a bit of a ritual. You write a test, switch contexts to a terminal, run a watcher, and wait for a green bar. In Clojure, it just clicked that you don't have to "do" the TDD ritual. The environment just gives it to you for free if you structure your fixtures correctly.

# Classic tests

When testing something stateful (databases, third-party-system, etc...), we usually wrap everything in fixtures to handle the lifecycle. I set mine up with a state-atom so I can inspect what's happening under the hood.

```Clojure
(def state-atom (atom nil))

(defn setup []
  ;; do some required setup
  ;; maybe an in-mem conn or mock service
  (let [my-system (start-my-system!)]
    (reset! state-atom {:system my-system})))

(defn teardown []
  ;; stop the system and reset the atom
  (stop! (:system @state-atom))
  (reset! state-atom nil))

(defn test-fixture [f]
  (setup)
  (f)
  (teardown))

(use-fixtures :each test-fixture)

(deftest my-awesome-test
    ...)
```

# What I have been doing. The Manual Scratchpad

Initially, I was using a comment block at the bottom of my test file to manually spin things up. I'd run `(setup)`, then take the logic from the inner part of my `deftest` (typically I'd wrap it in a `let` so I can pick and choose pieces I want to inspect), and treat it as my scratchpad.

This is already pretty great because I'm "in" the execution context. However, it’s manual. You run `(teardown)` manually when you've screwed something up, and you end up with REPL state. Sometimes I'd break my state-atom and then I have to and run `(setup)` again. It's a mess.

# The "Aha!" Moment

Finally, A colleage pointed out that `test-fixture` is just a function! It doesn't care if it's being called by a test runner or by me in a comment block.

Instead of manually managing the lifecycle, I can just pass my "scratchpad" logic directly to the fixture. I can pull my test logic into an actual function and call it directly from both my deftest and my REPL.

```Clojure

(defn do-my-amazing-test [] 
  (let [sys (:system @state-atom)
        res (query-system sys)]
    (is (= :ok res))
    res))

(deftest amazing-test
  (do-my-amazing-test))

(comment
    ;; The "Aha!" call:
    (test-fixture do-my-amazing-test)

    ;; Still here if I need them:
    (setup)
    (teardown))
```

*technically you can just call it wihtout pulling it into another function, but you wont see the results if it's passing, which is nice in tests, but bad for humans*

# "Free" TDD

I used to basically live in an `eric.clj` file that had easy REPL functions and my working interface or system for me, then translate what I thought needed to be under test into an actual test.

With this pattern, I'm getting all my favorite parts of the REPL, quick iteration, trying out functions, and instant feedback. But at the end, I have a working test ready to go. With a bit of cleanup, maybe a comment or renaming of variables, the scratchpad experiment becomes a long-term test.

I've been driving my development with this pattern for the last few weeks and I'm loving it.
