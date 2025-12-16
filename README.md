# Java Core, Concurrency & Systems Practice

This repository contains educational Java projects completed during an intensive 4-month course with focus on modern engineering practices.
The focus is on **Java Core**, data structures, concurrency, performance analysis, and low-level systems programming.

---

## Covered Topics

*   **Java Core & OOP**
*   **Collections Framework** (custom implementations)
*   **Algorithms & complexity analysis**
*   **Multithreading & concurrency**
*   **Java Reflection API**
*   **Performance benchmarking**
*   **ExecutorService & thread pools**
*   **Socket programming & web servers**
*   **Git workflow** (branches, PRs)

---

## Project Structure

### **HW01 – Custom List & Collections Framework**
*   Custom dynamic array implementing `List`
*   Unit tests comparing behavior with `ArrayList`
*   Generic implementation
*   Performance benchmarks vs JDK collections
*   Git fundamentals and workflow practice

**Key topics:** Java Collections, Generics, JUnit, Performance testing, Git

---

### **HW02 – Algorithms, Lombok & Reflection**
*   **Fibonacci implementations:**
    *   Recursive
    *   Memoized
    *   Iterative
*   Time & space complexity analysis
*   Performance and memory benchmarking
*   Array operations performance comparison
*   Custom test runner using Java Reflection API

**Key topics:** Algorithms, Big-O analysis, Lombok, Reflection, Testing frameworks

---

### **HW03 – LinkedList, Stack & Queue**
*   Custom `LinkedList` implementation
*   `Stack`, `Queue` and `Deque` based on custom structures
*   Performance comparison between custom list implementations

**Key topics:** Data structures, abstraction, performance analysis

---

### **HW04 – HashMap & Concurrent Maps**
*   Custom `HashMap` implementing `Map<K, V>`
*   Collision resolution strategies
*   Unit and performance tests vs JDK `HashMap`
*   Thread-safe map implementation
*   `ConcurrentSkipListMap` (advanced task)

**Key topics:** Hashing, concurrency, thread safety, performance

---

### **HW05 – Multithreading & Synchronization**
*   Parallel array processing
*   Platform threads vs virtual threads comparison
*   Banking system simulator with concurrent transfers
*   **Synchronization using:**
    *   `synchronized`
    *   `ReentrantLock`
    *   atomic operations
*   Thread-safe decorators for custom list
*   Guaranteed deadlock scenarios

**Key topics:** Concurrency control, synchronization, virtual threads, race conditions

---

### **HW06 – Custom ExecutorService & Web Server**
*   Custom `ExecutorService` implementation
*   Support for platform and virtual threads
*   Task queue and graceful shutdown
*   Multi-threaded socket-based web server
*   Serving static and dynamic content
*   Performance comparison with load testing

**Key topics:** Thread pools, virtual threads, networking, HTTP, system design

---

## Technologies Used
*   Java 21
*   Maven
*   JUnit 5
*   Lombok
*   Git / GitHub
*   Java Concurrency API
*   Java Reflection API
*   Sockets & Networking

---

## Notes
*   Each homework was developed in a separate Git branch (`hw01` … `hw06`)
*   All implementations were submitted via pull requests
*   Emphasis was placed on **correctness, performance, and understanding of internals**
