
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AzureBufferedCopier {

    public static void main(String[] args) throws Exception {
        String localDir = "C:/temp"; // chemin local à adapter
        String storageAccount = "<storage-account>";
        String container = "<container-name>";
        String targetFolder = "import";
        String suffix = storageAccount + ".dfs.core.windows.net";

        String targetURI = "abfss://" + container + "@" + suffix + "/" + targetFolder;

        Configuration conf = new Configuration();
        conf.set("fs.azure.account.auth.type." + suffix, "OAuth");
        conf.set("fs.azure.account.oauth.provider.type." + suffix, "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider");
        conf.set("fs.azure.account.oauth2.client.id." + suffix, "<client-id>");
        conf.set("fs.azure.account.oauth2.client.secret." + suffix, "<client-secret>");
        conf.set("fs.azure.account.oauth2.client.endpoint." + suffix, "https://login.microsoftonline.com/<tenant-id>/oauth2/token");

        // Optimisation réseau
        conf.set("fs.azure.write.request.timeout", "600000");
        conf.set("fs.azure.write.retries", "5");
        conf.set("fs.azure.write.retry.interval", "20000");

        FileSystem fs = FileSystem.get(new URI(targetURI), conf);

        Files.list(Paths.get(localDir))
                .parallel()
                .filter(path -> path.toString().endsWith(".csv"))
                .forEach(path -> {
                    String filename = path.getFileName().toString();
                    Path remotePath = new Path(targetURI + "/" + filename);

                    try (
                        BufferedInputStream in = new BufferedInputStream(new FileInputStream(path.toFile()), 8 * 1024 * 1024);
                        FSDataOutputStream out = fs.create(remotePath, true)
                    ) {
                        byte[] buffer = new byte[8 * 1024 * 1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) > 0) {
                            out.write(buffer, 0, bytesRead);
                        }
                        System.out.println("✅ Copié : " + filename);
                    } catch (Exception e) {
                        System.err.println("Erreur sur " + filename + " : " + e.getMessage());
                    }
                });

        fs.close();
    }
}
