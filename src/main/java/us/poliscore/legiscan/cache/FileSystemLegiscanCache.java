package us.poliscore.legiscan.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import us.poliscore.legiscan.service.CachedLegiscanService;
import us.poliscore.legiscan.view.LegiscanResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;

public class FileSystemLegiscanCache implements LegiscanCache {

    private static final Logger LOGGER = Logger.getLogger(FileSystemLegiscanCache.class.getName());

    private final File baseDir;
    private final ObjectMapper objectMapper;
    private final int defaultTtlSecs; // If > 0, applies to non-static entries unless overridden

    public FileSystemLegiscanCache(File baseDir, ObjectMapper objectMapper, int defaultTtlSecs) {
        this.baseDir = baseDir;
        this.objectMapper = objectMapper;
        this.defaultTtlSecs = defaultTtlSecs;

        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new IllegalStateException("Could not create cache directory: " + baseDir);
        }
    }

    public FileSystemLegiscanCache(File baseDir, ObjectMapper objectMapper) {
        this(baseDir, objectMapper, 0);
    }

    private File resolvePath(String key) {
        String filename = key.replaceAll("[^/a-zA-Z0-9\\-_]", "_") + "/cached.json";
        return new File(baseDir, filename);
    }

    @Override
    public <T> Optional<T> getOrExpire(String key, TypeReference<T> typeRef) {
        File file = resolvePath(key);
        if (!file.exists()) {
            return Optional.empty();
        }

        try {
            byte[] data = Files.readAllBytes(file.toPath());
            CachedEntry entry = objectMapper.readValue(data, CachedEntry.class);

            if (entry.isExpired()) {
                LOGGER.fine("Cache expired for key: " + key);
                file.delete(); // Clean up expired file
                return Optional.empty();
            }

            T value = objectMapper.convertValue(entry.getValue(), typeRef);
            return Optional.of(value);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to read cache for key: " + key, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<LegiscanResponse> getOrExpire(String key) {
    	return getOrExpire(key, new TypeReference<LegiscanResponse>() {});
    }
    
    @Override
    public Optional<CachedEntry> peek(String key) {
        File file = resolvePath(key);
        if (!file.exists()) {
            return Optional.empty();
        }

        try {
            byte[] data = Files.readAllBytes(file.toPath());
            CachedEntry entry = objectMapper.readValue(data, CachedEntry.class);

            return Optional.of(entry);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to read cache for key: " + key, e);
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object value) {
        put(key, value, ttlForCacheKey(key));
    }

    public void put(String key, Object value, long ttlSecs) {
        File file = resolvePath(key);
        file.getParentFile().mkdirs();
        try {
            CachedEntry entry = new CachedEntry(value, Instant.now().getEpochSecond(), ttlSecs);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, entry);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write cache for key: " + key, e);
        }
    }
    
    @Override
	public String toString() {
		return "File System Cache (" + baseDir.getAbsolutePath() + "]";
	}

    @Override
    public void remove(String cacheKey) {
        File file = resolvePath(cacheKey);
        if (file.exists()) {
            try {
                Files.delete(file.toPath());
                LOGGER.fine("Cache file deleted for key: " + cacheKey);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to delete cache file for key: " + cacheKey, e);
            }
        }
    }
    
    @Override
	public boolean presentAndValid(String key) {
    	return peek(key).isPresent();
	}
    
    protected long ttlForCacheKey(String cacheKey) {
    	return CachedLegiscanService.isCacheKeyStatic(cacheKey) ? 0 : defaultTtlSecs;
    }
}
