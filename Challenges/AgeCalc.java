import java.time.LocalDate;
import java.time.Period;
import java.util.Scanner;

public class AgeCalc {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        System.out.print("Enter your birth date: ");
        String date = scan.nextLine();
        LocalDate birthDate = LocalDate.parse(date);
        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();

        System.out.println("You are " + age + " years old.");
        scan.close();
    }
}