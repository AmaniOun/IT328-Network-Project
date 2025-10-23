import java.io.*;
import java.net.*;
import java.util.*;

// **Server.java** - الكلاس الرئيسي للخادم

public class Server {
    // قاعدة بيانات المستخدمين والملاعب
    private static final Map<String, String> USERS = new HashMap<>(); 
    private static final Map<String, Reservation> ALL_SLOTS = new HashMap<>();

    private static final int PORT = 12345; 

    public static void main(String[] args) {
        initializeSlots(); 
        
        System.out.println("Reservation Server is running on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                // بدء معالج عميل جديد (Thread)
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    // تهيئة جميع فترات الحجز المتاحة (كما في الوصف)
    private static void initializeSlots() {
        String[] sports = {"Tennis", "Padel", "Football"};
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] timeSlots = {"07:00-08:00 AM", "08:00-10:00 AM", "10:00-11:00 AM", 
                          "07:00-08:00 PM", "08:00-10:00 PM", "10:00-11:00 PM"}; 

        for (String sport : sports) {
            for (int field = 1; field <= 5; field++) {
                for (String day : days) {
                    for (String time : timeSlots) {
                        Reservation slot = new Reservation(sport, field, day, time);
                        ALL_SLOTS.put(slot.getKey(), slot);
                    }
                }
            }
        }
        System.out.println("Total slots initialized: " + ALL_SLOTS.size());
    }

    // كلاس داخلي لمعالجة طلبات كل عميل على حِدة (Thread)
    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received: " + inputLine);
                    String response = processRequest(inputLine);
                    out.println(response); 
                }
            } catch (IOException e) {
                // قد يكون بسبب إغلاق العميل للاتصال
                System.err.println("Client handler exception (Client disconnected): " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                System.out.println("Client disconnected.");
            }
        }

        private String processRequest(String request) {
            String[] parts = request.split("#"); 
            String command = parts[0];

            switch (command) {
                // 1. الاتصال والتسجيل
                case "REGISTER":
                    if (parts.length < 3) return "ERROR#Invalid registration format";
                    String regUsername = parts[1];
                    String regPassword = parts[2];
                    
                    // استخدام 'synchronized' لمزامنة الوصول إلى HashMap
                    synchronized (USERS) {
                        if (USERS.containsKey(regUsername)) {
                            return "REGISTER_FAILED#Username already exists";
                        }
                        USERS.put(regUsername, regPassword);
                    }
                    return "REGISTER_SUCCESS#You are now connected and registered! Welcome, " + regUsername;
                
                // 2. طلب التوافر (Request Availability)
                case "AVAILABILITY_REQUEST":
            if (parts.length < 3) return "ERROR#Invalid availability request format";
            
            String reqSport = parts[1];
            String reqDay = parts[2];

            // 1. طلب قائمة الملاعب المتاحة (Parts Length = 3)
            if (parts.length == 3) { 
                Set<Integer> availableFields = new HashSet<>();
                
                // البحث عن الملاعب التي تحتوي على أي فترات متاحة في ذلك اليوم وتلك الرياضة
                synchronized (ALL_SLOTS) {
                    for (Reservation slot : ALL_SLOTS.values()) {
                        if (slot.getSportType().equalsIgnoreCase(reqSport) && 
                            slot.getDay().equalsIgnoreCase(reqDay) && 
                            slot.isAvailable()) {
                            availableFields.add(slot.getFieldNumber());
                        }
                    }
                }
                
                if (availableFields.isEmpty()) {
                    return "AVAILABILITY_RESPONSE#No available fields for " + reqSport + " on " + reqDay;
                }
                
                // إرجاع قائمة الملاعب المتاحة (مثلاً: Field 1~Field 3~...)
                StringBuilder sb = new StringBuilder("AVAILABILITY_RESPONSE#");
                availableFields.stream().sorted().forEach(f -> sb.append("Field ").append(f).append("~"));
                return sb.toString();
            } 
            
            // 2. طلب الأوقات المتاحة لملعب محدد (Parts Length = 4)
            else if (parts.length == 4) {
                try {
                    int reqField = Integer.parseInt(parts[3]); // رقم الملعب
                    
                    List<String> availableTimeSlots = new ArrayList<>();
                    
                    // البحث عن جميع الأوقات الستة لهذا الملعب المحدد واليوم والرياضة
                    synchronized (ALL_SLOTS) {
                        for (Reservation slot : ALL_SLOTS.values()) {
                            if (slot.getSportType().equalsIgnoreCase(reqSport) && 
                                slot.getDay().equalsIgnoreCase(reqDay) && 
                                slot.getFieldNumber() == reqField) {
                                
                                String status = slot.isAvailable() ? " (Available)" : " (BOOKED)";
                                availableTimeSlots.add(slot.getTimeSlot() + status);
                            }
                        }
                    }

                    if (availableTimeSlots.isEmpty()) {
                        return "AVAILABILITY_RESPONSE#Field data not found.";
                    }
                    
                    // إرجاع جميع الفترات الست مع حالتها
                    StringBuilder sb = new StringBuilder("AVAILABILITY_RESPONSE#");
                    availableTimeSlots.forEach(s -> sb.append(s).append("~"));
                    return sb.toString();

                } catch (NumberFormatException e) {
                    return "ERROR#Invalid field number format.";
                }
            } else {
                 return "ERROR#Invalid availability request format (Wrong number of parts).";
            }

                // 3 و 4. تأكيد وتحديث الحجز (Confirm and Update)
                case "RESERVE":
                    if (parts.length < 6) return "ERROR#Invalid reservation format";
                    String resUsername = parts[1];
                    String resSport = parts[2];
                    int resField = Integer.parseInt(parts[3]);
                    String resTime = parts[4];
                    String resDay = parts[5];
                    
                    String key = String.format("%s_%d_%s_%s", resSport, resField, resDay, resTime);
                    
                    // استخدام 'synchronized' لضمان أن التحديث والقراءة يتمان كوحدة واحدة (Atomic)
                    synchronized (ALL_SLOTS) {
                        Reservation slotToReserve = ALL_SLOTS.get(key);
                        
                        if (slotToReserve == null) {
                             return "RESERVATION_FAILED#Invalid slot details provided.";
                        }
                        
                        if (slotToReserve.isAvailable()) {
                            slotToReserve.reserve(resUsername); // تحديث التوافر وجعلها غير متاحة
                            // الوظيفة 3: إرسال رسالة التأكيد
                            return "RESERVATION_CONFIRMED#Successfully reserved Field " + resField + " on " + resDay + " at " + resTime + ". Confirmation Sent."; 
                        } else {
                            return "RESERVATION_FAILED#Slot is no longer available.";
                        }
                    }
                default:
                    return "ERROR#Unknown command";
            }
        }
    }
}
