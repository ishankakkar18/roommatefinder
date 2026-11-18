package in.flatmate.auth;

/** Authenticated principal placed in the SecurityContext by the JWT filter. */
public record AppUserPrincipal(Long id, String email) {
}
