# Arch. Todos
* consider full masterState communication messages
* solve thread synchronization issue


# Agenda
1. message queue/event handling refactoring + proper clock implementation and clock drift error
1.1. com test implementation
2. completion of object model implementation (masterState retrieval / serialization)
2.1 test for object model implementation
3. integration of object model with com 
3.1 mutual update tests

4. tcp/ip socket implementation
-- presentation

5. js object model implementation
5.1 test for js object model
6. js com implementation (replicaName only)
6.1 tests for js com implementation

7 com setup / proper masterState initialization
7.1 test for masterState initialization

<--- here we are now

8. server
8.1 replicaName lifecycle tests
8.2 various error scenario tests

9. datastore connector for files
9.1 tests for data store

10. binding for popular java websocket framework (socket.io?)

11. client binding to websockets

12. client data model layer
12.1 model layer tests

13. examplary integration with (hype) javascript framework

14. make builds available
14.1 revise build systems and project modules

15. make a simple demo application
 
16. write articles
16.1 short problem statement - relevance of fast, optimistic replication with eventual consistency - motivate with MVP/MMP buzz  
16.2 demo application

17. share with valued peers

-- Milestone 2

18. setup a project web space with getting started etc.

19. integrate with more hype frameworks


tbd.
plugin for common java and js mvc frameworks (spring, backbone, angular, ember)?
think of persistence in key-value store (cassandra, redis)

- master - time controlled heartbeat to make sure all queues are emptied
- refactor api arrays  into collections
- server controller
- client refactoring
- client controller
- sparc binding server