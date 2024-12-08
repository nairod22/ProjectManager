# Project manager

This is a small cli Client / Server app to manage tasks in projects.

The features include:
- Creating / deleting projects
- Creating / deleting tasks

This app uses a custom protocol to communicate between the client and the server. The protocol is described in the `protocol_doc.md` file.

## Usage

There are 3 ways to use this app:
- Build from source
- Clone the repo and run the docker file
- Use the docker image from github packages

### Build from source
First clone this repo on your machine and `cd` in the cloned folder:
```bash
git clone git@github.com:nairod22/ProjectManager.git
cd ProjectManager
```
You need to have [Maven](https://maven.apache.org/) and [SdkMan](https://sdkman.io/) installed on your system.

Then, you need to download the project dependencies with the following command:
```bash
./mvnw dependency:go-offline
```

To build, you have two options:
- If you have [IntelliJ](https://www.jetbrains.com/idea/) installed, you can simply launch the project with it and, in the top right corner, select the `Package application as JAR file` option and run it.
- If you want to do it from the terminal, you can run the following command:
```bash
./mvnw package
```

Both options will generate a `.jar` archive in the `target` directory. To run the application, run:
```
java -jar target/java-tcp-programming-1.0-SNAPSHOT.jar
```
and add the options you want.

### Run from local docker image

First clone this repo on your machine and `cd` in the cloned folder:
```bash
git clone git@github.com:nairod22/ProjectManager.git
cd ProjectManager
```

Then, you need to build the docker image:
```bash
docker build -t project-manager .
```

Finally, you can run the docker image:
```bash
docker run -it project-manager
```

### Use the docker image from github packages

You can also use the docker image from the github packages. To do so, you need to pull the image:
```bash
docker pull ghcr.io/nairod22/project-manager-docker:latest```

And then run it:
```bash
docker run -it ghcr.io/nairod22/project-manager-docker:latest
```

## How to use

The app is in two parts. The client and the server. You need to run the server first and then the client. For the server, you need to know it's IP address and the port you want to use. The default port is `6433`.

Here is the general help message:
```bash
A small game to experiment with TCP.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  client  Start the client part of the network game.
  server  Start the server part of the network game.
```

### Server

Here is the server's help message:
```bash
Usage: java-tcp-programming-1.0-SNAPSHOT.jar server [-hV] [-p=<port>]
Start the server part of the network game.
  -h, --help          Show this help message and exit.
  -p, --port=<port>   Port to use (default: 6433).
  -V, --version       Print version information and exit.
```

To run the server, you need to run the following command:
```bash
java -jar target/java-tcp-programming-1.0-SNAPSHOT.jar server <port>
```

or

```bash
docker run -it project-manager server <port>
```

The port is optional and will default to `6433`.

### Client

Here is the client's help message:
```bash
Usage: java-tcp-programming-1.0-SNAPSHOT.jar client [-hV] -H=<host> [-p=<port>]
Start the client part of the network game.
  -h, --help          Show this help message and exit.
  -H, --host=<host>   Host to connect to.
  -p, --port=<port>   Port to use (default: 6433).
  -V, --version       Print version information and exit.
```

To run the client, you need to run the following command:
```bash
java -jar target/java-tcp-programming-1.0-SNAPSHOT.jar client <host> <port>
```

or

```bash
docker run -it project-manager client <host> <port>
```

The port is optional and will default to `6433`.


Now you are in! You can start creating projects and tasks.
Follow the instructions on the screen to navigate through the app. One thing you need to know, is that for all actions that may require an argument like a name or some information, you first need to type the command and then, you will be prompted to enter the argument.

### Example usage

```bash
java -jar target/java-tcp-programming-1.0-SNAPSHOT.jar client -H=localhost -p=6433
List of all projects:
1. test1
2. test2
3. test1
4. test2

Options:
[1-N]: Choose a project by number
[Name]: Choose a project by name
[add]: Add a new project
[delete]: Delete a project by number or name
> 1

Tasks in project: test1
No tasks available.

Options:
[add]: Add a new task
[delete]: Delete a task
[modify]: Modify a task
[back]: Go back to project menu
> add
Enter the task name: DAI practical work 2
Enter the priority (low/medium/high) or leave empty: high
Enter the due date (YYYY-MM-DD) or leave empty: 2024-12-08
[Client]  Receive OKAYY

Tasks in project: test1
- DAI practical work 2: DAI practical work 2 : Due on the 2024-12-08 - priority HIGH

Options:
[add]: Add a new task
[delete]: Delete a task
[modify]: Modify a task
[back]: Go back to project menu
>
```

## Libraries
The project tries to stay the closest to `java` and `javax` and use few external dependencies. The only external dependency used, are [picocli](https://picocli.info/) and [Gson](https://google.github.io/gson/).
