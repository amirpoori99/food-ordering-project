package com.myapp.auth;

import com.myapp.auth.dto.RegisterRequest;
import com.myapp.auth.dto.ProfileResponse;
import com.myapp.auth.dto.UpdateProfileRequest;
import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.common.exceptions.InvalidCredentialsException;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthController Comprehensive Tests")
class AuthControllerTest {

    private AuthService service;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(AuthService.class);
        controller = new AuthController(service);
    }

    @Nested
    @DisplayName("Registration Controller Tests")
    class RegistrationControllerTests {

        @Test
        @DisplayName("Register delegates to service successfully")
        void register_delegatesToService_success() {
            RegisterRequest req = new RegisterRequest("Test User", "09120000001", "test@example.com", "hash", User.Role.BUYER, "Address");
            User expected = new User(1L, "Test User", "09120000001", "test@example.com", "hash", User.Role.BUYER, "Address");
            when(service.register(req)).thenReturn(expected);

            User actual = controller.register(req);

            verify(service).register(req);
            assertThat(actual).isSameAs(expected);
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getFullName()).isEqualTo("Test User");
            assertThat(actual.getPhone()).isEqualTo("09120000001");
        }

        @EnumSource(User.Role.class)
        @ParameterizedTest
        @DisplayName("Register with different roles delegates correctly")
        void register_differentRoles_delegatesCorrectly(User.Role role) {
            RegisterRequest req = new RegisterRequest("Test User", "0912000000" + role.ordinal(), "test@example.com", "hash", role, "Address");
            User expected = new User(1L, "Test User", "0912000000" + role.ordinal(), "test@example.com", "hash", role, "Address");
            when(service.register(req)).thenReturn(expected);

            User actual = controller.register(req);

            verify(service).register(req);
            assertThat(actual.getRole()).isEqualTo(role);
        }

        @Test
        @DisplayName("Register propagates DuplicatePhoneException from service")
        void register_duplicatePhone_propagatesException() {
            RegisterRequest req = new RegisterRequest("Test User", "09120000002", "test@example.com", "hash", User.Role.BUYER, "Address");
            when(service.register(req)).thenThrow(new DuplicatePhoneException("Phone number already exists: 09120000002"));

            assertThatThrownBy(() -> controller.register(req))
                    .isInstanceOf(DuplicatePhoneException.class)
                    .hasMessageContaining("09120000002");

            verify(service).register(req);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "09111111111", "09222222222", "09333333333", "09444444444", "09555555555"
        })
        @DisplayName("Register with various phone numbers delegates correctly")
        void register_variousPhoneNumbers_delegatesCorrectly(String phone) {
            RegisterRequest req = new RegisterRequest("Test User", phone, "test@example.com", "hash", User.Role.BUYER, "Address");
            User expected = new User(1L, "Test User", phone, "test@example.com", "hash", User.Role.BUYER, "Address");
            when(service.register(req)).thenReturn(expected);

            User actual = controller.register(req);

            verify(service).register(req);
            assertThat(actual.getPhone()).isEqualTo(phone);
        }
    }

    @Nested
    @DisplayName("Login Controller Tests")
    class LoginControllerTests {

        @Test
        @DisplayName("Login delegates to service successfully")
        void login_delegatesToService_success() {
            String phone = "09120000002";
            String passwordHash = "hash2";
            User expected = new User(2L, "Ali Mohammadi", phone, "ali@example.com", passwordHash, User.Role.BUYER, "Isfahan");
            when(service.login(phone, passwordHash)).thenReturn(expected);

            User actual = controller.login(phone, passwordHash);

            verify(service).login(phone, passwordHash);
            assertThat(actual).isSameAs(expected);
            assertThat(actual.getId()).isEqualTo(2L);
            assertThat(actual.getFullName()).isEqualTo("Ali Mohammadi");
            assertThat(actual.getPhone()).isEqualTo(phone);
        }

        @Test
        @DisplayName("Login propagates InvalidCredentialsException from service")
        void login_invalidCredentials_propagatesException() {
            String phone = "09120000003";
            String wrongPassword = "wrongPassword";
            when(service.login(phone, wrongPassword)).thenThrow(new InvalidCredentialsException());

            assertThatThrownBy(() -> controller.login(phone, wrongPassword))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(service).login(phone, wrongPassword);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "09111111111", "09222222222", "09333333333", "09444444444"
        })
        @DisplayName("Login with various phone numbers delegates correctly")
        void login_variousPhoneNumbers_delegatesCorrectly(String phone) {
            String password = "password123";
            User expected = new User(1L, "Test User", phone, "test@example.com", password, User.Role.BUYER, "Address");
            when(service.login(phone, password)).thenReturn(expected);

            User actual = controller.login(phone, password);

            verify(service).login(phone, password);
            assertThat(actual.getPhone()).isEqualTo(phone);
        }
    }

    @Nested
    @DisplayName("Profile Controller Tests")
    class ProfileControllerTests {

        @Test
        @DisplayName("Get profile delegates to service successfully")
        void getProfile_delegatesToService_success() {
            long userId = 123L;
            User user = new User(userId, "John Doe", "09120000006", "john@example.com", "hash", User.Role.BUYER, "Tehran");
            when(service.getProfile(userId)).thenReturn(user);

            ProfileResponse actual = controller.getProfile(userId);

            verify(service).getProfile(userId);
            assertThat(actual.id()).isEqualTo(userId);
            assertThat(actual.fullName()).isEqualTo("John Doe");
            assertThat(actual.phone()).isEqualTo("09120000006");
            assertThat(actual.email()).isEqualTo("john@example.com");
            assertThat(actual.address()).isEqualTo("Tehran");
            assertThat(actual.role()).isEqualTo(User.Role.BUYER);
        }

        @Test
        @DisplayName("Get profile propagates NotFoundException from service")
        void getProfile_userNotFound_propagatesException() {
            long nonExistentUserId = 999999L;
            when(service.getProfile(nonExistentUserId)).thenThrow(new NotFoundException("User", nonExistentUserId));

            assertThatThrownBy(() -> controller.getProfile(nonExistentUserId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User");

            verify(service).getProfile(nonExistentUserId);
        }

        @Test
        @DisplayName("Update profile delegates to service successfully")
        void updateProfile_delegatesToService_success() {
            long userId = 456L;
            UpdateProfileRequest request = new UpdateProfileRequest("Updated Name", "updated@example.com", "Updated Address");
            User updatedUser = new User(userId, "Updated Name", "09120000008", "updated@example.com", "hash", User.Role.BUYER, "Updated Address");
            when(service.updateProfile(userId, request)).thenReturn(updatedUser);

            ProfileResponse actual = controller.updateProfile(userId, request);

            verify(service).updateProfile(userId, request);
            assertThat(actual.id()).isEqualTo(userId);
            assertThat(actual.fullName()).isEqualTo("Updated Name");
            assertThat(actual.email()).isEqualTo("updated@example.com");
            assertThat(actual.address()).isEqualTo("Updated Address");
            assertThat(actual.phone()).isEqualTo("09120000008");
        }
    }

    // Legacy tests for backward compatibility
    @Test
    void register_delegatesToService() {
        RegisterRequest req = new RegisterRequest("Test", "09120000001", null, "hash", User.Role.BUYER, "Addr");
        User expected = new User(1, "Test", "09120000001", null, "hash", User.Role.BUYER, "Addr");
        when(service.register(req)).thenReturn(expected);

        User actual = controller.register(req);

        verify(service).register(req);
        assertThat(actual).isSameAs(expected);
    }

    @Test
    void login_delegatesToService() {
        User expected = new User(2, "Ali", "09120000002", null, "hash2", User.Role.BUYER, "A");
        when(service.login("09120000002", "hash2")).thenReturn(expected);

        User actual = controller.login("09120000002", "hash2");

        verify(service).login("09120000002", "hash2");
        assertThat(actual).isSameAs(expected);
    }
}
