import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Arrays;

// ----------------------------------------------------------------------
// **كلاس مساعد: ImagePanel** - يستخدم للخلفية
// ----------------------------------------------------------------------
class ImagePanel extends JPanel {
    private Image backgroundImage;

    public ImagePanel(String imagePath) {
        try {
            backgroundImage = Toolkit.getDefaultToolkit().getImage(imagePath);
        } catch (Exception e) {
            System.err.println("Could not load background image: " + imagePath);
        }
        setLayout(new GridBagLayout()); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

// ----------------------------------------------------------------------
// **Client.java** - The main client class
// ----------------------------------------------------------------------
public class Client extends JFrame {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 20000;

    // Colors and Fonts
    private static final Color NEW_DARK_GREEN = Color.decode("#254525"); // اللون الأخضر الداكن الجديد
    private static final Color LIGHT_BG = Color.decode("#FFFFFF"); // خلفية اللوحة البيضاء والنصوص
    private static final Color ACCENT_COLOR = NEW_DARK_GREEN.brighter(); // للتباين 

    private static final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 22); 
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 18);

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String currentUsername = null; 
    
    // Panel Management
    private JPanel mainPanel = new JPanel(new CardLayout());
    private static final String LOGIN_PANEL = "Login";
    private static final String PREFERENCE_PANEL = "Preference"; // اللوحة الجديدة
    private static final String RESERVATION_PANEL = "Reservation";

    // Central White Container
    private JPanel whiteContainerPanel = new JPanel(new CardLayout());

    // Login Page Components
    private JTextField usernameField = new JTextField(10); // تصغير حجم مربع النص
    private JPasswordField passwordField = new JPasswordField(10); // تصغير حجم مربع النص
    private JButton connectRegisterButton = new JButton("SIGN UP"); // تغيير اسم الزر

    // Preference Page Components
    private JComboBox<String> sportCombo;
    private JComboBox<String> dayCombo;
    private JButton checkButton = new JButton("CHECK");

    // Reservation Page Components
    private JList<String> fieldList = new JList<>(); 
    private JList<String> timeSlotList = new JList<>(); 
    private JButton reserveButton = new JButton("RESERVE"); 

    // Sizing
    private static final int WINDOW_WIDTH = 1000; 
    private static final int WINDOW_HEIGHT = 700;
    private static final int CARD_WIDTH = 600; 
    private static final int CARD_HEIGHT = 500; // زيادة الحجم العمودي


    public Client() {
        super("Online Reservation System - KSU IT328");
        initializeGUI();
    }

    // Function to apply the professional theme
    private void applyProfessionalTheme(Component component) {
        if (component instanceof JPanel || component instanceof JScrollPane) {
            component.setBackground(LIGHT_BG);
            component.setForeground(NEW_DARK_GREEN);
        } else if (component instanceof JLabel) {
            component.setBackground(LIGHT_BG);
            component.setForeground(NEW_DARK_GREEN);
            ((JLabel) component).setFont(MAIN_FONT); 
        } else if (component instanceof JTextField || component instanceof JPasswordField) {
            // لون أبيض نقي لمربعات النص
            component.setBackground(LIGHT_BG); 
            component.setForeground(NEW_DARK_GREEN);
            component.setFont(MAIN_FONT); 
        } else if (component instanceof JComboBox) {
            component.setBackground(LIGHT_BG);
            component.setForeground(NEW_DARK_GREEN);
            component.setFont(MAIN_FONT); 
        } else if (component instanceof JButton) {
            component.setBackground(ACCENT_COLOR); 
            component.setForeground(LIGHT_BG);
            component.setFont(BUTTON_FONT); 
        } else if (component instanceof JList) {
            // خلفية بيضاء لقائمة الاختيارات
            component.setBackground(LIGHT_BG); 
            component.setForeground(NEW_DARK_GREEN);
            ((JList) component).setSelectionBackground(NEW_DARK_GREEN);
            ((JList) component).setSelectionForeground(LIGHT_BG);
            ((JList) component).setFont(MAIN_FONT); 
        }
    }

