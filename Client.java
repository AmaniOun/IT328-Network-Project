import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Arrays;

// **Client.java** - Finalized Client with English UI, improved sizing, and bug fixes

public class Client extends JFrame {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    // Required Colors (Inverted Theme: Light BG, Dark FG)
    private static final Color DARK_BG = Color.decode("#0C1C2B"); // Text, Buttons, Component BG
    private static final Color LIGHT_FG = Color.decode("#9AB5C9"); // Main Screen BG
    private static final Color ACCENT_COLOR = Color.decode("#0C1C2B").brighter(); 

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String currentUsername = null; 

    // Panel Management
    private JPanel mainPanel = new JPanel(new CardLayout());
    private static final String LOGIN_PANEL = "Login";
    private static final String RESERVATION_PANEL = "Reservation";
    
    // Font Settings
    private static final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);

    // Login Page Components
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton connectRegisterButton = new JButton("Connect & Register");

    // Reservation Page Components
    private JComboBox<String> sportCombo;
    private JComboBox<String> dayCombo;
    private JList<String> availabilityList = new JList<>();
    private JButton reserveButton = new JButton("Reserve Selected Slot");
    
    // Sizing
    private static final int INITIAL_WIDTH = 750; 
    private static final int INITIAL_HEIGHT = 550; 


    public Client() {
        super("Online Reservation System - KSU IT328");
        this.getContentPane().setBackground(LIGHT_FG); 
        initializeGUI();
    }

    // Function to apply the inverted theme and font sizes
    private void applyLightInverseTheme(Component component) {
        if (component instanceof JPanel || component instanceof JScrollPane) {
            component.setBackground(LIGHT_FG);
            component.setForeground(DARK_BG);
        } else if (component instanceof JLabel) {
            component.setBackground(LIGHT_FG);
            component.setForeground(DARK_BG);
            ((JLabel) component).setFont(MAIN_FONT); 
        } else if (component instanceof JTextField || component instanceof JPasswordField || component instanceof JComboBox) {
            component.setBackground(LIGHT_FG.darker()); 
            component.setForeground(DARK_BG);
            component.setFont(MAIN_FONT); 
        } else if (component instanceof JButton) {
            component.setBackground(ACCENT_COLOR); 
            component.setForeground(LIGHT_FG);
            component.setFont(HEADER_FONT); 
        } else if (component instanceof JList) {
            component.setBackground(LIGHT_FG.darker());
            component.setForeground(DARK_BG);
            ((JList) component).setSelectionBackground(DARK_BG);
            ((JList) component).setSelectionForeground(LIGHT_FG);
            ((JList) component).setFont(MAIN_FONT); 
        }
    }

    private void traverseAndApplyTheme(Container container) {
        for (Component c : container.getComponents()) {
            applyLightInverseTheme(c);
            if (c instanceof Container) {
                traverseAndApplyTheme((Container) c);
            }
        }
    }

    private void initializeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Add Panels
        mainPanel.add(createConnectPanel(), LOGIN_PANEL);
        mainPanel.add(createReservationPanel(), RESERVATION_PANEL);
        
        add(mainPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> traverseAndApplyTheme(this.getContentPane()));

        setReservationControlsEnabled(false);
        setupActionListeners();
        
        pack();
        // Set size and center the window
        this.setSize(INITIAL_WIDTH, INITIAL_HEIGHT); 
        this.setMinimumSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT)); 
        this.setLocationRelativeTo(null); // Centers the window
        setVisible(true);
    }

    // ----------------------------------------------------------------------
    // --- Page 1: Connect & Register ---
    // ----------------------------------------------------------------------
    private JPanel createConnectPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; 

        // Label for Username
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Username:"), gbc);
        // Field for Username
        gbc.gridx = 1; gbc.gridy = 0; panel.add(usernameField, gbc);

        // Label for Password
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Password:"), gbc);
        // Field for Password
        gbc.gridx = 1; gbc.gridy = 1; panel.add(passwordField, gbc);

        // Connect Button 
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(30, 15, 15, 15); 
        panel.add(connectRegisterButton, gbc);

        return panel;
    }

    // ----------------------------------------------------------------------
    // --- Page 2: New Reservation ---
    // ----------------------------------------------------------------------
    private JPanel createReservationPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15)); 
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel topContainer = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new GridLayout(1, 4, 15, 15)); 
        
        String[] sports = {"Tennis", "Padel", "Football"};
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        sportCombo = new JComboBox<>(sports);
        dayCombo = new JComboBox<>(days);
        
        filterPanel.add(new JLabel("Sport:"));
        filterPanel.add(sportCombo);
        filterPanel.add(new JLabel("Day:"));
        filterPanel.add(dayCombo);
        
        JButton checkAvailabilityButton = new JButton("Check Availability");
        checkAvailabilityButton.addActionListener(e -> {
            String sport = (String) sportCombo.getSelectedItem();
            String day = (String) dayCombo.getSelectedItem();
            requestAvailability(sport, day);
        });

        topContainer.add(filterPanel, BorderLayout.NORTH);
        topContainer.add(checkAvailabilityButton, BorderLayout.SOUTH);

        panel.add(topContainer, BorderLayout.NORTH);
        
        panel.add(new JScrollPane(availabilityList), BorderLayout.CENTER); 
        panel.add(reserveButton, BorderLayout.SOUTH);

        return panel;
    }
    
    // ----------------------------------------------------------------------
    // --- Action Listeners & Logic ---
    // ----------------------------------------------------------------------
    
    private void switchPanel(String panelName) {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());
        cl.show(mainPanel, panelName);
        this.pack(); 
        this.setSize(this.getWidth(), INITIAL_HEIGHT); 
    }

    private void setReservationControlsEnabled(boolean enabled) {
        sportCombo.setEnabled(enabled);
        dayCombo.setEnabled(enabled);
        reserveButton.setEnabled(enabled);
    }

    private void setupActionListeners() {
        
        connectRegisterButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            connectAndRegister(username, password);
        });

        reserveButton.addActionListener(e -> {
            String selectedSlotStr = availabilityList.getSelectedValue();
            
            if (selectedSlotStr == null || selectedSlotStr.contains("No slots available")) {
                showNotification("Error", "Please select a valid slot to reserve.", JOptionPane.ERROR_MESSAGE, false);
                return;
            }
            
            int field = 0;
            String time = "";
            
            try {
                int fieldStart = selectedSlotStr.indexOf("Field ") + 6;
                int fieldEnd = selectedSlotStr.indexOf(" -");
                field = Integer.parseInt(selectedSlotStr.substring(fieldStart, fieldEnd).trim());
                
                int timeStart = selectedSlotStr.indexOf("Time: ") + 6;
                time = selectedSlotStr.substring(timeStart).trim();

                if (field <= 0 || time.isEmpty()) {
                    throw new Exception("Extracted reservation details are incomplete or invalid.");
                }
                    
                String sport = (String) Objects.requireNonNull(sportCombo.getSelectedItem());
                String day = (String) Objects.requireNonNull(dayCombo.getSelectedItem());
                
                sendReservationRequest(sport, field, day, time);
                
            } catch (Exception ex) {
                showNotification("Parsing Error", "An internal error occurred during reservation processing. Please check server status.", JOptionPane.ERROR_MESSAGE, false);
                System.err.println("Parsing Error: " + ex.getMessage());
            }
        });
    }

    // -- Connect and Register Function --
    private void connectAndRegister(String username, String password) {
        if (socket != null && !socket.isClosed()) return;
        
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String request = "REGISTER#" + username + "#" + password;
            out.println(request);
            
            String response = in.readLine();
            
            if (response != null && response.startsWith("REGISTER_SUCCESS")) {
                currentUsername = username;
                connectRegisterButton.setEnabled(false);
                usernameField.setEditable(false);
                passwordField.setEditable(false);
                setReservationControlsEnabled(true);
                showNotification("Connection Success", response.substring(response.indexOf("#") + 1), JOptionPane.INFORMATION_MESSAGE, false);
                switchPanel(RESERVATION_PANEL); 
            } else {
                showNotification("Registration Failed", response != null ? response.substring(response.indexOf("#") + 1) : "Server did not respond.", JOptionPane.WARNING_MESSAGE, false);
                if (socket != null) socket.close();
            }
        } catch (IOException ex) {
            // Error notification is suppressed here to prevent the unwanted pop-up when returning to the login page.
            System.err.println("Connection Error: " + ex.getMessage()); 
        }
    }
    
    // -- Send Reservation Request --
    private void sendReservationRequest(String sport, int field, String day, String time) {
        String reserveRequest = String.format("RESERVE#%s#%s#%d#%s#%s", currentUsername, sport, field, time, day);
        try {
            out.println(reserveRequest);
            String response = in.readLine();
            
            if (response != null && response.startsWith("RESERVATION_CONFIRMED")) {
                 String message = response.substring(response.indexOf("#") + 1);
                 showNotification("Reservation Confirmed! 🎉", message + "\n\nPress OK to return to the Connect screen.", JOptionPane.INFORMATION_MESSAGE, true);
                 
                 switchPanel(LOGIN_PANEL);
                 closeConnection(); 
                 resetLoginState(); 
            } else {
                String message = response != null ? response.substring(response.indexOf("#") + 1) : "Reservation failed. Server error.";
                showNotification("Reservation Failed", message, JOptionPane.WARNING_MESSAGE, false);
            }
            requestAvailability((String) sportCombo.getSelectedItem(), (String) dayCombo.getSelectedItem());
            
        } catch (IOException ex) {
            showNotification("Communication Error", "Lost connection to server.", JOptionPane.ERROR_MESSAGE, false);
        }
    }

    // -- Pop-up Notification System --
    private void showNotification(String title, String message, int messageType, boolean promptForReturn) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    private void closeConnection() {
         try {
             if (socket != null && !socket.isClosed()) {
                 socket.close();
             }
         } catch (IOException e) {
             System.err.println("Error closing connection: " + e.getMessage());
         }
    }
    
    private void resetLoginState() {
        currentUsername = null;
        usernameField.setText("");
        passwordField.setText("");
        usernameField.setEditable(true);
        passwordField.setEditable(true);
        connectRegisterButton.setEnabled(true);
        setReservationControlsEnabled(false);
        availabilityList.setListData(new String[]{}); 
    }
    
    // -- Request Availability Function --
    private void requestAvailability(String sport, String day) {
        // Fix: Returning immediately if not connected prevents the unwanted pop-up notification.
        if (currentUsername == null) return; 

        try {
            String request = "AVAILABILITY_REQUEST#" + sport + "#" + day;
            out.println(request);
            
            String response = in.readLine();
            
            if (response != null && response.startsWith("AVAILABILITY_RESPONSE#")) {
                String data = response.substring("AVAILABILITY_RESPONSE#".length());
                
                if (data.startsWith("No available")) {
                    availabilityList.setListData(new String[]{"No slots available for this search."});
                } else {
                    String[] availableSlotsArray = data.split("~");
                    availabilityList.setListData(Arrays.stream(availableSlotsArray).filter(s -> !s.isEmpty()).toArray(String[]::new));
                }
            } else {
                 showNotification("Error", "Failed to get availability from server.", JOptionPane.ERROR_MESSAGE, false);
            }
        } catch (IOException ex) {
            showNotification("Communication Error", "Lost connection to server.", JOptionPane.ERROR_MESSAGE, false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}
