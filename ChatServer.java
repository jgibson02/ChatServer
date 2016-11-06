
/**
 * Created by John Gibson on 10/31/2016.
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer
{

    private static final String HR = "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
            + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550";

    private static final String cow
            = "       \\   ,__,                                                            \n"
            + "        \\  (oo)____                                                        \n"
            + "           (__)    )\\                                                      \n"
            + "              ||--|| *                                                      \n";

    static ConcurrentHashMap<Socket, String> users = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception
    {
        ServerSocket server = new ServerSocket(9001);
        while (true)
        {
            Socket client = server.accept();
            System.out.println(client.getInetAddress() + " has been connected.");
            Thread st = new ClientThread(client);
            st.start();
        }
    }

    private static void printToAll(Socket client, String message)
    {
        for (Socket socket : users.keySet())
            if (!socket.equals(client))
                try
                {
                    new PrintWriter(socket.getOutputStream(), true).println(message);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
    }

    private static class ClientThread extends Thread
    {

        private final Socket client;
        private final Scanner in;
        private final PrintWriter out;

        ClientThread(Socket client) throws IOException
        {
            this.client = client;
            out = new PrintWriter(this.client.getOutputStream(), true);
            in = new Scanner(this.client.getInputStream());
        }

        @Override
        public void run()
        {
            String s;

            //Prints out first to ask users for their username
            out.println("For a list of available commands, type \"/help\"");
            out.println("Choose a username: ");

            //Creates unique username
            boolean validNameChosen = false;
            while (!validNameChosen)
            {
                String nameRequest = in.nextLine();
                if (!users.containsValue(nameRequest))
                {
                    System.out.println(client.getInetAddress() + " has chosen the username " + nameRequest + ".");
                    users.put(client, nameRequest);
                    printToAll(client, nameRequest + " has connected.");
                    out.println();
                    validNameChosen = true;
                }
                else
                    out.print("Username already chosen, please enter another username: ");
            }

            out.println(HR);

            // Print out a list of all the users who are online
            out.println("Users online: ");
            for (String username : users.values())
                out.println(username);

            out.println(HR);

            while (true)
            {
                if (!in.hasNext())
                    break;
                s = in.next();
                switch (s)
                {
                    case "/help":
                        out.println(HR);
                        out.println("/help -- lists all possible commands");
                        out.println("/msg <USERNAME> <MESSAGE> -- sends a message to the specified user");
                        out.println("/cowsay <MESSAGE> -- displays a cow saying a message");
                        out.println("/quit -- disconnects from the server and exits the program");
                        out.println(HR);
                        in.nextLine();
                        System.out.println(s);
                        break;
                    case "/msg":
                        String recipient = in.next();
                        if (users.contains(recipient)) // check to see if the recipient requested is connected
                            for (Socket sock : users.keySet()) // for every socket that's connected
                                if (users.get(sock).equals(recipient)) // if that socket's username is the recipient's
                                    // create a PrintWriter from that socket and send them the message
                                    try
                                    {
                                        new PrintWriter(sock.getOutputStream(), true).println(users.get(client)
                                                + " privately says: " + in.nextLine());
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                        break;
                    case "/cowsay":
                        String output = " ";
                        String message = in.nextLine();
                        // first line of message bubble
                        for (int i = 0; i < message.length() + 2; i++)
                            output += "_";
                        output += "\n";

                        output += "< " + message + " >\n"; // second line of message bubble

                        // third line of message bubble
                        output += " ";
                        for (int i = 0; i < message.length() + 2; i++)
                            output += "-";
                        output += "\n";

                        output += cow; // the cow
                        printToAll(client, users.get(client) + " says: \n" + output);
                        break;
                    case "/quit":
                        System.out.println(users.get(client) + " has disconnected.");
                        printToAll(client, users.get(client) + " has disconnected.");
                        users.remove(client);
                        break;
                    default:
                        printToAll(client, users.get(client) + " says: " + s);
                }
            }
            out.close();
            in.close();
        }
    }
}
