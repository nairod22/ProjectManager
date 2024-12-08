# Presentation

## Overview of project
### General
ProjectManager is an internet application that allows users to manage projects. 
Users can create, update, and delete projects on a shared server and working together.

### Features
Person can get a list of all current projects, select details of a specific one, add a new one or
delete one.
He can then select a projet to add a task, delete a task or get details of a specific task and modify it. 

__mettre une image du schéma__

## implementation and architecture 
- usage of TCP protocol with multi-threading with help of newVirtualTrheadPerTaskExecutor()
- ensures lightweight and scalable handling of simultaneous client requests.


## Server
Server is the core of the application. It handles client connections, processes requests, and interacts with the database to manage projects.
It is implemented as a multi-threaded server that can handle multiple client connections simultaneously.
It guarantees smooth parallel processing of client requests and ensures database is persistent across server sessions.
Messages et Errors management through Enum

laodDatabase() : Reads, initializes the database from a database.json file and ensures persistence across server sessions.
saveDatabase() : Serializes the current state of the database back to json format into database.json.

call() : Starts a ServerSocket on the specified port, Initializes an ExecutorService to handle client connections,
Continuously listens for client connections., Each client connection is handled by a new virtual thread (ClientHandler), ensuring smooth parallel processing.

run() et class ClientHanlder :Reads client requests, parses and validates commands using Message enums.
Interacts with the database to process commands like ADDPRJ, PROJS, DELPR, etc. Sends appropriate responses to the client and 
handles errors gracefully with Error enums for better debugging

## Client
Client operate through ann interactive console that provides a command-line interface where users input commands to interact with the server.
It displays server responses to guide users through operations and help having a smouth experience.

Client handle the connection to the server, sending requests and receiving responses.
His main function is to parse user input, send it to the server, and display the server's response to the user.
Processing of user input is done through the server. 


## Protocol
## Database

Stores projects, tasks, and notes in a JSON format.
CRUD : Create, Read, Update, Delete operations for projects, tasks, and notes.


## other

GSON Integration
Utilizes GSON for serializing and deserializing JSON data sent to and received from the server.

having a more technical paragrahp ? 

## how to use ? 