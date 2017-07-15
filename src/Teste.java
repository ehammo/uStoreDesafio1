/**
 * Created by eduardo on 14/07/2017.
 */
public class Teste {
    public static void main(String[] args){
        try {
            long start = System.nanoTime();
            Backup backup = new Backup("C:\\Users\\eduar\\Downloads\\hd\\sv", "C:\\Users\\eduar\\Desktop\\teste");
            backup.start();
            backup.awaitThreads();
            long end = System.nanoTime();
            System.out.println((end-start)/1000000000);
        }catch(Exception e){

        }
    }
}
