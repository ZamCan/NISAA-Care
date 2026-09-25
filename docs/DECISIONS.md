# Engineering decisions requiring future review

1. **No authoritative seed text:** the first build ships metadata-only
   `REVIEW_REQUIRED` health/Islamic topic records and no quotations. This is
   intentional to avoid fabricated claims.
2. **Relationship sync is settings-only:** the gateway contract carries
   permissions/version/timestamps, never cycle or fertility records.
3. **Local pairing is honest:** short-lived tokens and local acceptance are
   implemented for UX/testing, but the UI states that a real sync gateway is
   not configured.
4. **Plain SQLite with targeted encryption:** the schema is local-first and
   sensitive notes/support messages use Android Keystore AES-GCM. Full database
   encryption and authenticated backup require a threat-model review.
5. **Biological estimates are not medical advice:** UI consistently labels
   predictions as estimates and preserves an algorithm version.
6. **Husband access is explicit:** the husband screen reads cycle/fertility data
   only when the relationship-specific permission is active.
