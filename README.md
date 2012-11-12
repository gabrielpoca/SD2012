# Distributed Systems
Class work from the year 2012.

## NIOPeer
This is a ring where each peer listens to one port for the mutex and sends it through the other.
Each peer also listens for local connections. Each local connection is trying to acquire the mutex.
If there is no local connection the mutex keeps be sent in the ring.
Each peer is implemented with two threads that are a nio server each.

## DumpIT
This is my personal library for output. The idea is to have a ready to use library where I can dump
stuff to out err or files.