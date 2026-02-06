package com.file_exchange.unit.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.file_exchange.handlers.utilsFiles.MimeTypeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("MimeTypeUtils tests")
class MimeTypeUtilsTest {

    @Test
    @DisplayName("Should return octet-stream for null filename")
    void testNullFilename() {
        String result = MimeTypeUtils.detect(null);
        assertThat(result).isEqualTo("application/octet-stream");
    }

    @ParameterizedTest
    @CsvSource({
        "image.png, image/png",
        "IMAGE.PNG, image/png",
        "photo.jpg, image/jpeg",
        "photo.jpeg, image/jpeg",
        "document.pdf, application/pdf",
        "readme.txt, text/plain; charset=utf-8",
        "archive.zip, application/zip",
        "unknown.xyz, application/octet-stream",
        "file, application/octet-stream"
    })
    @DisplayName("Should detect correct mime type for various extensions")
    void testMimeTypeDetection(String filename, String expectedMimeType) {
        String result = MimeTypeUtils.detect(filename);
        assertThat(result).isEqualTo(expectedMimeType);
    }

    @Test
    @DisplayName("Should handle mixed case extensions")
    void testMixedCaseExtensions() {
        assertThat(MimeTypeUtils.detect("file.PNG")).isEqualTo("image/png");
        assertThat(MimeTypeUtils.detect("file.JpG")).isEqualTo("image/jpeg");
        assertThat(MimeTypeUtils.detect("file.PDF")).isEqualTo("application/pdf");
    }
}
