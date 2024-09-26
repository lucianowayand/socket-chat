# Instant Messaging Application 
This project is a socket-based instant messaging application implemented in Java. It includes two main components: a **Server**  and a **Client** . The server facilitates communication between clients, allowing them to exchange text messages and files directly.
## Features 
 
- **Client-Server Architecture** : All communication between clients is routed through the server.
 
- **Private Messaging** : Clients can send messages or files to other connected clients.
 
- **Connected Clients List** : Users can list all active clients via the `/users` command.
 
- **Text Messaging** : Clients can send direct messages using the `/send message <recipient> <message>` command.
 
- **File Transfer** : Clients can send files to each other using the `/send file <recipient> <file path>` command.
 
- **Client Disconnect** : Clients can exit the application at any time using the `/sair` command.
 
- **Server Logging** : The server logs all client connections, including their IP address, date, and time of connection.

## Prerequisites 

- Java 17 or higher must be installed.

- A terminal or command prompt to run the application.

## Commands 
 
- `/users`: Lists all currently connected clients.
 
- `/send message <recipient> <message>`: Sends a text message to the specified recipient.
 
- `/send file <recipient> <file path>`: Sends a file to the specified recipient.
 
- `/sair`: Disconnects the client from the server.

## Server Logs 
The server automatically records logs for each client connection in a file named `server-log.txt`. Each log entry contains the following details:
- Client IP address
- Connection date and time

## License 

This project is for academic purposes and not intended for commercial use.

