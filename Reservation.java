public class Reservation {

    private String sportType;
    private String courtid;
    private String date;
    private String time;

    // Constructor
    public Reservation(String sportType, String courtid, String date, String time) {
        this.sportType = sportType;
        this.courtid = courtid;
        this.date = date;
        this.time = time;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getSportMessage() {
        if (sportType.equalsIgnoreCase("TENNIS")) {
            return "🎾 Tennis courts are available with clay and hard surfaces.";
        } else if (sportType.equalsIgnoreCase("PADEL")) {
            return "🏓 Padel courts are indoor and have glass walls.";
        } else if (sportType.equalsIgnoreCase("FOOTBALL")) {
            return "⚽ Football fields are 5-a-side and outdoor.";
        } else {
            return "❌ Unknown sport type.";
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
