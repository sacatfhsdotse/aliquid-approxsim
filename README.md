stratmas V7.6
=======================

An attempt to improve Stratmas V7.5, a country level military/disaster simulator, originally written in 2006 by Swedish National Defence College.

The major new additions are graphs for simulating infrastructure such as roads, electricity powerlines and water pipes. Unfortunately the old version was bareley usable and a lot of effort has been spent at general bug fixing and user interface improvements.

### Building the project
All parts should work on Windows, Linux and Mac but is not guaranteed.

The *client* is written in java and uses Apache Ant as a build system. Simply go into the client directory and run 'ant'.

The *server* is written in c++ and uses CMake as a build system. Go into the server directory and read the README in there for detailed instructions on how to compile.

The *dispatcher* is only used to share a large amount of servers with multiple clients. Simply go into the dispatcher directory and run 'ant'.
