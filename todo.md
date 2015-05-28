# Arch. Todos
* consider full state communication messages
* solve thread synchronization issue


# Agenda
1. message queue/event handling refactoring + proper clock implementation and clock drift error
1.1. com test implementation
2. completion of object model implementation (state retrieval / serialization)
2.1 test for object model implementation
3. integration of object model with com 
3.1 mutual update tests

4. tcp/ip socket implementation
-- presentation

5. js object model implementation
5.1 test for js object model
6. js com implementation (member only)
6.1 tests for js com implementation

7 com setup / proper state initialization
7.1 test for state initialization

<--- here we are now

8. obsolete

9. com shutdown / client removal
9.1 test for shutdown
9.2 add life-cycle methods to endpoints
10 error handling (client disappearance)
10.1 test error handling    

11 web compliant java-js channel binding (socket.io?)
11.1 overall tests with embedded server (heroku)

tbd.
plugin for common java and js mvc frameworks (spring, backbone, angular, ember)?
think of persistence in key-value store (cassandra, redis)