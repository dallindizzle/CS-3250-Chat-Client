import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ChatClient extends JFrame {
    private Socket sock;
    private Scanner in;
    private PrintWriter out;

    private ChatClient.ChatReader chatReader;

    private JScrollPane scrollPane;
    private JTextField input;
    private JTextArea textArea;
    private Box box;
    private JButton button;

    private String name;

    public ChatClient(String name) {
        this.name = name;
        this.setTitle("Chat Client: " + this.name);
        this.setLayout(new BorderLayout());
        this.textArea = new JTextArea();
        this.textArea.setEditable(false);
        this.scrollPane = new JScrollPane(this.textArea);
        this.input = new JTextField();

        this.button = new JButton("Disconnect");

        this.box = Box.createHorizontalBox();
        this.box.add(input);
        this.box.add(button);

        this.button.addActionListener(e -> this.disconnect());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });


        this.add(this.scrollPane, "Center");
        this.add(this.box, "South");

        input.addActionListener(e -> {
            this.sendMessage();
        });

        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
    }

    public static void main(String[] args) {
        String name = "Anonymous";
        int port = 4688;

        if (args.length == 1) {
            name = args[0];
        }
        else if (args.length > 2) {
            name = args[0];
            port = Integer.parseInt(args[1]);
        }

        ChatClient chatClient = new ChatClient(name);
        chatClient.setSize(500, 700);
        chatClient.connect(port);
        chatClient.setVisible(true);
    }

    void connect(int port) {
        try {
            this.sock = new Socket("localhost", port);
            this.in = new Scanner(this.sock.getInputStream());
            this.out = new PrintWriter(this.sock.getOutputStream(), true);

            this.chatReader = new ChatClient.ChatReader();
            this.chatReader.start();

            this.out.println("connect " + this.name);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void sendMessage() {
        String message = this.input.getText();
        if (message.equalsIgnoreCase("/q")) {
            this.disconnect();
        } else {
            this.getMessage( "Me: " + message + "\n");
            this.sendMessage(message);
            this.input.setText("");
        }
    }

    public void sendMessage(String var1) {
        this.out.println(var1);
    }

    synchronized void getMessage(String text) {
        this.textArea.append(text);
        this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
    }

    void disconnect() {
        this.out.println("disconnect " + this.name);
        this.chatReader.disconnect();

        try {
            this.sock.close();
        } catch (IOException e) {e.printStackTrace();}

        System.exit(0);
    }

    class ChatReader extends Thread {
        boolean done = false;
        String message;

        public void disconnect() {
            this.done = true;
        }

        @Override
        public void run() {
            while(!this.done) {
                if (ChatClient.this.in.hasNextLine()) {
                    message = ChatClient.this.in.nextLine();
                }else{message = null;}

                if (message != null) {
                    ChatClient.this.getMessage(message + "\n");
                }
            }

        }
    }
}

