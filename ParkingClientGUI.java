import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ParkingClientGUI extends JFrame {
    private Socket socket; // Socket to manage the connection with the server.
    private BufferedReader bufferedReader; // Buffered reader to read messages from the server.
    private PrintWriter printWriter; // Print writer to send messages to the server.

    private JTextArea textArea; // Text area for displaying messages from the server.
    private JButton connectButton; // Button to initiate connection to the server.
    private JTextField inputField; // Text field for user to enter parking spot to reserve.
    private JButton sendButton; // Button to send the reservation request to the server.

    private static final String SERVER_IP = "localhost"; // IP address of the server.
    private static final int SERVER_PORT = 12345; // Port number the server is listening on.

    public ParkingClientGUI() {
        setTitle("Parking Reservation Client"); // Sets the title of the window.
        setSize(400, 300); // Sets the size of the window.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensures the program exits when window is closed.
        setLayout(new BorderLayout()); // Uses BorderLayout manager for layout.

        textArea = new JTextArea(); // Creates the text area for server messages.
        textArea.setEditable(false); // Makes the text area non-editable.
        add(new JScrollPane(textArea), BorderLayout.CENTER); // Adds a scroll pane around the text area.

        inputField = new JTextField(); // Creates the text field for user input.
        sendButton = new JButton("Send"); // Creates the send button.
        sendButton.addActionListener(e -> sendInput()); // Adds an action listener to the send button.
        JPanel inputPanel = new JPanel(new BorderLayout()); // Creates a panel with BorderLayout for input area.
        inputPanel.add(inputField, BorderLayout.CENTER); // Adds the input field to the center.
        inputPanel.add(sendButton, BorderLayout.EAST); // Adds the send button to the east.

        connectButton = new JButton("Connect"); // Creates the connect button.
        connectButton.addActionListener(e -> connectToServer()); // Adds an action listener to connect to the server.
        add(connectButton, BorderLayout.NORTH); // Adds the connect button to the north of the layout.

        add(inputPanel, BorderLayout.SOUTH); // Adds the input panel to the south of the layout.

        setVisible(true); // Makes the window visible.
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT); // Tries to establish a connection to the server.
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Initializes the reader to receive data from the server.
            printWriter = new PrintWriter(socket.getOutputStream(), true); // Initializes the writer to send data to the server, with auto flush on.
            textArea.append("Connected to server.\n"); // Displays connection success in the text area.
            connectButton.setEnabled(false); // Disables the connect button after connection is established.
            new Thread(this::listenToServer).start(); // Starts a new thread to listen to messages from the server.
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to the server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE); // Shows an error dialog if connection fails.
        }
    }

    private void listenToServer() {
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) { // Continuously reads lines from the server.
                String response = "Server: " + line + "\n"; // Prepends "Server:" to the received message.
                SwingUtilities.invokeLater(() -> textArea.append(response)); // Ensures the GUI update is done on the Swing event thread.
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> textArea.append("Lost connection to server.\n")); // Displays message if connection is lost.
        } finally {
            try {
                socket.close(); // Tries to close the socket.
                connectButton.setEnabled(true); // Re-enables the connect button.
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> textArea.append("Error closing socket.\n")); // Displays socket closing error.
            }
        }
    }

    private void sendInput() {
        String input = inputField.getText().trim(); // Gets and trims the user input from the text field.
        printWriter.println(input); // Sends the input to the server.
        inputField.setText(""); // Clears the input field after sending.
    }

    public static void main(String[] args) {
        new ParkingClientGUI(); // Creates and shows the GUI.
    }
}
