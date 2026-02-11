package com.example.crudboard.util;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TestAuthHelper가 주는 이점
 * 기존 테스트 코드에서 아래 프로세스가 반복되는 것을 해결해줌
 * 회원가입 요청, 로그인 요청, 새션 꺼내기, 세션 붙여서 글 생성
 */
public final class TestAuthHelper {

    private TestAuthHelper() {
    }

    public static MockHttpSession signupAndLogin(MockMvc mockMvc) throws Exception {
        String email = "user" + System.nanoTime() + "@example.com"; // nonoTime()으로 중복 계정 예방
        String password = "password123!";
        String body = String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, email, password);
        // 회원가입 요청
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // 로그인 요청
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent())
                .andReturn();
        // 로그인으로 만들어진 세션 반환
        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    public static String createPost(MockMvc mockMvc, MockHttpSession session, String title, String content) throws Exception {
        String body = String.format("""
                {
                    "title": "%s",
                    "content": "%s"
                }
                """, title, content);

        return mockMvc.perform(post("/api/posts")
                        .session(session) // 로그인된 세션을 추가해서 게시글을 생성한다. 중요한 부분임
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
    }
    // Location에서 {id}만 뽑아서 Long으로 변환
    // ex) Location: /api/posts/123 -> 123
    public static Long createPostId(MockMvc mockMvc, MockHttpSession session, String title, String content) throws Exception {
        String location = createPost(mockMvc, session, title, content);
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}
