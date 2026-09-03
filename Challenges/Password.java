import java.util.Scanner;

public class Password{
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter a Valid Password : ");
        String s = scan.nextLine();
        boolean charUpper = false;
        boolean charLower = false;
        boolean digit = false;
        if (s.length()<8){
            System.out.println("Password Rejected : Atleast 8 Characters Needed");
        }
        scan.close();

        for (int i = 0 ; i<s.length() ; i++){
            char ch = s.charAt(i);
            if (Character.isUpperCase(ch)){
                charUpper = true;
            }
            else if (Character.isLowerCase(ch)){
                charLower = true;
            }
            else if (Character.isDigit(ch)){
                digit = true;
            }
        }

        if (charUpper == false){
            System.out.println("Password must contain atleast 1 uppercase letter.");
        }
        if (charLower == false){
            System.out.println("Password must contain atleast 1 lowercase letter.");
        }
        if (digit == false){
            System.out.println("Password must contain atleast 1 digit.");
        }

        if (charUpper && charLower && digit){
            System.out.println("Valid Password");
        }
    }
}