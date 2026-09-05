import java.time.LocalDate;

public class DateTime {
    public static void main(String[] args) {
        LocalDate today = LocalDate.now();

        System.out.println("Today's date: " + today);
        System.out.println("Current year: " + today.getYear());
        System.out.println("Current month: " + today.getMonth());
        System.out.println("Current day: " + today.getDayOfMonth());
    }
}