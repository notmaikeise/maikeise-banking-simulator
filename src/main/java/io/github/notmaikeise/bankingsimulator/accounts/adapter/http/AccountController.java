package io.github.notmaikeise.bankingsimulator.accounts.adapter.http;

import io.github.notmaikeise.bankingsimulator.accounts.application.AccountQueryService;
import io.github.notmaikeise.bankingsimulator.accounts.application.DemoCreditRepository.Receipt;
import io.github.notmaikeise.bankingsimulator.accounts.application.DemoCreditService;
import io.github.notmaikeise.bankingsimulator.accounts.domain.Account;
import io.github.notmaikeise.bankingsimulator.accounts.domain.AccountEntry;
import io.github.notmaikeise.bankingsimulator.shared.application.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts/me")
public class AccountController {
    private final AccountQueryService queries;
    private final DemoCreditService credits;

    public AccountController(AccountQueryService queries, DemoCreditService credits) {
        this.queries = queries;
        this.credits = credits;
    }

    @GetMapping
    public AccountResponse account(@AuthenticationPrincipal AuthenticatedUser principal) {
        Account account = queries.forOwner(principal.userId());
        return new AccountResponse(account.id(), account.balance().amount(), account.balance().currency());
    }

    @GetMapping("/entries")
    public List<EntryResponse> statement(@AuthenticationPrincipal AuthenticatedUser principal,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return queries.statement(principal.userId(), page, size).stream().map(EntryResponse::from).toList();
    }

    @PostMapping("/demo-credits")
    public CreditResponse credit(@AuthenticationPrincipal AuthenticatedUser principal,
                                 @RequestHeader("Idempotency-Key") UUID key,
                                 @Valid @RequestBody CreditRequest request) {
        Receipt receipt = credits.credit(principal.userId(), key, request.amount());
        return new CreditResponse(receipt.entryId(), receipt.amount().amount(),
                receipt.balanceAfter().amount(), receipt.occurredAt());
    }

    public record CreditRequest(@NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2)
                                BigDecimal amount) { }

    public record AccountResponse(UUID accountId, BigDecimal balance, String currency) { }

    public record EntryResponse(UUID entryId, String kind, BigDecimal amount, Instant occurredAt) {
        static EntryResponse from(AccountEntry entry) {
            return new EntryResponse(entry.id(), entry.kind().name(), entry.amount().amount(), entry.occurredAt());
        }
    }

    public record CreditResponse(UUID entryId, BigDecimal amount, BigDecimal balanceAfter, Instant occurredAt) { }
}
