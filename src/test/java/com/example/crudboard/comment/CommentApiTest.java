package com.example.crudboard.comment;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static com.example.crudboard.util.TestAuthHelper.createPostId;
import static com.example.crudboard.util.TestAuthHelper.signupAndLogin;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CommentApiTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("댓글 생성 시 201과 응답 바디가 반환된다.")
    void createCommentReturns201AndBody() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        Long postId = createPostId(mockMvc, session, "post", "content");

        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "first comment"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.postId").value(postId.intValue()))
                .andExpect(jsonPath("$.content").value("first comment"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("댓글 목록은 게시글별로 필터링된다.")
    void listCommentsFiltersByPost() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        Long postId = createPostId(mockMvc, session, "post1", "2번 댓글까지 선물 증정");
        Long otherPostId = createPostId(mockMvc, session, "post2", "1빠 댓글만 선물 증정");

        createComment(session, postId, "1번 댓글 남기고 갑니다");
        createComment(session, postId, "2번 댓글 남기고 갑니다.");
        createComment(session, otherPostId, "1빠");

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].postId", everyItem(is(postId.intValue()))));
        /**
         * $.content[*].postId 관련 구조
         * {
         *   "content": [
         *     { "id": 1, "postId": 10, "content": "..." },
         *     { "id": 2, "postId": 10, "content": "..." }
         *   ]
         * }
         */
    }

    @Test
    @DisplayName("댓글 삭제 후 동일 댓글 삭제 요청은 404가 반환된다.")
    void deleteCommentReturns204AndThen404() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        Long postId = createPostId(mockMvc, session, "이몸 등장", "안녕들하신가");
        Long commentId = createComment(session, postId, "여 간만이군");

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("댓글 내용이 비어 있으면 400 + VALIDATION_ERROR 반환")
    void createCommentValidationFailReturns400() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        Long postId = createPostId(mockMvc, session, "이몸 등장", "안녕들 하신가");

        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.content", not(emptyOrNullString())));
    }

    private Long createComment(MockHttpSession session, Long postId, String content) throws Exception {
        String body = String.format("""
                {
                    "content": "%s"
                }
                """, content);

        MvcResult result = mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Number id = JsonPath.read(json, "$.id");
        return id.longValue();
    }
}
