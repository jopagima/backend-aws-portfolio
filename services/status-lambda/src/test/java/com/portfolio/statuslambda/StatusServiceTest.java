package com.portfolio.statuslambda;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatusServiceTest {

    @Test
    public void testGetStatusMessage() {
        StatusService service = new StatusService();
        String result = service.getStatusMessage();
        assertEquals("Service is running", result);
    }

}
