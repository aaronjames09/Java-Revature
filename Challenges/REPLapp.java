import java.util.Scanner;
import java.util.random.RandomGenerator;

public class REPLapp {
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        RandomGenerator rand = RandomGenerator.getDefault();
        
        System.out.println("1.Add\n2.Subtract\n3.Multiply\n4.Divide\n5.Random\n6.Reverse\n7.Quit");
        int choice;

        do{
            System.out.println("\nEnter a Choice : ");
            choice = scan.nextInt();
            double num1 = 0;
            double num2 = 0;
            if(choice>=1 && choice<=4){
                System.out.println("Enter First Number : ");
                num1 = scan.nextDouble();
                System.out.println("Enter Second Number : ");
                num2 = scan.nextDouble();
            }
            
            switch (choice){
                case 1:
                    System.out.printf("Result = %f",(num1+num2));
                    break;
                case 2:
                    System.out.printf("Result = %f",(num1-num2));
                    break;
                case 3:
                    System.out.printf("Result = %f",(num1*num2));
                    break;
                case 4:
                    System.out.printf("Result = %f",(num1/num2));
                    break;
                case 5:
                    System.out.println("Enter Minimum Number : ");
                    int min = scan.nextInt();
                    System.out.println("Enter Maximum Number : ");
                    int max = scan.nextInt();
                    int ran = rand.nextInt(min,max);
                    System.out.printf("Random = %d",ran);
                    break;
                case 6:
                    System.out.println("Enter a String");
                    scan.nextLine();
                    String s = scan.nextLine();       
                    String reversed = "";

                    for (int i = (s.length()-1) ; i>=0 ; i--){
                        reversed += s.charAt(i);
                    }

                    System.out.printf("Reversed : %s",reversed);
                    break;
                case 7:
                    System.out.println("Thank You");
                    break;
            }
        }while(choice!=7);
        scan.close();
    }
}