import java.awt.Color;
    import java.awt.Dimension;
    import java.awt.Font;
    import java.awt.event.ActionEvent;
    import java.awt.event.KeyAdapter;
    import java.awt.event.KeyEvent;
    import java.io.PrintWriter;
    import java.net.Socket;
    import java.util.Scanner;
    import javax.swing.JButton;
    import javax.swing.JFrame;
    import javax.swing.JPanel;
    import javax.swing.JTextArea;
    import javax.swing.JTextField;
    import javax.swing.border.LineBorder;

    /**
     * Client class extends JFrame and creates a GUI and allows the user a 
     * friendly way of contacting a chat room server. The basic outline of the
     * GUI includes a chat area where the user can send messages. A server area
     * where the user receives information from the server and a send button
     * that allows the user to send information with a button press instead
     * of the enter key. The client works on the backend by waiting for
     * server input from a thread while waiting for user input in another.
     *
     * @author James Sharpe
     */
    public class JWSClient extends JFrame
    {
        /**
         * Server IP, Port
         */
        static private final String SERVER_IP = "localhost";
        static private final int SERVER_PORT = 9001;

        /**
         * client connects the user to the server given an ip.
         */
        static private Socket client;
        static private PrintWriter out;
        static private Scanner in;

        /**
         * chatPanel organizes the components of the frame. 
         */
        static private JPanel chatPanel;

        /**
         * serverChat is the panel that appends server output to the client GUI.
         * It is located in the middle of the GUI. The amount of rows serverChat
         * is allowed to display is dictated by int ROW_MAX.
         */
        static private JTextArea serverChat;

        /**
         * Row max dictates how many server lines can be active on the screen at
         * any given time.
         */
        static private final int ROW_MAX = 19;

        /**
         * CHAR_MAX dictates how long a user-sent string can be. This max was
         * calculated solely based on programmer's choice and style of the
         * program.
         */
        static private final int CHAR_MAX = 42;

        /**
         * sendButton allows the user to press a button to send out input to the
         * server in the GUI. It is located and named "Send" in the bottom right
         * of the GUI.
         */
        static private JButton sendButton;

        /**
         * clientChat allows the user to type in messages using their keyboard and
         * send the code to the server. The maximum amount of characters a user
         * can send it dependent on CHAR_MAX. This field is located in the bottom
         * left of the GUI.
         */
        static private JTextField clientChat;

        public JWSClient() throws Exception
        {
            //initialization of connection to server.
            //This is done first to make sure the user can connect.
            client = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new Scanner(client.getInputStream());

            //initialization of GUI components, but not the frame.
            chatPanel = new JPanel();
            serverChat = new JTextArea();
            clientChat = new JTextField();
            sendButton = new JButton("Send");
            LineBorder outline = new LineBorder(Color.BLACK, 1);

            //serverChat settings
            serverChat.setPreferredSize(new Dimension(500, 250));
            serverChat.setBackground(new Color(240, 240, 240));
            serverChat.setEditable(false);
            serverChat.setBorder(outline);
            serverChat.setFont(new Font("Lucida Console", 0, 11));
            chatPanel.add(serverChat);

            //clientChat settings
            clientChat.setPreferredSize(new Dimension(360, 30));
            clientChat.addKeyListener(new KeyAdapter()
            {
                public void keyTyped(KeyEvent e)
                {
                    //limits client character output
                    if (clientChat.getText().length() >= CHAR_MAX)
                        e.consume();
                }

                public void keyPressed(KeyEvent e)
                {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                        clientSendMessage();
                }
            });
            chatPanel.add(clientChat);

            //sendButton settings
            sendButton.setPreferredSize(new Dimension(120, 30));
            sendButton.setFocusable(false);
            sendButton.addActionListener(
                    (ActionEvent e)
                    -> 
                    {
                        clientSendMessage();
            });
            chatPanel.add(sendButton);
        }

        /**
         * Initializes the clientClass's extended JFrame.
         * This is to be
         */
        private void initFrame()
        {
            this.setTitle("Chatroom");
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setSize(510, 325);
            this.setVisible(true);
            this.setAlwaysOnTop(true);
            this.setResizable(false);
            this.add(chatPanel);
        }

        /**
         * Private method that sends the message from the client chat.
         */
        private void clientSendMessage()
        {
            if (serverChat.getLineCount() > ROW_MAX)
                serverChat.setText("");
            out.println(clientChat.getText());
            serverChat.append("<you>: " + clientChat.getText() + "\n");
            clientChat.setText("");
        }

        public static void main(String[] args) throws Exception
        {
            JWSClient gui = new JWSClient();
            gui.initFrame();

            Thread cit = new ClientInputThread(in);
            cit.start();

            boolean tru = true;
            while (tru);

            //Closing Client
            client.close();
            out.close();
            in.close();
        }

        /**
         * ClientInputThread is a thread that takes in information from the server
         * and appends it to the serverChat. When ROW_MAX is reached, the client 
         * thread sleeps for an amount of time and clears the text field.
         */
        private static class ClientInputThread extends Thread
        {

            Scanner in;

            public ClientInputThread(Scanner in)
            {
                this.in = in;
            }

            @Override
            public void run()
            {
                while (true)
                {
                    if (serverChat.getLineCount() > ROW_MAX)
                        try
                        {
                            //Wait a bit before clearing the server text
                            sleep(800);
                            serverChat.setText("");
                        }
                        catch (InterruptedException e)
                        {
                        }

                    serverChat.append(in.nextLine() + "\n");
                }
            }

        }
    }
