package com.myapp.ui.analytics;

import com.myapp.ui.analytics.util.ChartUtil;
import com.myapp.ui.common.SimpleTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Logic-only tests for ChartUtil (no JavaFX UI dependencies)
 */
@DisplayName("ChartUtil Logic Tests")
public class ChartUtilLogicTest extends SimpleTestBase {

    @Test
    @DisplayName("Should get chart CSS string")
    void shouldGetChartCSS() {
        ChartUtil util = new ChartUtil();
        String css = util.getChartCSS();
        assertNotNull(css);
        assertTrue(css.contains("chart"));
    }

    // Add more pure logic tests here if available
} 