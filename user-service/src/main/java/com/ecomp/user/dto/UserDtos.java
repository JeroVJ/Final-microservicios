package com.ecomp.user.dto;

import com.ecomp.user.entity.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileDto {
        private String keycloakId;
        @NotBlank(message = "Username is required")
        private String username;
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;
        private Integer age;
        private String photoBase64;
        private String description;
        private String role;
        private String phone;
        private String website;
        private String socialMedia;

        public static UserProfileDto fromEntity(UserProfile entity) {
            if (entity == null) return null;
            return UserProfileDto.builder()
                    .keycloakId(entity.getKeycloakId())
                    .username(entity.getUsername())
                    .email(entity.getEmail())
                    .age(entity.getAge())
                    .photoBase64(entity.getPhotoBase64())
                    .description(entity.getDescription())
                    .role(entity.getRole().name())
                    .phone(entity.getPhone())
                    .website(entity.getWebsite())
                    .socialMedia(entity.getSocialMedia())
                    .build();
        }

        public UserProfile toEntity(String keycloakId) {
            return UserProfile.builder()
                    .keycloakId(keycloakId)
                    .username(this.username)
                    .email(this.email)
                    .age(this.age)
                    .photoBase64(this.photoBase64)
                    .description(this.description)
                    .role(this.role != null ? UserProfile.UserRole.valueOf(this.role) : UserProfile.UserRole.CLIENT)
                    .phone(this.phone)
                    .website(this.website)
                    .socialMedia(this.socialMedia)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileInput {
        @NotBlank(message = "Username is required")
        private String username;
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;
        private Integer age;
        private String photoBase64;
        private String description;
        private String role;
        private String phone;
        private String website;
        private String socialMedia;
    }
}
