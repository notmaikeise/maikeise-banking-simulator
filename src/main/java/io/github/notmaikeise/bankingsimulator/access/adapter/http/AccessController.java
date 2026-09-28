package io.github.notmaikeise.bankingsimulator.access.adapter.http;

import io.github.notmaikeise.bankingsimulator.access.application.RegisterUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccessController {
    private final RegisterUserService registration;

    public AccessController(RegisterUserService registration) {
        this.registration = registration;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    @PostMapping("/users")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        UUID id = registration.register(request.name(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(id));
    }

    public record CsrfResponse(String headerName, String parameterName, String token) { }

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password) { }

    public record RegisterResponse(UUID userId) { }
}
