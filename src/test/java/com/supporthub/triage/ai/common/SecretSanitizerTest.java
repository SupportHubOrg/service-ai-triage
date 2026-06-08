package com.supporthub.triage.ai.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecretSanitizerTest {

    private final SecretSanitizer sanitizer = new SecretSanitizer();

    @Test
    void shouldRedactCommonCredentialFormats() {
        var text = """
                Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMifQ.signature
                password=summer2026
                postgres://admin:secret123@localhost:5432/app
                jdbc:postgresql://localhost:5432/app?user=admin&password=jdbcSecret123
                token: ghp_abcdefghijklmnopqrstuvwxyz123456
                """;

        var sanitized = sanitizer.sanitize(text);

        assertThat(sanitized)
                .contains("Authorization: Bearer [REDACTED_TOKEN]")
                .contains("password=[REDACTED_SECRET]")
                .contains("postgres://admin:[REDACTED_PASSWORD]@localhost:5432/app")
                .contains("jdbc:postgresql://localhost:5432/app?user=admin&password=[REDACTED_PASSWORD]")
                .contains("token: [REDACTED_SECRET]")
                .doesNotContain("summer2026")
                .doesNotContain("secret123")
                .doesNotContain("jdbcSecret123")
                .doesNotContain("eyJhbGci");
    }

    @Test
    void shouldRedactProviderTokensAppearingStandalone() {
        var text = """
                The customer pasted sk-abcdefghijklmnopqrstuvwxyz123456 from their logs.
                AWS access key AKIA1234567890ABCDEF was visible in the dump.
                A leftover JWT eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5OTkifQ.othersignature was logged.
                """;

        var sanitized = sanitizer.sanitize(text);

        assertThat(sanitized)
                .contains("[REDACTED_API_KEY]")
                .contains("[REDACTED_AWS_ACCESS_KEY]")
                .contains("[REDACTED_JWT]")
                .doesNotContain("sk-abcdefghijklmnopqrstuvwxyz123456")
                .doesNotContain("AKIA1234567890ABCDEF")
                .doesNotContain("othersignature");
    }

    @Test
    void shouldRedactPrivateKeyBlock() {
        var text = """
                Operator note before the key:
                -----BEGIN PRIVATE KEY-----
                MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQ
                content-that-must-disappear
                -----END PRIVATE KEY-----
                after the key.
                """;

        var sanitized = sanitizer.sanitize(text);

        assertThat(sanitized)
                .contains("[REDACTED_PRIVATE_KEY]")
                .contains("Operator note before the key:")
                .contains("after the key.")
                .doesNotContain("content-that-must-disappear")
                .doesNotContain("BEGIN PRIVATE KEY");
    }

    @Test
    void shouldRedactSecretStatedWithIsKeyword() {
        var text = "Reminder: the admin token is gh1pTokenValueABCDEF123456 for the portal.";

        var sanitized = sanitizer.sanitize(text);

        assertThat(sanitized)
                .contains("the admin token is [REDACTED_SECRET]")
                .doesNotContain("gh1pTokenValueABCDEF123456");
    }

    @Test
    void shouldRedactHighEntropyValueNextToKeyword() {
        var text = "client_secret=Xk9aZ2pQ7rL4mB1tV8wC3nD6yE0fH5j";

        var sanitized = sanitizer.sanitize(text);

        assertThat(sanitized)
                .doesNotContain("Xk9aZ2pQ7rL4mB1tV8wC3nD6yE0fH5j");
    }

    @Test
    void shouldNotRedactNormalSupportContent() {
        var text = "Employee: VPN stopped working after the sign-in method renewal. "
                + "Operator: Refresh the user sign-in session in the admin console and recreate the VPN profile.";

        assertThat(sanitizer.sanitize(text)).isEqualTo(text);
    }

    @Test
    void shouldLeaveBlankInputUnchanged() {
        assertThat(sanitizer.sanitize("")).isEqualTo("");
        assertThat(sanitizer.sanitize(null)).isNull();
    }

    @Test
    void shouldDetectPotentialSecret() {
        assertThat(sanitizer.containsPotentialSecret("api_key=abcDEF1234567890abcDEF1234567890")).isTrue();
        assertThat(sanitizer.containsPotentialSecret("Use sk-abcdefghijklmnopqrstuvwxyz123456 here")).isTrue();
        assertThat(sanitizer.containsPotentialSecret(
                "Refresh the user sign-in session in the admin console.")).isFalse();
    }
}
