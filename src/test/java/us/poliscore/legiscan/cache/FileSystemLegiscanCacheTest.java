package us.poliscore.legiscan.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileSystemLegiscanCacheTest {

    private File tempDir;
    private FileSystemLegiscanCache cache;
    private ObjectMapper objectMapper;

    @BeforeAll
    void setup() throws Exception {
        objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        tempDir = Files.createTempDirectory("legiscan-cache-test").toFile();
    }

    @AfterEach
    void cleanUp() {
        for (File file : tempDir.listFiles()) {
            file.delete();
        }
    }

    @AfterAll
    void tearDown() {
        tempDir.delete();
    }

    @Test
    void testPutAndGet() {
        cache = new FileSystemLegiscanCache(tempDir, objectMapper);
        String key = "testKey";
        Map<String, String> value = Map.of("foo", "bar");

        cache.put(key, value);

        Optional<Map<String, String>> result = cache.getOrExpire(key, new TypeReference<>() {});
        assertTrue(result.isPresent());
        assertEquals("bar", result.get().get("foo"));
    }

    @Test
    void testOverwriteValue() {
        cache = new FileSystemLegiscanCache(tempDir, objectMapper);
        String key = "overwriteTest";
        Map<String, String> first = Map.of("a", "1");
        Map<String, String> second = Map.of("a", "2");

        cache.put(key, first);
        cache.put(key, second);

        Optional<Map<String, String>> result = cache.getOrExpire(key, new TypeReference<>() {});
        assertTrue(result.isPresent());
        assertEquals("2", result.get().get("a"));
    }
}
