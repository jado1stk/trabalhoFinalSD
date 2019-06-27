package javareadtextfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

public class DatasetFileReader {

    public static Dataset fromFile(Path path) {
        
        Dataset dataset = new Dataset();
        try (BufferedReader reader = Files.newBufferedReader(path);) {
            Iterator<String> it = reader.lines().iterator();
            
            Charset charset = StandardCharsets.UTF_8;

            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("\\s{2,}", " ");
            Files.write(path, content.getBytes(charset));
            
            String[] colNames = (it.hasNext()) ? it.next().split("\\s+") : null;
            for (String col : colNames) {
                dataset.getData().put(col, new ArrayList());
            }

            while (it.hasNext()) {
                String[] cols = it.next().split("\\s+");

                if (cols.length != colNames.length) {
                    throw new IncompatibleClassChangeError("Houve algum erro na quantidade de colunas do arquivo");
                }

                for (int i = 0; i < colNames.length; i++) {
                    dataset.get(colNames[i]).add(cols[i]);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataset;
    }

}
