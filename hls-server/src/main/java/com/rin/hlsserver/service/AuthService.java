package com.rin.hlsserver.service;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.rin.hlsserver.dto.response.AuthResponse;
import com.rin.hlsserver.exception.BaseException;
import com.rin.hlsserver.model.Role;
import com.rin.hlsserver.model.User;
import com.rin.hlsserver.repository.RoleRepository;
import com.rin.hlsserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    static String DEFAULT_ROLE = "USER";
    static long JWT_EXPIRATION_MS = 86400000; // 1 day
    @Value("${app.jwt.signerKey}")
    @NonFinal
    String JWT_SECRET;
    PasswordEncoder passwordEncoder;


    public AuthResponse registerUser(String email, String password, String fullName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BaseException("Email is already in use");
        }

        Role defaultRole= createDefaultRoleIfNotExist();

        User newUser= User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .roles(Set.of(defaultRole))
                .build();

        userRepository.save(newUser);

        return buildAuthResponse(newUser);
    }

    public AuthResponse loginUser(String email, String password) {
        User user= userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException("User not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BaseException("Invalid credentials");
        }

        return buildAuthResponse(user);
    }


    private Role createDefaultRoleIfNotExist() {
        return roleRepository.findById(DEFAULT_ROLE).orElseGet(() -> {
            Role role= Role.builder().name(DEFAULT_ROLE).build();
            roleRepository.save(role);
            return role;
        });
    }

    private AuthResponse buildAuthResponse(User user ) {

        return AuthResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .id(user.getId())
                .roles(user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()))
                .build();
    }


//    private String createJWTToken(User user) {
//        JWSHeader jwsHeader= new JWSHeader(JWSAlgorithm.HS512);
//
//        JWTClaimsSet jwtClaimsSet= new JWTClaimsSet.Builder()
//                .subject(user.getId().toString())
//                .issuer("com.rin.hls-server")
//                .issueTime(new Date())
//                .expirationTime(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
//                .jwtID(UUID.randomUUID().toString())
//                .claim("scope", buildScope(user))
//                .build();
//
//        Payload payload= new Payload(jwtClaimsSet.toJSONObject());
//
//        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
//
//        try {
//            jwsObject.sign(new MACSigner(JWT_SECRET.getBytes()));
//            return jwsObject.serialize();
//        } catch (JOSEException e) {
//            log.error("Cannot create token", e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    private String buildScope(User user) {
//        // ROLE_USER ROLE_ADMIN ...
//        StringJoiner stringJoiner= new StringJoiner(" ");
//        user.getRoles().forEach(role -> stringJoiner.add("ROLE_" + role.getName()));
//        return stringJoiner.toString();
//    }






}
