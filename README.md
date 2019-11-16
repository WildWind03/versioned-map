# Versioned Map
**Versioned Map** is a project that contains VersionedMap interface and its thread-safe implementation called 
ConcurrentVersionedMap.

The implementation has the following features:
* It consumes `O(n)` memory where `n` is a count of `put` operations performed on the map
* `get` operation costs `O(1)` and does not use any locks
* `getByVersion` operation costs `O(log n)` and also does not use any locks
* Multiple `put` operations on the same key are expensive because every `put` 
causes copying of all previously added values with the same key

To run tests, use `./gradlew test` or `./gradlew.bat test` command, depending on the used operating system.