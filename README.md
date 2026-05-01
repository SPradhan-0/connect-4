# Connect 4 Java Swing Client

This is a Java Swing client for the Connect 4 game, implemented according to the design specification.

## Prerequisites

- Java Development Kit (JDK) 11 or higher.

## How to Run

You can run the client using the pre-compiled JAR file:

```bash
java -cp "Connect4Client.jar:json.jar" com.connect4.Connect4Client
```

By default, the client connects to `http://localhost:8080`. You can override this using the `connect4.url` system property:

```bash
java -Dconnect4.url=http://your-server-address:8080 -cp "Connect4Client.jar:json.jar" com.connect4.Connect4Client
```

## Features

- **Username Entry:** Prompts for an alphanumeric username upon startup.
- **Game Grid:** 6x7 grid of buttons for making moves.
- **Server Strategy:** Dropdown to select different server strategies.
- **Real-time Updates:** Game state and board are updated after each move or manually via the Refresh button.
- **Error Handling:** Displays server-side error messages in a dedicated log area.
