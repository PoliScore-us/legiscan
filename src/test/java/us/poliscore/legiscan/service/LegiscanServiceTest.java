package us.poliscore.legiscan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import us.poliscore.legiscan.cache.LegiscanCache;
import us.poliscore.legiscan.exception.LegiscanException;
import us.poliscore.legiscan.view.LegiscanBillView;
import us.poliscore.legiscan.view.LegiscanResponse;

public class LegiscanServiceTest {

    private ObjectMapper objectMapper;
    private LegiscanCache mockCache;
    private LegiscanService service;

//    @BeforeEach
//    void setup() {
//        objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
//        mockCache = mock(LegiscanCache.class);
//        service = new LegiscanService("fake-api-key", objectMapper, mockCache);
//        injectFakeHttpClient(service);
//    }
//
//    @SuppressWarnings("unchecked")
//	@Test
//    void testGetBillReturnsFromCache() {
//        String billId = "123";
//        String cacheKey =  "getBill:" + billId;
//
//        LegiscanBillView bill = new LegiscanBillView();
//        bill.setBillId("123");
//        bill.setTitle("Test Bill");
//
//        LegiscanResponse response = new LegiscanResponse();
//        response.setBill(bill);
//
//        when(mockCache.get(eq(cacheKey), any(TypeReference.class))).thenReturn(Optional.of(response));
//
//        LegiscanBillView result = service.getBill(billId);
//
//        assertEquals("Test Bill", result.getTitle());
//        verify(mockCache).get(eq(cacheKey), any(TypeReference.class));
//        verify(mockCache, never()).put(any(), any());
//    }

//    @Test
//    void testGetBillThrowsIfNotFoundAndApiFails() {
//        String billId = "not-found";
//        String cacheKey = "getBill:" + billId;
//
//        LegiscanService spyService = spy(new LegiscanService("fake-api-key", objectMapper, mockCache));
//        when(mockCache.get(eq(cacheKey), any())).thenReturn(Optional.empty());
//        doThrow(new LegiscanException("forced failure")).when(spyService).makeRequest(anyString(), any());
//
//        assertThrows(LegiscanException.class, () -> spyService.getBill(billId));
//    }

    private void injectFakeHttpClient(LegiscanService service) {
        try {
            Field httpClientField = LegiscanService.class.getDeclaredField("httpClient");
            httpClientField.setAccessible(true);
            httpClientField.set(service, HttpClient.newHttpClient()); // will fail fast if ever used
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject fake HttpClient", e);
        }
    }
}
