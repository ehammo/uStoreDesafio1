import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;

/**
 * Created by eduardo on 14/07/2017.
 */
public class Backup {

    private Path source;
    private String target;
    private List<Future<Boolean>> transfers;
    private ExecutorService threadpool;
    private static final Logger LOGGER = Logger.getLogger( Backup.class.getName() );

    public Backup(String source, String target){
        this.source = Paths.get(source);
        this.target = target;
        transfers = new ArrayList<>();
        threadpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private boolean fileEquals(File file1, File file2) throws Exception{
        if(file1.length() != file2.length()){
            return false;
        }

        try(InputStream in1 =new BufferedInputStream(new FileInputStream(file1));
            InputStream in2 =new BufferedInputStream(new FileInputStream(file2));
        ){

            int value1,value2;
            do{
                //since we're buffered read() isn't expensive
                value1 = in1.read();
                value2 = in2.read();
                if(value1 !=value2){
                    return false;
                }
            }while(value1 >=0);

            //since we already checked that the file sizes are equal
            //if we're here we reached the end of both files without a mismatch
            return true;
        }
    }

    private Boolean copyFile(Path sourcefile, Path targetfile) throws Exception {
        if(Files.exists(targetfile)){
            File source = sourcefile.toFile();
            File target = targetfile.toFile();

            //Verificar se os arquivos são iguais, caso sejam não há necessidade de copiar
            //Ainda muito lento
//            if(fileEquals(source,target)){
//                System.out.println("No need for copping: "+targetfile.getFileName());
//                return true;
//            }else{
//                Files.delete(targetfile);
//            }

            //se o arquivo já existe substitua
            Files.delete(targetfile);
        }
        Files.copy(sourcefile, targetfile);
        System.out.println("copping: "+targetfile.getFileName());
        return true;
    }

    private void feedTransfers() throws IOException{
        try (Stream<Path> paths = Files.walk(source)) {
            paths
                .filter(Files::isRegularFile)
                .forEach((file)->transfers.add(createTasks(threadpool, file)));
        }
    }

    private Future<Boolean> createTasks(ExecutorService threadpool, Path sourcefile){
        return threadpool.submit(()-> copyFile(sourcefile, Paths.get(target+"\\"+sourcefile.getFileName()))) ;
    }

    public void start() throws Exception{
        threadpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        feedTransfers();
    }

    public void awaitThreads(){
        for (Future<Boolean> fut : transfers) {
            try {
                fut.get();
            } catch (Exception ioe) {
                LOGGER.warning("Unable to transfer file: " + ioe.getMessage());
            }
        }
        threadpool.shutdown();
        System.out.println("terminei");
    }

    public void stop() throws Exception{
        threadpool.shutdownNow();
    }
}
