/**
 * Created by eduardo on 14/07/2017.
 */
public class Teste {
    public static void main(String[] args){
        try {
            Backup backup = new Backup("C:\\Users\\eduar\\Desktop", "C:\\Users\\eduar\\Desktop\\teste");
            backup.start();
            backup.awaitThreads();
        }catch(Exception e){

        }
    }
}
