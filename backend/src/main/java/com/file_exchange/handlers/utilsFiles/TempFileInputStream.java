package com.file_exchange.handlers.utilsFiles;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Wrapping a temporary file for later processing
 * This class is used in HttpRequestParser
 */
public class TempFileInputStream extends FileInputStream implements AutoCloseable {
    private final Path tempFilePath;
    private final String originalFileName;
    private final long fileSize;
    private boolean closed = false;

    public TempFileInputStream(Path tempFilePath, String originalFileName, long fileSize) throws  FileNotFoundException {
        super(tempFilePath.toFile());
        this.tempFilePath = tempFilePath;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
    }

    public Path getTempFilePath() {
        return tempFilePath;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    @Override
    public void close() throws IOException {
        if(!closed) {
            closed = true;
            super.close();

            try{
                Files.deleteIfExists(tempFilePath);
            }catch(IOException e){
                throw new IOException(e.getMessage());
            }
        }
    }
}
