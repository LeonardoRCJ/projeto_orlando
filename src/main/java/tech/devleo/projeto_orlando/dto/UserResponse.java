package tech.devleo.projeto_orlando.dto;

import java.util.UUID;

public record UserResponse(UUID id, String username, String email, boolean enabled) {
}

