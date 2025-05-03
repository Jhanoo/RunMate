package com.runhwani.runmate.domain.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Schema(description = "회원 정보")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {
    
    @Schema(description = "회원 ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "이메일")
    @Column(unique = true)
    private String email;
    
    @Schema(description = "비밀번호")
    private String password;
    
    @Schema(description = "닉네임")
    private String nickname;
} 