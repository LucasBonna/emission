package br.com.contafacil.bonnarotec.emission.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MultipartFileUtil {
    
    public static MultipartFile convertStringToMultipartFile(String content, String fileName) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "application/xml";
            }

            @Override
            public boolean isEmpty() {
                return content == null || content.isEmpty();
            }

            @Override
            public long getSize() {
                return content.getBytes(StandardCharsets.UTF_8).length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return content.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                try (FileOutputStream fos = new FileOutputStream(dest)) {
                    fos.write(content.getBytes(StandardCharsets.UTF_8));
                }
            }
        };
    }
}
