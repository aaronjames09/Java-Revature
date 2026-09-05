import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class DaysCalc {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        System.out.print("Enter your birthday: ");
        String date = scan.nextLine();
        LocalDate birthday = LocalDate.parse(date);
        LocalDate today = LocalDate.now();
        LocalDate nextBirthday = LocalDate.of(
                today.getYear(),
                birthday.getMonthValue(),
                birthday.getDayOfMonth()
        );

        if (!nextBirthday.isAfter(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        long days = ChronoUnit.DAYS.between(today, nextBirthday);

        System.out.println("Days until your next birthday: " + days);
        scan.close();
    }
}