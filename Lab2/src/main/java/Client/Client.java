package Client;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private String host;
    private int port;
    static Logger logger = Logger.getLogger(Client.class);

    public static void main(String[] args) throws UnknownHostException, IOException {
        String command;
        /*
        Если клиент не подключен к серверу, то доступны только три команды:
        connect <host> <port> - подключение к серверу;
        quit - завершение работы программы;
        help - вызывает справку с поддерживаемыми командами для клиент-серверного приложения

         */
        try{

            while (true) {
                System.out.print("EchoClient> ");
                Scanner sc = new Scanner(System.in);

                while (sc.hasNextLine()) {
                    command = sc.nextLine(); // Считываение команды введённой в команндной строке
                    if (command.contains("connect") || command.contains("c/")) {
                        connect(command);
                    } else if (command.contains("quit") || command.contains("q/")) {
                        quit(command);
                    } else
                        help();
                }
        }
        } catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws UnknownHostException, IOException {


        Socket client = new Socket(host, port);  // подключить клиента к серверу
        logger.info("Socket is created");
        System.out.println("Connection to MSRG Echo server established: " + host + ":" + port);
        logger.info("Connection to MSRG Echo server established");

        PrintStream output = new PrintStream(client.getOutputStream()); // Получение выходного потока Socket
        // (куда клиент отправляет свое сообщение)


        new Thread(new ReceivedMessagesHandler(client.getInputStream())).start(); // Создание нового потока для
        // получения(обработки) сообщений от сервера

        // переменная "буфер" отвечает за сообщение(команды) введённые в консоли
        String command;

        Scanner sc = new Scanner(System.in);
        System.out.print("EchoClient> ");
        while (sc.hasNextLine()) {
            System.out.print("EchoClient> ");
            command = sc.nextLine();
            if (command.contains("send") || command.contains("s/")) {
                if (command.contains(" ")) {
                    if (!client.isClosed()) {
                        int firstSpace = command.indexOf(" ");
                        String message = command.substring(firstSpace + 1, command.length());

                        output.println(message); //отправка сообщения серверу

                        logger.info("Get message to server");
                    } else
                        System.out.println("Error! Not connected!");
                    logger.warn("Don't connected to server");
                } else
                    wrongСommand();
            } else if (command.contains("disconnect") || command.contains("d/")) {
                System.out.println("Connection terminated: " + host + ":" +  port);
                logger.info("Client " + host + " /" + port + " is disconnected");
                client.close();
                output.close();
            } else if (command.contains("quit") || command.contains("q/")) {
                quit(command, output, client);

            } else if (command.contains("connect") || command.contains("c/")) {
                connect(command);
            } else if (command.contains("help") || command.contains("h/")) {
                help();
            } else if (command.contains("logLevel") || command.contains("l/")) {
                logLevel(command);
            } else
                wrongСommand();
        }
        output.close();
        sc.close();
        client.close();

    }


    public static void connect(String command) throws IOException {
        try {
            if (command.contains(" ")) {
                int firstSpace = command.indexOf(" ");
                int secondSpace = command.indexOf(" ", firstSpace + 1);
                if (secondSpace > -1) {
                    String host = command.substring(firstSpace + 1, secondSpace);
                    String _port = command.substring(secondSpace + 1, command.length());

                    int port = Integer.parseInt(_port);

                    new Client(host, port).run(); //запуск потока

                } else {
                    System.out.println("Error! Not connected!");
                    logger.error("Error! Not connected");
                }
            } else {
                wrongСommand();
            }
        } catch (IOException exception) {
            System.out.println("Error! Not connected! \n");
            logger.error("Error! Not connected: " + exception + "\n");
        }

    }

    public static void help() {
        System.out.println("\n" +
                "c/" + " or connect <address> <port>       Пытается установить TCP соединение с эхо-сервером на основе заданного адреса сервера и номера порта эхо-службы. \n" +
                "d/" + " or disconnect             Пытается отключиться от подключенного сервера. \n" +
                "s/" + " or send <message>         Отправляет текстовое сообщение на эхо-сервер в соответствии с протоколом связи. \n" +
                "l/" + " or logLevel <level>       Устанавливает логгер на указанный уровень логирования (all | debug | info | warn | error | fatal | off) \n" +
                "q/" + " or  quit       Разрывает активное соединение с сервером и завершает выполнение программы \n" +
                "h/" + " or help       Справка \n");
    }

    public static void wrongСommand() {
        System.out.println("WRONG COMMAND!!!. Enter \"help\" for reference");
        logger.error("WRONG COMMAND!");
    }

    public static void logLevel(String command) {
        if (command.contains(" ")) {
            int firstSpace = command.indexOf(" ");
            String level = command.substring(firstSpace + 1, command.length());
            //(ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF)
            if ("all".equals(level)) {
                logger.setLevel(Level.ALL);

                currentLoggerLevel(logger.getLevel());
            } else if ("info".equals(level)) {
                logger.setLevel(Level.INFO);

                currentLoggerLevel(logger.getLevel());
            } else if ("debug".equals(level)) {
                logger.setLevel(Level.DEBUG);

                currentLoggerLevel(logger.getLevel());
            } else if ("warn".equals(level)) {
                logger.setLevel(Level.WARN);

                currentLoggerLevel(logger.getLevel());
            } else if ("error".equals(level)) {
                logger.setLevel(Level.ERROR);

                currentLoggerLevel(logger.getLevel());
            } else if ("fatal".equals(level)) {
                logger.setLevel(Level.FATAL);

                currentLoggerLevel(logger.getLevel());
            } else if ("OFF".equals(level)) {
                logger.setLevel(Level.OFF);

                currentLoggerLevel(logger.getLevel());
            }
        } else {
            System.out.println("Error, write level!");
            logger.error("Wrong enter command logLevel");
        }
    }

    public static void quit(String command, PrintStream output, Socket client) throws IOException {

        System.out.println("Application exit!");
        logger.info("Client is exited");
        output.close();
        client.close();
        System.exit(0);

    }

    public static void quit(String command) throws IOException {

        System.out.println("Application exit!");
        logger.info("Client is exited");
        System.exit(0);
    }

    public static void currentLoggerLevel(Level level) {
        System.out.println("Current logging level: " + level);
    }
}

class ReceivedMessagesHandler implements Runnable {

    private InputStream server;

    public ReceivedMessagesHandler(InputStream server) {
        this.server = server;
    }

    public void run() {
        // Если приходит сообщение от сервера , то выводит его в консоль
        Scanner s = new Scanner(server);
        String tmp = "";
        while (s.hasNextLine()) {
            tmp = s.nextLine();
            System.out.println(tmp);
            if (tmp.charAt(0) == '[') {
                tmp = tmp.substring(1, tmp.length() - 1);
            } else {
                try {
                    System.out.println(getTagValue(tmp));
                } catch (Exception ignore) {
                }
            }
        }
        s.close();
    }

    public static String getTagValue(String xml) {
        return xml.split("<span>")[1].split("</span>")[0];
    }

}
