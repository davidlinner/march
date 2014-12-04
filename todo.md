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

<--- here we are now
4. tcp/ip socket implementation
-- presentation

5 com setup / proper state initialization
5.1 test for state initialization

6. com shutdown / client removal
6.1 test for shutdown
7 error handling (client disappearance)
7.1 test error handling    

8. js object model implementation
8.1 test for js object model
9. js com implementation (member only)
9.1 tests for js com implementation
10. js network communication

9 web compliant java-js channel binding (socket.io?)
9.1 overall tests with embedded server

tbd.
plugin for common java and js frameworks (spring, backbone, angular, ember)?
