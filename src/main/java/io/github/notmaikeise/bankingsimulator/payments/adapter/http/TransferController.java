package io.github.notmaikeise.bankingsimulator.payments.adapter.http;

import io.github.notmaikeise.bankingsimulator.payments.application.TransferService;
import io.github.notmaikeise.bankingsimulator.payments.domain.Transfer;
import io.github.notmaikeise.bankingsimulator.shared.application.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transfers;

    public TransferController(TransferService transfers) {
        this.transfers = transfers;
    }

    @PostMapping
    public TransferResponse send(@AuthenticationPrincipal AuthenticatedUser principal,
                                 @RequestHeader("Idempotency-Key") UUID key,
                                 @Valid @RequestBody TransferRequest request) {
        Transfer transfer = transfers.transfer(principal.userId(), key,
                request.destinationAccountId(), request.amount());
        return new TransferResponse(transfer.id(), transfer.destinationAccountId(),
                transfer.sourceEntryId(), transfer.destinationEntryId(), transfer.amount().amount(),
                transfer.sourceBalanceAfter().amount(), transfer.occurredAt());
    }

    public record TransferRequest(@NotNull UUID destinationAccountId,
                                  @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2)
                                  BigDecimal amount) { }

    public record TransferResponse(UUID transferId, UUID destinationAccountId, UUID sourceEntryId,
                                   UUID destinationEntryId, BigDecimal amount, BigDecimal balanceAfter,
                                   Instant occurredAt) { }
}
