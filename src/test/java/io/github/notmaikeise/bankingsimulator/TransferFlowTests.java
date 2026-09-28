package io.github.notmaikeise.bankingsimulator;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TransferFlowTests {
    @Autowired MockMvc mvc;

    @Test
    void pixMovesMoneyOnceAndOnlyTheOwnersSeeTheirEntries() throws Exception {
        User sender = registerAndLogin();
        User receiver = registerAndLogin();
        User thirdParty = registerAndLogin();
        String key = UUID.randomUUID().toString();
        String funds = "{\"amount\":100.00}";
        mvc.perform(post("/api/accounts/me/demo-credits").session(sender.session()).with(csrf())
                        .header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(funds))
                .andExpect(status().isOk());

        String body = request(receiver.accountId(), "40.00");
        mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Idempotency-Key", key).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/api/transfers").session(sender.session())
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Idempotency-Key", key))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/transfers").session(sender.session()).with(csrf())
                        .header("Idempotency-Key", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                        .content(request(sender.accountId(), "1.00")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/transfers").session(sender.session()).with(csrf())
                        .header("Idempotency-Key", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                        .content(request(receiver.accountId(), "101.00")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/transfers").session(sender.session()).with(csrf())
                        .header("Idempotency-Key", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                        .content(request(UUID.randomUUID(), "1.00")))
                .andExpect(status().isNotFound());

        String first = mvc.perform(post("/api/transfers").session(sender.session()).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balanceAfter").value(60.0))
                .andReturn().getResponse().getContentAsString();
        String retry = mvc.perform(post("/api/transfers").session(sender.session()).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertEquals(first, retry);
        String transferId = JsonPath.read(first, "$.transferId");
        mvc.perform(post("/api/transfers").session(sender.session()).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
                        .content(request(receiver.accountId(), "41.00")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/transfers").session(sender.session()).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
                        .content(request(thirdParty.accountId(), "40.00")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/transfers").session(sender.session()).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
                        .content(request(UUID.randomUUID(), "40.00")))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/accounts/me").session(sender.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(60.0));
        mvc.perform(get("/api/accounts/me").session(receiver.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(40.0));
        mvc.perform(get("/api/accounts/me").session(thirdParty.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(0.0));
        mvc.perform(get("/api/accounts/me/entries").session(sender.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].kind").value("PIX_SENT"))
                .andExpect(jsonPath("$[0].referenceId").value(transferId));
        mvc.perform(get("/api/accounts/me/entries").session(receiver.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].kind").value("PIX_RECEIVED"))
                .andExpect(jsonPath("$[0].referenceId").value(transferId));
        mvc.perform(get("/api/accounts/me/entries").session(thirdParty.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    private User registerAndLogin() throws Exception {
        String email = UUID.randomUUID() + "@example.test";
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Example User\",\"email\":\"" + email
                                + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin().user(email).password("password123"))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession(false);
        String account = mvc.perform(get("/api/accounts/me").session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new User(session, UUID.fromString(JsonPath.read(account, "$.accountId")));
    }

    private String request(UUID target, String amount) {
        return "{\"destinationAccountId\":\"" + target + "\",\"amount\":" + amount + "}";
    }

    private record User(MockHttpSession session, UUID accountId) { }
}
