// **Reservation.java**
// يمثل فترة حجز ملعب معينة.

public class Reservation {
    private String sportType; 
    private int fieldNumber; 
    private String day;       
    private String timeSlot;  
    private boolean isAvailable; 
    private String reservedByUsername; 

    // Constructor
    public Reservation(String sportType, int fieldNumber, String day, String timeSlot) {
        this.sportType = sportType;
        this.fieldNumber = fieldNumber;
        this.day = day;
        this.timeSlot = timeSlot;
        this.isAvailable = true; 
        this.reservedByUsername = null;
    }

    // Getters
    public boolean isAvailable() { return isAvailable; }
    public String getSportType() { return sportType; }
    public int getFieldNumber() { return fieldNumber; }
    public String getDay() { return day; }
    public String getTimeSlot() { return timeSlot; }
    
    // Method to reserve the slot (Implementing function 4: Update the availability)
    public void reserve(String username) {
        this.isAvailable = false;
        this.reservedByUsername = username;
    }

    // لطباعة معلومات الفترات المتاحة للعميل (سنستخدم جزءاً منها في الـ GUI)
    public String getDisplayString() {
        return String.format("Field %d - Time: %s", fieldNumber, timeSlot);
    }
    
    // المفتاح الفريد الذي سيستخدمه الخادم لتحديد فترة الحجز
    public String getKey() {
        return String.format("%s_%d_%s_%s", sportType, fieldNumber, day, timeSlot);
    }
}