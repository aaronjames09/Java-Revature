import java.util.Scanner;

public class REPL {
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        double accbalance = 0.0;
        double deposit;
        double withdraw;
        System.out.println("1. Check Balance\n2. Deposit\n3. Withdraw Amount\n4. Exit");
        int choice;

        do {
            System.out.println("\nEnter your Choice : ");
            choice = scan.nextInt();
            switch (choice){
                case 1:
                    System.out.printf("\nRemaining Balance :%.2f",accbalance);
                    break;
                case 2:
                    System.out.println("\nEnter Amount to be Deposited :");
                    deposit = scan.nextDouble();
                    accbalance += deposit;
                    break;
                case 3:
                    System.out.println("\nEnter amount to be Withdrawn :");
                    withdraw = scan.nextDouble();
                    if (withdraw>accbalance) {
                        System.out.println("\nInsufficient Funds Remaining");                    
                    }
                    else {
                        accbalance -= withdraw;
                        System.out.printf("\nSuccessful Withdrawal\nRemaining Balance : %.2f",accbalance);
                    }
                    break;
                case 4:
                    System.out.println("\nThank You for Banking with Us");
                    break;
                default:
                    System.out.println("\nInvalid Choice");
                    break;
                }
            }while (choice != 4);
    scan.close();
    }
}