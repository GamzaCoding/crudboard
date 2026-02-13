package com.example.crudboard.auth;

import com.example.crudboard.auth.dto.AuthRequest;
import com.example.crudboard.global.error.ApiException;
import com.example.crudboard.global.error.ErrorCode;
import com.example.crudboard.user.User;
import com.example.crudboard.user.UserRepository;
import com.example.crudboard.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long signup(AuthRequest authRequest) {
        validateAlreadySignup(authRequest);
        String hash = passwordEncoder.encode(authRequest.password());
        User user = new User(authRequest.email(), hash, UserRole.USER);
        return userRepository.save(user).getId();
    }

    public void login(AuthRequest authRequest, HttpServletRequest request) {
        User user = userRepository.findByEmail(authRequest.email())
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_VALUE_OF_EMAIL_OR_PASSWORD));
        validatePassword(authRequest, user);
        inputAuthToSession(request, user);
    }

    public void logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    private void validatePassword(AuthRequest authRequest, User user) {
        if (!passwordEncoder.matches(authRequest.password(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.BAD_VALUE_OF_EMAIL_OR_PASSWORD);
        }
    }

    private static void inputAuthToSession(HttpServletRequest request, User user) {
        var auth = new UsernamePasswordAuthenticationToken(
                user.getId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        /**
         * SecurityContextHolder은 보통 ThreadLocal로 현재 요청 스레드에 SecurityContext를 보관한다.
         * 즉, 이 시점부터 현재 요청 처리 중에는 SecutiryContextHolder.getContext().getAuthentication()으로
         * auth 접근 가능
         */
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        /**
         * 세션에 SecurityContext 저장(다음 요청에서도 유지)
         * Spring Security의 세션 기반 보관 방식과 같은 키를 써서, 다음 요청부터는 Security 필터가 세션에서 context를 꺼내
         * SecurityContextHolder에 다시 넣어줄 수 있게 된다.
         * 즉, 로그인 요청에서 "세션에 인증정보 저장"
         * 다음 요청에서 "세션에서 인증정보 로드"
         * 그래서 컨트롤러에서 Authentication 파라미터로 주입받을 수 있음
         */
        request.getSession(true)
                .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    private void validateAlreadySignup(AuthRequest authRequest) {
        if (userRepository.existsByEmail(authRequest.email())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
