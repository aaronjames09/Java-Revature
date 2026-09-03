import java.util.Scanner;

public class StringAnalyzer {
    public static void main(String[] args){
        System.out.println("Enter a Word : ");
        Scanner scan = new Scanner(System.in);
        String s = scan.nextLine();
        scan.close();
        int vow = 0;
        int con = 0;
        int digi = 0;
        int spac = 0;


        for (int i = 0 ; i<s.length() ; i++){
            char ch = s.charAt(i);

            if (ch == ' '){
                spac++;
            }
            else if (Character.isDigit(ch)){
                digi++;
            }
            else if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u' || 
            ch == 'A' || ch == 'E' || ch == 'I' || ch == 'O' || ch == 'U'){
                vow++;                
            }
            else if (Character.isLowerCase(ch) || Character.isUpperCase(ch)){
                con++;
            }
        }

        System.out.printf("\nCharacters = %d\nVowels = %d\nConsonants = %d\nDigits = %d\nSpaces = %d",s.length(),vow,con,digi,spac);
    }
}