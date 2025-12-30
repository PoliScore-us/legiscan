package us.poliscore.legiscan.cache;

import java.time.Instant;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import us.poliscore.legiscan.view.LegiscanResponse;
import us.poliscore.legiscan.view.RefreshFrequency;

public interface LegiscanCache {
    /**
     * Returns the raw cache value, if it exists. The object may or may not be expired. If it is expired, it will not be removed from the cache.
     * 
     * @param key
     * @return
     */
    public <T> Optional<T> peek(String key, TypeReference<T> typeRef);
    
    public Optional<CachedEntry> peekEntry(String key);
    
    /**
     * Attempts to fetch the object from the cache. If the object is expired, it will be cleared out from the cache and Optional.empty() will be returned.
     * 
     * @param key
     * @return
     */
    public Optional<LegiscanResponse> getOrExpire(String key);
    
    /**
     * Attempts to fetch the object from the cache. If the object is expired, it will be cleared out from the cache and Optional.empty() will be returned.
     * 
     * @param key
     * @param typeRef
     * @return
     */
    public <T> Optional<T> getOrExpire(String key, TypeReference<T> typeRef);
    
    public void put(String key, Object value, long ttlSecs);
    
    public void put(String key, Object value, String objectHash, long ttlSecs);
    
    /**
     * Returns true if and only if the cache contains a value for the given key and the value is not expired.
     * 
     * @param key
     * @return
     */
    public boolean presentAndValid(String key);
    
	public void remove(String cacheKey);
	
	@Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CachedEntry {
        private Object value;
        private long timestamp;
        private long ttlSecs;
        private String objectHash;
        
        @JsonIgnore
        public boolean isExpired(RefreshFrequency minFreshness) {
        	if (getTtlSecs() <= 0) return false;
        	
        	long validLength = minFreshness == null ? getTtlSecs() : Math.max(minFreshness.asDuration().getSeconds(), getTtlSecs());
        	
            return Instant.now().getEpochSecond() > getTimestamp() + validLength;
        }
    }
}
