import java.util.Scanner;

public class testScore {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the 5 test scores : ");

        int score1 = scan.nextInt();
        int score2 = scan.nextInt();
        int score3 = scan.nextInt();
        int score4 = scan.nextInt();
        int score5 = scan.nextInt();
        int total = score1+score2+score3+score4+score5;

        scan.close();

        System.out.printf("Total = %d\n", total);
        System.out.printf("Average = %d\n", (total/5));
        
        int[] scores = {score1, score2, score3, score4, score5};
        int smallest = scores[0];
        int largest = scores[0];

        for (int i=0 ; i<5 ; i++){
            if (scores[i]<smallest) 
                smallest = scores[i];
            if (scores[i]>largest)
                largest = scores[i];
        }

        System.out.printf("Highest Score : %d\n",largest);
        System.out.printf("Lowest Score : %d\n",smallest);

        for (int i=0 ; i<5 ; i++){
            if (scores[i]>=90) 
                System.out.printf("%d : A\n",scores[i]);
            else if (scores[i]>=80) 
                System.out.printf("%d : B\n",scores[i]);
            else if (scores[i]>=70)
                System.out.printf("%d : C\n",scores[i]);
            else if (scores[i]>=60)
                System.out.printf("%d : D\n",scores[i]);
            else
                System.out.printf("%d : F\n",scores[i]);
        }
    }
}