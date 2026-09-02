import java.util.*;

public class Calculator {
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the first number : ");        
        int a = scan.nextInt();
        System.out.println("Enter the second number : ");
        int b = scan.nextInt();
        System.out.println("Enter your choice (1/ADD) (2/SUB) (3/MUL) (4/DIV) : ");
        int choice = scan.nextInt();

        scan.close();

        switch(choice) {
            case 1:
                System.out.println(a+b);
            break;

            case 2:
                System.out.println(a-b);
            break;

            case 3:
                System.out.println(a*b);
            break;

            case 4:
                System.out.println(a/b);
            break;

            default:
                System.out.println("Invalid Choice");
       }
    }
}