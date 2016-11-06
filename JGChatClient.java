import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

/**
 * Created by John Gibson on 10/31/2016.
 */
public class JGChatClient
{

    public static void main(String[] args) throws Exception
    {
        Socket client = new Socket("localhost", 9001);
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        Scanner in = new Scanner(client.getInputStream());
        Scanner keyboard = new Scanner(System.in);

        Thread cot = new ClientOutputThread(out, keyboard);
        Thread cit = new ClientInputThread(in);

        cot.start();
        cit.start();

        System.out.println("For a list of available commands, type \"/help\"");
        System.out.print("Choose a username: ");

        boolean tru = true;
        while (tru) {
        }

        // Closing scanners and streams
        client.close();
        in.close();
        out.close();
    }

    private static class ClientInputThread extends Thread
    {
        Scanner in;

        ClientInputThread(Scanner in)
        {
            this.in = in;
        }

        @Override
        public void run()
        {
            boolean tru = true;
            while (tru)
                System.out.println(in.nextLine());
        }
    }

    private static class ClientOutputThread extends Thread
    {
        PrintWriter out;
        Scanner keyboard;

        ClientOutputThread(PrintWriter out, Scanner keyboard)
        {
            this.out = out;
            this.keyboard = keyboard;
        }

        @Override
        public void run()
        {
            boolean tru = true;
            while (tru) {
                String s = keyboard.nextLine();
                if (s.equals("/quit")) {
                    out.println(s);
                    out.close();
                    keyboard.close();
                    System.exit(0);
                }
                out.println(s);
            }
        }
    }
}