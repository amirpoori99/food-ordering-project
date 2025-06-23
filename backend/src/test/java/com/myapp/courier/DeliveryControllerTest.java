package com.myapp.courier;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Delivery;
import com.myapp.common.models.DeliveryStatus;
import com.myapp.common.models.Order;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("DeliveryController Tests")
class DeliveryControllerTest {

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private HttpExchange exchange;

    private DeliveryController controller;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new DeliveryController(deliveryService);
        responseBody = new ByteArrayOutputStream();
        
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
    }

    @Nested
    @DisplayName("GET Endpoints Tests")
    class GetEndpointsTests {

        @Test
        @DisplayName("Should get delivery details successfully")
        void shouldGetDeliveryDetailsSuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Delivery delivery = createTestDelivery(deliveryId);
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            when(deliveryService.getDelivery(deliveryId)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getDelivery(deliveryId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should get delivery by order successfully")
        void shouldGetDeliveryByOrderSuccessfully() throws IOException {
            // Arrange
            Long orderId = 1L;
            Delivery delivery = createTestDelivery(1L);
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/order/" + orderId));
            when(deliveryService.getDeliveryByOrderId(orderId)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getDeliveryByOrderId(orderId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should get courier deliveries successfully")
        void shouldGetCourierDeliveriesSuccessfully() throws IOException {
            // Arrange
            Long courierId = 1L;
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L), createTestDelivery(2L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId));
            when(deliveryService.getCourierDeliveryHistory(courierId)).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getCourierDeliveryHistory(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should get courier active deliveries successfully")
        void shouldGetCourierActiveDeliveriesSuccessfully() throws IOException {
            // Arrange
            Long courierId = 1L;
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId + "/active"));
            when(deliveryService.getCourierActiveDeliveries(courierId)).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getCourierActiveDeliveries(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should get deliveries by status successfully")
        void shouldGetDeliveriesByStatusSuccessfully() throws IOException {
            // Arrange
            DeliveryStatus status = DeliveryStatus.PENDING;
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/status/pending"));
            when(deliveryService.getDeliveriesByStatus(status)).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getDeliveriesByStatus(status);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should get active deliveries successfully")
        void shouldGetActiveDeliveriesSuccessfully() throws IOException {
            // Arrange
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/active"));
            when(deliveryService.getActiveDeliveries()).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getActiveDeliveries();
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should get pending deliveries successfully")
        void shouldGetPendingDeliveriesSuccessfully() throws IOException {
            // Arrange
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/pending"));
            when(deliveryService.getDeliveriesByStatus(DeliveryStatus.PENDING)).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getDeliveriesByStatus(DeliveryStatus.PENDING);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should check courier availability successfully")
        void shouldCheckCourierAvailabilitySuccessfully() throws IOException {
            // Arrange
            Long courierId = 1L;
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId + "/available"));
            when(deliveryService.isCourierAvailable(courierId)).thenReturn(true);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).isCourierAvailable(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("available"));
        }

        @Test
        @DisplayName("Should get courier statistics successfully")
        void shouldGetCourierStatisticsSuccessfully() throws IOException {
            // Arrange
            Long courierId = 1L;
            DeliveryRepository.CourierStatistics stats = new DeliveryRepository.CourierStatistics(
                10L, 8L, 1L, 0L, 25.5, 800.0
            );
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId + "/statistics"));
            when(deliveryService.getCourierStatistics(courierId)).thenReturn(stats);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getCourierStatistics(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should return 400 for invalid status")
        void shouldReturn400ForInvalidStatus() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/status/invalid"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Invalid status: invalid"));
        }

        @Test
        @DisplayName("Should return 404 for delivery not found")
        void shouldReturn404ForDeliveryNotFound() throws IOException {
            // Arrange
            Long deliveryId = 999L;
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            when(deliveryService.getDelivery(deliveryId)).thenThrow(new NotFoundException("Delivery", deliveryId));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should return 404 for unknown endpoint")
        void shouldReturn404ForUnknownEndpoint() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/unknown"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }

        @Test
        @DisplayName("Should handle empty delivery list responses")
        void shouldHandleEmptyDeliveryListResponses() throws IOException {
            // Arrange
            List<Delivery> emptyList = Arrays.asList();
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/active"));
            when(deliveryService.getActiveDeliveries()).thenReturn(emptyList);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getActiveDeliveries();
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("[]"));
        }

        @Test
        @DisplayName("Should handle null courier availability response")
        void shouldHandleNullCourierAvailabilityResponse() throws IOException {
            // Arrange
            Long courierId = 1L;
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId + "/available"));
            when(deliveryService.isCourierAvailable(courierId)).thenReturn(false);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).isCourierAvailable(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("\"available\":false"));
        }
    }

    @Nested
    @DisplayName("POST Endpoints Tests")
    class PostEndpointsTests {

        @Test
        @DisplayName("Should create delivery successfully")
        void shouldCreateDeliverySuccessfully() throws IOException {
            // Arrange
            Long orderId = 1L;
            Double estimatedFee = 50.0;
            Delivery delivery = createTestDelivery(1L);
            String requestBody = "{\"orderId\":" + orderId + ",\"estimatedFee\":" + estimatedFee + "}";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.createDelivery(orderId, estimatedFee)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).createDelivery(orderId, estimatedFee);
            verify(exchange, times(1)).sendResponseHeaders(201, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should return 400 when order ID is missing")
        void shouldReturn400WhenOrderIdIsMissing() throws IOException {
            // Arrange
            String requestBody = "{\"estimatedFee\":50.0}";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Order ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when estimated fee is missing")
        void shouldReturn400WhenEstimatedFeeIsMissing() throws IOException {
            // Arrange
            String requestBody = "{\"orderId\":1}";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Estimated fee is required"));
        }

        @Test
        @DisplayName("Should return 404 for unknown POST endpoint")
        void shouldReturn404ForUnknownPostEndpoint() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/unknown"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }

        @Test
        @DisplayName("Should handle NotFoundException when creating delivery")
        void shouldHandleNotFoundExceptionWhenCreatingDelivery() throws IOException {
            // Arrange
            Long orderId = 999L;
            Double estimatedFee = 50.0;
            String requestBody = "{\"orderId\":" + orderId + ",\"estimatedFee\":" + estimatedFee + "}";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.createDelivery(orderId, estimatedFee))
                .thenThrow(new NotFoundException("Order", orderId));

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).createDelivery(orderId, estimatedFee);
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should handle invalid JSON in POST request")
        void shouldHandleInvalidJsonInPostRequest() throws IOException {
            // Arrange
            String requestBody = "invalid json";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Invalid JSON"));
        }
    }

    @Nested
    @DisplayName("PUT Endpoints Tests")
    class PutEndpointsTests {

        @Test
        @DisplayName("Should assign courier successfully")
        void shouldAssignCourierSuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Long courierId = 2L;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"courierId\":" + courierId + "}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.assignCourier(deliveryId, courierId)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).assignCourier(deliveryId, courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should mark picked up successfully")
        void shouldMarkPickedUpSuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Long courierId = 2L;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"courierId\":" + courierId + "}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/pickup"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.markPickedUp(deliveryId, courierId)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).markPickedUp(deliveryId, courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should mark delivered successfully")
        void shouldMarkDeliveredSuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Long courierId = 2L;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"courierId\":" + courierId + "}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/deliver"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.markDelivered(deliveryId, courierId)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).markDelivered(deliveryId, courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should cancel delivery successfully")
        void shouldCancelDeliverySuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String reason = "Customer requested cancellation";
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"reason\":\"" + reason + "\"}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/cancel"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.cancelDelivery(deliveryId, reason)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).cancelDelivery(deliveryId, reason);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should cancel delivery with null reason")
        void shouldCancelDeliveryWithNullReason() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/cancel"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.cancelDelivery(deliveryId, null)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).cancelDelivery(deliveryId, null);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should update delivery status successfully")
        void shouldUpdateDeliveryStatusSuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            DeliveryStatus status = DeliveryStatus.DELIVERED;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"status\":\"delivered\"}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/status"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.updateDeliveryStatus(deliveryId, status)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).updateDeliveryStatus(deliveryId, status);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should return 400 when courier ID is missing for assign")
        void shouldReturn400WhenCourierIdIsMissingForAssign() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Courier ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when courier ID is missing for pickup")
        void shouldReturn400WhenCourierIdIsMissingForPickup() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/pickup"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Courier ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when courier ID is missing for deliver")
        void shouldReturn400WhenCourierIdIsMissingForDeliver() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/deliver"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Courier ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when status is missing")
        void shouldReturn400WhenStatusIsMissing() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/status"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Status is required"));
        }

        @Test
        @DisplayName("Should return 400 for invalid status")
        void shouldReturn400ForInvalidStatusInUpdate() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{\"status\":\"invalid\"}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/status"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Invalid status: invalid"));
        }

        @Test
        @DisplayName("Should return 409 for illegal state exception")
        void shouldReturn409ForIllegalStateException() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Long courierId = 2L;
            String requestBody = "{\"courierId\":" + courierId + "}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.assignCourier(deliveryId, courierId))
                .thenThrow(new IllegalStateException("Can only assign courier to pending deliveries"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(409, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should handle invalid JSON in request body")
        void shouldHandleInvalidJsonInRequestBody() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "invalid json";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Invalid JSON"));
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Courier ID is required"));
        }

        @Test
        @DisplayName("Should return 404 for unknown PUT endpoint")
        void shouldReturn404ForUnknownPutEndpoint() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/1/unknown"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }
    }

    @Nested
    @DisplayName("DELETE Endpoints Tests")
    class DeleteEndpointsTests {

        @Test
        @DisplayName("Should delete delivery successfully")
        void shouldDeleteDeliverySuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            doNothing().when(deliveryService).deleteDelivery(deliveryId);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).deleteDelivery(deliveryId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("deleted successfully"));
        }

        @Test
        @DisplayName("Should return 404 for unknown DELETE endpoint")
        void shouldReturn404ForUnknownDeleteEndpoint() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/unknown"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }

        @Test
        @DisplayName("Should handle NotFoundException when deleting delivery")
        void shouldHandleNotFoundExceptionWhenDeletingDelivery() throws IOException {
            // Arrange
            Long deliveryId = 999L;
            
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            doThrow(new NotFoundException("Delivery", deliveryId)).when(deliveryService).deleteDelivery(deliveryId);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).deleteDelivery(deliveryId);
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 405 for unsupported method")
        void shouldReturn405ForUnsupportedMethod() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("PATCH");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/1"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(405, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Method not allowed"));
        }

        @Test
        @DisplayName("Should return 500 for internal server error")
        void shouldReturn500ForInternalServerError() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            when(deliveryService.getDelivery(deliveryId)).thenThrow(new RuntimeException("Database error"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(500, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Internal server error"));
        }

        @Test
        @DisplayName("Should handle invalid delivery ID in path")
        void shouldHandleInvalidDeliveryIdInPath() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/invalid"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }

        @Test
        @DisplayName("Should handle malformed path")
        void shouldHandleMalformedPath() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }
    }

    // Helper method to create test delivery
    private Delivery createTestDelivery(Long id) {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(id);
        
        Delivery delivery = new Delivery(order, 50.0);
        // Use reflection to set ID
        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(delivery, id);
        } catch (Exception e) {
            // Ignore reflection errors in tests
        }
        
        return delivery;
    }
}