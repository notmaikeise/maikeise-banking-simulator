package io.github.notmaikeise.bankingsimulator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BankingFlowTests {
    @Autowired
    MockMvc mvc;
    @Autowired
    UserDetailsService users;
    @Autowired
    PasswordEncoder passwords;

    @Test
    void registrationLoginSessionDemoCreditAndStatement() throws Exception {
        String email = UUID.randomUUID() + "@example.test";
        String registration = "{\"name\":\"Example User\",\"email\":\"" + email
                + "\",\"password\":\"password123\"}";

        mvc.perform(get("/api/csrf")).andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"));
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(registration)).andExpect(status().isCreated());
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(registration)).andExpect(status().isConflict());
        mvc.perform(get("/api/accounts/me")).andExpect(status().is3xxRedirection());

        assertTrue(passwords.matches("password123", users.loadUserByUsername(email).getPassword()),
                "Registration must store a BCrypt hash matching the user's password");
        MockHttpSession authenticatedSession = (MockHttpSession) mvc.perform(formLogin()
                        .user(email).password("password123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername(email))
                .andReturn().getRequest().getSession(false);
        mvc.perform(get("/api/accounts/me").session(authenticatedSession))
                .andExpect(status().isOk()).andExpect(jsonPath("$.currency").value("BRL"));

        String key = UUID.randomUUID().toString();
        String credit = "{\"amount\":25.00}";
        mvc.perform(post("/api/accounts/me/demo-credits").session(authenticatedSession)
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(credit))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/accounts/me/demo-credits").session(authenticatedSession).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(credit))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balanceAfter").value(25.0));
        mvc.perform(post("/api/accounts/me/demo-credits").session(authenticatedSession).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(credit))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balanceAfter").value(25.0));
        mvc.perform(post("/api/accounts/me/demo-credits").session(authenticatedSession).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":26.00}")).andExpect(status().isConflict());
        mvc.perform(get("/api/accounts/me").session(authenticatedSession))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(25.0));
        mvc.perform(get("/api/accounts/me/entries").session(authenticatedSession))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].kind").value("DEMO_CREDIT"));

        String otherEmail = UUID.randomUUID() + "@example.test";
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Other User\",\"email\":\"" + otherEmail
                                + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());
        MockHttpSession otherSession = (MockHttpSession) mvc.perform(formLogin()
                        .user(otherEmail).password("password123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername(otherEmail))
                .andReturn().getRequest().getSession(false);
        mvc.perform(get("/api/accounts/me").session(otherSession))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(0.0));
        mvc.perform(get("/api/accounts/me/entries").session(otherSession))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }
}
