package io.github.notmaikeise.bankingsimulator.shared.application;

import java.util.UUID;

/** Identity from the authenticated session, without depending on Spring Security. */
public interface AuthenticatedUser {
    UUID userId();
}