    private void traverseAndApplyTheme(Container container) {
        for (Component c : container.getComponents()) {
            applyProfessionalTheme(c);
            if (c instanceof Container) {
                traverseAndApplyTheme((Container) c);
            }
        }
    }

    private void initializeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 1. Setup Background
        ImagePanel backgroundPanel = new ImagePanel("D:\\files maryam\\network\\background.png"); 
        backgroundPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        
        // 2. Setup Central White Container
        whiteContainerPanel.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        whiteContainerPanel.setBackground(LIGHT_BG);
        
        // Add pages to the white container
        whiteContainerPanel.add(createConnectPanel(), LOGIN_PANEL);
        whiteContainerPanel.add(createPreferencePanel(), PREFERENCE_PANEL); // إضافة اللوحة الجديدة
        whiteContainerPanel.add(createReservationPanel(), RESERVATION_PANEL);

        // 3. Center the white container on the background
        backgroundPanel.add(whiteContainerPanel);
        
        this.setContentPane(backgroundPanel);

        SwingUtilities.invokeLater(() -> traverseAndApplyTheme(whiteContainerPanel));

        setReservationControlsEnabled(false);
        setupActionListeners();
        
        pack();
        this.setLocationRelativeTo(null); 
        setVisible(true);
    }
    
    // ----------------------------------------------------------------------
    // --- Page 1: Connect & Register (SIGN UP) ---
    // ----------------------------------------------------------------------
    private JPanel createConnectPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel title = new JLabel("Welcome! Please Sign Up", SwingConstants.CENTER);
        title.setFont(HEADER_FONT.deriveFont(Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(passwordField, gbc);

        panel.add(inputPanel, BorderLayout.CENTER);

        // Connect Button
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(connectRegisterButton, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ----------------------------------------------------------------------
    // --- Page 2: Preference Selection ---
    // ----------------------------------------------------------------------
    private JPanel createPreferencePanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        
        JLabel title = new JLabel("Select Reservation Preferences", SwingConstants.CENTER);
        title.setFont(HEADER_FONT.deriveFont(Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        // Input/Selection Panel (Center)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS)); // ترتيب عمودي
        
        String[] sports = {"Tennis", "Padel", "Football"};
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        sportCombo = new JComboBox<>(sports);
        dayCombo = new JComboBox<>(days);

        // Sport Container (Centered)
        JPanel sportContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        sportContainer.add(new JLabel("Sport Type:"));
        sportContainer.add(sportCombo);
        
        // Day Container (Centered)
        JPanel dayContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        dayContainer.add(new JLabel("Day:"));
        dayContainer.add(dayCombo);
        
        // Add components to the vertical panel
        centerPanel.add(Box.createVerticalStrut(20)); // مسافة فاصلة
        centerPanel.add(sportContainer);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(dayContainer);
        centerPanel.add(Box.createVerticalStrut(20)); 

        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Check Button (South)
        JPanel checkButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        checkButton.setPreferredSize(new Dimension(150, 40)); // تحديد حجم الزر
        checkButtonPanel.add(checkButton);
        panel.add(checkButtonPanel, BorderLayout.SOUTH);
        
        // Action Listener for CHECK button
        checkButton.addActionListener(e -> {
            String sport = (String) sportCombo.getSelectedItem();
            String day = (String) dayCombo.getSelectedItem();
            requestAvailableFields(sport, day);
            switchPanel(RESERVATION_PANEL); // الانتقال لصفحة عرض الملاعب
        });

        return panel;
    }

    // ----------------------------------------------------------------------
    // --- Page 3: New Reservation (Fields & Slots) ---
    // ----------------------------------------------------------------------
    private JPanel createReservationPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header Panel (Logo and Title)
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Available Fields & Slots", SwingConstants.CENTER);
        title.setFont(HEADER_FONT.deriveFont(Font.BOLD, 24));
        
        // **LOGO PLACEMENT**
        JLabel logoPlaceholder = new JLabel(" [LOGO HERE] ", SwingConstants.LEFT);
        logoPlaceholder.setFont(new Font("Arial", Font.BOLD, 12));
        logoPlaceholder.setForeground(NEW_DARK_GREEN);
        logoPlaceholder.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        headerPanel.add(logoPlaceholder, BorderLayout.WEST); 
        headerPanel.add(title, BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Lists Panel: Fields and Time Slots
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        
        // Field List (Left)
        JPanel fieldContainer = new JPanel(new BorderLayout());
        fieldContainer.add(new JLabel("Available Fields (1-5):"), BorderLayout.NORTH);
        fieldContainer.add(new JScrollPane(fieldList), BorderLayout.CENTER);
        listsPanel.add(fieldContainer);
        
        // Time Slot List (Right)
        JPanel slotContainer = new JPanel(new BorderLayout());
        slotContainer.add(new JLabel("Time Slots Status:"), BorderLayout.NORTH);
        slotContainer.add(new JScrollPane(timeSlotList), BorderLayout.CENTER);
        listsPanel.add(slotContainer);
        
        panel.add(listsPanel, BorderLayout.CENTER); 
        panel.add(reserveButton, BorderLayout.SOUTH);

        return panel;
    }
    
    // ----------------------------------------------------------------------
    // --- Logic Implementation ---
    // ----------------------------------------------------------------------
    
    private void switchPanel(String panelName) {
        CardLayout cl = (CardLayout)(whiteContainerPanel.getLayout());
        cl.show(whiteContainerPanel, panelName);
        this.pack(); 
        this.setLocationRelativeTo(null); 
        // نحافظ على حجم النافذة الكلي عند التبديل
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT); 
    }

    private void setReservationControlsEnabled(boolean enabled) {
        if (sportCombo != null) sportCombo.setEnabled(enabled);
        if (dayCombo != null) dayCombo.setEnabled(enabled);
        if (reserveButton != null) reserveButton.setEnabled(enabled);
        if (checkButton != null) checkButton.setEnabled(enabled);
    }

    private void setupActionListeners() {
        
        connectRegisterButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            connectAndRegister(username, password);
        });

        fieldList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && fieldList.getSelectedValue() != null) {
                String selectedField = fieldList.getSelectedValue();
                String sport = (String) sportCombo.getSelectedItem();
                String day = (String) dayCombo.getSelectedItem();
                requestAvailableSlotsForField(sport, day, selectedField);
            }
        });
        
        reserveButton.addActionListener(e -> {
            String selectedSlotStr = timeSlotList.getSelectedValue();
            String selectedFieldStr = fieldList.getSelectedValue();
            
            if (selectedSlotStr == null || selectedFieldStr == null || selectedSlotStr.contains("BOOKED") || selectedSlotStr.contains("Select a field")) {
                showNotification("Error", "Please select an available field AND an available time slot.", JOptionPane.ERROR_MESSAGE, false);
                return;
            }
            
            String time = selectedSlotStr.replaceAll(" \\(Available\\)| \\(BOOKED\\)", "").trim();
            
            try {
                int field = Integer.parseInt(selectedFieldStr.replaceAll("[^0-9]", ""));
                String sport = (String) Objects.requireNonNull(sportCombo.getSelectedItem());
                String day = (String) Objects.requireNonNull(dayCombo.getSelectedItem());
                
                sendReservationRequest(sport, field, day, time);
                
            } catch (Exception ex) {
                showNotification("Parsing Error", "An internal error occurred during reservation processing.", JOptionPane.ERROR_MESSAGE, false);
                System.err.println("Parsing Error: " + ex.getMessage());
            }
        });
    }

    // --- Logic Functions ---
    
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
                // حذف الإشعار بعد تسجيل الدخول بنجاح
                switchPanel(PREFERENCE_PANEL); // الانتقال مباشرة لصفحة التفضيلات
            } else {
                showNotification("Registration Failed", "Username already exists or password incorrect.", JOptionPane.WARNING_MESSAGE, false);
                if (socket != null) socket.close();
            }
        } catch (IOException ex) {
            System.err.println("Connection Error: " + ex.getMessage()); 
            showNotification("Connection Error", "Could not connect to server.", JOptionPane.ERROR_MESSAGE, false);
        }
    }
    
    private void requestAvailableFields(String sport, String day) {
        if (currentUsername == null) return;
        
        try {
            String request = "AVAILABILITY_REQUEST#" + sport + "#" + day;
            out.println(request);
            
            String response = in.readLine();
            
            if (response != null && response.startsWith("AVAILABILITY_RESPONSE#")) {
                String data = response.substring("AVAILABILITY_RESPONSE#".length());
                
                if (data.startsWith("No available")) {
                    fieldList.setListData(new String[]{"No fields available."});
                    timeSlotList.setListData(new String[]{});
                } else {
                    String[] fieldData = data.split("~");
                    fieldList.setListData(Arrays.stream(fieldData).filter(s -> !s.isEmpty()).toArray(String[]::new));
                    timeSlotList.setListData(new String[]{"Select a field above."});
                }
            }
        } catch (IOException ex) {
            showNotification("Communication Error", "Lost connection to server.", JOptionPane.ERROR_MESSAGE, false);
        }
    }
    
    private void requestAvailableSlotsForField(String sport, String day, String fieldDisplay) {
        int fieldNumber = Integer.parseInt(fieldDisplay.replaceAll("[^0-9]", ""));
        
        try {
            String request = String.format("AVAILABILITY_REQUEST#%s#%s#%d", sport, day, fieldNumber);
            out.println(request);
            
            String response = in.readLine();

            if (response != null && response.startsWith("AVAILABILITY_RESPONSE#")) {
                String data = response.substring("AVAILABILITY_RESPONSE#".length());
                
                if (data.startsWith("No available")) {
                    timeSlotList.setListData(new String[]{"Fully booked."});
                } else {
                    String[] slotData = data.split("~");
                    timeSlotList.setListData(Arrays.stream(slotData).filter(s -> !s.isEmpty()).toArray(String[]::new));
                }
            }
        } catch (IOException ex) {
            showNotification("Communication Error", "Lost connection to server.", JOptionPane.ERROR_MESSAGE, false);
        }
    }

    private void sendReservationRequest(String sport, int field, String day, String time) {
        String reserveRequest = String.format("RESERVE#%s#%s#%d#%s#%s", currentUsername, sport, field, time, day);
        try {
            out.println(reserveRequest);
            String response = in.readLine();
            
            if (response != null && response.startsWith("RESERVATION_CONFIRMED")) {
                 String message = "Booking confirmed for Field " + field + " at " + time + ".";
                 showNotification("Confirmed! ✅", message, JOptionPane.INFORMATION_MESSAGE, true); // اختصار الإشعار
                 
                 switchPanel(LOGIN_PANEL);
                 closeConnection(); 
                 resetLoginState(); 
            } else {
                String message = "Booking failed. Slot may be unavailable."; // اختصار الرسالة
                showNotification("Booking Failed", message, JOptionPane.WARNING_MESSAGE, false);
            }
            requestAvailableFields((String) sportCombo.getSelectedItem(), (String) dayCombo.getSelectedItem());
            
        } catch (IOException ex) {
            showNotification("Communication Error", "Lost connection to server.", JOptionPane.ERROR_MESSAGE, false);
        }
    }

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
        fieldList.setListData(new String[]{}); 
        timeSlotList.setListData(new String[]{}); 
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}
