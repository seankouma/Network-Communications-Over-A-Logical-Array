## Network Communications Over a Logical Array

Overview: Allow for commuinication between nodes and a registry over a TCP 
connection. 

# How to Run: 
You must start up a connection between the nodes and the server first. 
This can be done through the command line. We start by running the server on a specific port
the nodes can connect to. In a seperate terminal start a new connection from the 
host you want to run on and specify the port for the server. Once all the nodes 
are connected you can run the setup-overlay command and then start #. Afterwards
the traffic summary will be printed and you can choose to exit the overlay from 
a specific node, or restart the message passing.

# Registry: 
Allows for nodes to be registered in the registry, and creating an overlay for 
the nodes to pass messages between themselves, and wait for a summary of all 
nodes to be returned and displayed by the registry

Our registry begins in its constructor, where it creates a TCP server thread. 
Our TCP server thread waits for connections to be made. When a node connects, 
it is assigned an ID. Once all the nodes are connected, the server waits for 
command line inputs to create an overlay and begin passing messages amongst themselves.
Since we are working with threads, we needed to synchronize the traffic summary,
so no data is overwritten or corrupted by a thread.  

# Node:
Our messaging node extends our node interface, and starts off by connecting
to the port the server is running on. Our handleEvent function has a switch statement 
that decides the protocol to be used. Once the registry specfifies to start passing
messages, our messaging node helper class helps us keep track of the next and 
previous nodes. It tracks information related to the number of messages, a total, and others,
but eventually sends the traffic summary to the registry to be displayed, where it
then deregisters and leaves the overlay if so desired.
