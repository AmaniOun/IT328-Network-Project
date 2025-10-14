public class Reservation {
    private String username;
    private String sportType;
    private String fieldName;
    private String day;
    private String time;

    // Constructor
    public Reservation(String username, String sportType, String fieldName, String day, String time) {
        this.username = username;
        this.sportType = sportType;
        this.fieldName = fieldName;
        this.day = day;
        this.time = time;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getSportType() {
        return sportType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    // Method to get a readable summary of the reservation
    public String getReservationInfo() {
        return "User: " + username + " | Sport: " + sportType + " | Field: " + fieldName +
               " | Day: " + day + " | Time: " + time;
    }

    // Method to check if this reservation matches a given slot
    public boolean matches(String sport, String day, String time) {
        return this.sportType.equalsIgnoreCase(sport) &&
               this.day.equalsIgnoreCase(day) &&
               this.time.equalsIgnoreCase(time);
    }
}
