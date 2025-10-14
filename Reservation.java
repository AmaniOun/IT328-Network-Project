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

    public class SportSelector {

    public static String chooseSportType() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("اختر نوع الرياضة:");
        System.out.println("1. Tennis 🎾");
        System.out.println("2. Padel 🏓");
        System.out.println("3. Football ⚽");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                return "TENNIS";
            case 2:
                return "PADEL";
            case 3:
                return "FOOTBALL";
            default:
                System.out.println("❌ اختيار غير صالح. سيتم اختيار Tennis تلقائيًا.");
                return "TENNIS";
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
