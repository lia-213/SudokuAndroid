# SudokuAndroid

## Attribution / Credit
This project is a fork of **[GraphSudokuOpen](https://github.com/BracketCove/GraphSudokuOpen)** by **[BracketCove](https://github.com/BracketCove)**. 

The original project serves as the foundation for this repository. Specifically, the following components were designed and authored by BracketCove:
- The core Sudoku game logic and architecture.
- The Graph data structure approach to Sudoku modeling.
- The original Sudoku solver algorithm, including the innovative "Nice Values" technique.
- The initial Jetpack Compose UI boilerplate.

**If you find this project helpful, please consider supporting the original author by [downloading their free app on the Play Store](https://play.google.com/store/apps/details?id=com.bracketcove.graphsudoku).**

---

## What this fork actually is
I am using this excellent project as a learning base to deepen my understanding of Jetpack Compose, MVI architecture, and advanced graph algorithms. My work so far has focused on modernizing the build system to work with current Android development standards and troubleshooting compatibility issues across different platforms (macOS/Windows). 

Moving forward, I plan to iterate on the solver algorithm and eventually implement a **Multiplayer** mode on top of the existing single-player foundation - this will be for my University dissertation. At this stage, the core game mechanics and original algorithm remain the work of the original author.

---

## Modernization Steps
The project has been significantly upgraded by me from the original boilerplate to align with modern Android tooling:

| Component | Original Boilerplate | Current (Fork) |
| :--- | :--- | :--- |
| **Gradle DSL** | Groovy (`.gradle`) | **Kotlin DSL (`.gradle.kts`)** |
| **Version Management** | Manual in `build.gradle` | **Version Catalog (`libs.versions.toml`)** |
| **Gradle Version** | ~7.0 | **8.10.2** |
| **Android Gradle Plugin** | ~7.x | **8.6.1** |
| **Kotlin Version** | ~1.5.0 | **1.9.24** |
| **Java / JDK** | 1.8 | **17 (via Java Toolchains)** |
| **Compose UI** | Material 2 | **Material 3** |
| **Min SDK** | 21 | **23** |
| **Target / Compile SDK** | 30 | **34** |
| **Protoc Compiler** | 3.10.0 | **3.25.1 (Apple Silicon Support)** |

---

## Dependency Issues & Resolutions
Modernizing a codebase from 2021/2022 to 2024/2025 standards presented several challenges:

### 1. The 'lifecycle' Property Error
- **Error:** `Could not get unknown property 'lifecycle' for build of type org.gradle.invocation.DefaultGradle`.
- **Cause:** A diagnostic script ("Kotlin Project Doctor") injected by the IDE was using a Gradle property removed in Gradle 8.0.
- **Fix:** Added `kotlin.internal.doctor.enabled=false` to `gradle.properties` and mocked the property in `settings.gradle.kts`.

### 2. JDK / Toolchain Mismatches
- **Error:** `Incompatible Gradle JVM version` / `JAVA_HOME not defined`.
- **Cause:** Discrepancies between local environment variables and the IDE's bundled JDK (JDK 25) vs. Gradle's supported versions.
- **Fix:** Implemented **Java Toolchains** in `app/build.gradle.kts` using `jvmToolchain(17)` and the `foojay-resolver-convention` plugin to allow Gradle to automatically manage and download the correct JDK version regardless of the host machine.

### 3. Apple Silicon (M1/M2) Protobuf Support
- **Error:** Could not find `protoc` executable for `aarch_64`.
- **Cause:** Original version (3.10.0/3.14.0) lacked native binaries for Apple Silicon.
- **Fix:** Updated `protoc` to **3.25.1** in the `protobuf` configuration block.

---

## Original Author's Notes on the Solver Algorithm
The following technical details are preserved verbatim from the original repository's README:

> # DS & Algos
> 
> The algorithms in here were written by me. I do not learn well at all from textbooks, so apart from spending a week trying to understand what an Adjacency List was, everything came from my head.
> 
> The only part that I am particularly proud of, is the Sudoku Solver algorithm. In order to make my algorithm more efficient, I decided to borrow a concept I learned from studying UNIX operating systems: Nice Values. What this means, is that as the algorithm attempts to allocate numbers to a puzzle in order to solve it, it will become more or less picky based on such allocations.  It took some time to tweak the values properly, but the end result can be summarized with the following benchmarks for building 101 puzzles:
> **First benchmarks (101 puzzles)**:  
> 2.423313576E9 (4 m 3 s 979 ms to completion)  
> 2.222165776E9 (3 m 42 s 682 ms to completion)  
> 2.002508687E9 (3 m 20 s 624 ms ...)
> 
> **Second benchmarks** after refactoring seed algorithm:  (101 puzzles)  
> 3.526342681E9 (6 m 1 s 89 ms)  
> 3.024547185E9 (5 m 4 s 971 ms)
> 
> **Third Benchmarks** testing with and without nice values (10 puzzles)  
> With:  
> 3.05801502E8  
> 6.14246012E8  
> 3.71489082E8
> 
> Without:  
> Did not complete even after 10 minutes
> 
> **Fourth benchmarks**, niceValue may not go higher than boundary/2 (101 puzzles)  
> 3.639675188E9 (6 m 4 s 229 ms)
> 
> **Fifth benchmarks** niceValue only adjusted after a fairly comprehensive search (boundary *
> boundary) for a suitable allocation 101 puzzles:
> 
> 9 * 9:  
> 3774511.0 (480 ms)  
> 3482333.0 (456 ms)  
> 3840088.0 (468 ms)  
> 3813932.0 (469 ms)  
> 3169410.0 (453 ms)  
> 3908975.0 (484 ms)
> 
> 16 * 16 (all previous benchmarks were for 9 * 9):  
> 9.02626914E8 (1 m 31 s 45 ms)  
> 7.75323967E8 (1 m 20 s 155 ms)  
> 7.06454975E8 (1 m 11 s 838 ms)
