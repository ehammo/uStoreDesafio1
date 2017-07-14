import java.io.IOException;
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

    private Boolean copyFile(Path sourcefile, Path targetfile) throws IOException {
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
        System.out.println("terminei");
    }

    public void stop() throws Exception{
        threadpool.shutdownNow();
    }
}
