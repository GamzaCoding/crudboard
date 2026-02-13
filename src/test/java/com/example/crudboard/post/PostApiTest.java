package com.example.crudboard.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.example.crudboard.util.TestAuthHelper.createPost;
import static com.example.crudboard.util.TestAuthHelper.signupAndLogin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test") // 테스트 실행 시 test 프로파일 실행, application-test.yml 설정이 적용됨
@SpringBootTest // 스프링 부트 애플리케이션을 실제처럼 통째로 띄운 상태로 테스트한다는 뜻, Controller, Service, Repository, JPA 까지 전부 올라오고 스프링 컨테이너를 실제로 만들어서 테스트 실행, 통합테스트에 가까움
/*
    MockMvc를 스프링이 자동으로 준비해서주 주입해주세 하는 설정
    MockMvc는 서버를 진짜로 띄우지 않고도, 스프링 MVC 디스패처(DispatcherServlet) 레벌로 요청을 "가짜 HTTP"처럼 보내고 응답을 검증할 수 있게 해준다.
    장점 : 빠르고, 실제 컨트롤러/필터/검증 흐름을 그대로 탄다.
 */
@AutoConfigureMockMvc
/*
    각 테스트가 끝나면 트랜잭션이 롤백돼서 DB 상태가 깨끗하게 유지됨.
    즉, 테스트들이 서로 데이터에 영향을 주지 않게 "격리"해주는 역할
 */
@Transactional
public class PostApiTest {

    @Autowired MockMvc mockMvc; // 테스트에서 HTTP 요청을 보내는 도구, perform()으로 요청을 날리고, andExpect()로 결과를 검증한다.

    @Test
    @DisplayName("Create api 실행시 201이 반환된다.")
    void createPostAndReturns201() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);

        mockMvc.perform(post("/api/posts")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "title": "hello",
                        "content": "first post"
                        }
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Create api 실행시 Location 헤더가 /api/posts/{id} 형식으로 반환된다.")
    void createPostReturnsLocationHeader() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);

        String location = createPost(mockMvc, session, "hello", "first post");
        assertThat(location, matchesPattern("/api/posts/\\d+"));
    }

    @Test
    @DisplayName("Read api 실행시 200가 반환된다.")
    void getPostReturn200() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        String location = createPost(mockMvc, session, "hello", "first post");

        mockMvc.perform(get(location))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DB에 없는 read 요청 시 ApiExceptionHandler 통해 에러가 처리된다.")
    void getNonExistingPostReturns404AndErrorCode() throws Exception {
        mockMvc.perform(get("/api/posts/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("게시글을 찾을 수 없습니다.")))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Update api 실행시 204가 반환된다.")
    void updatePostReturn204() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        String location = createPost(mockMvc, session, "hello", "first post");

        String updateBody = """
                {
                    "title": "updated title",
                    "content": "updated content"
                }
                """;

        mockMvc.perform(put(location)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Update api 실행시 데이터 변경이 성공한다.")
    void updatePostAndChangeData() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        String location = createPost(mockMvc, session, "hello", "first post");

        String updateBody = """
                {
                    "title": "updated title",
                    "content": "updated content"
                }
                """;

        mockMvc.perform(put(location)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updated title"))
                .andExpect(jsonPath("$.content").value("updated content"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }


    @Test
    @DisplayName("Delete api 실행시 204가 반환된다.")
    void deletePostReturns204() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        String location = createPost(mockMvc, session, "hello", "first post");


        mockMvc.perform(delete(location)
                        .session(session))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete api 실행 후 조회 시 404가 반환된다.")
    void deletePostReturn404OnGet() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        String location = createPost(mockMvc, session, "hello", "first post");
        mockMvc.perform(delete(location)
                .session(session));

        mockMvc.perform(get(location))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }


    @Test
    @DisplayName("키워드 검색 기능이 정상 동작한다.")
    void listPostWithKeywordFiltersResult() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        createPost(mockMvc, session, "spring", "boot");
        createPost(mockMvc, session, "java", "jpa");

        mockMvc.perform(get("/api/posts").param("keyword", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title", containsStringIgnoringCase("spring")));
    }

    /*
    이 테스트가 통과한다는 의미
    @Valid 실패 시에도 응답이 비지 않고, 항상 JSON이며 code가 고정된 값으로 내려오고 details에 어떤 필드가 왜 실패했는지 들어간다
    즉, API를 사용하는 입장(프론트/앱)에서 "예외 처리가 예측 가능"해진다.
     */
    @Test
    @DisplayName("title에 공백문자 입력 시 400가 발생된다.")
    void createPostReturns400() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        String body = """
                {
                    "title": "",
                    "content": "ok"
                }
                """;
        mockMvc.perform(post("/api/posts")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("title에 공백문자 입력 시 VALIDATION_ERROR가 확인된다.")
    void createPostValidateFile() throws Exception {
        MockHttpSession session = signupAndLogin(mockMvc);
        String body = """
                {
                    "title": "",
                    "content": "ok"
                }
                """;

        mockMvc.perform(post("/api/posts")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }


    @Test
    @DisplayName("페이지당 1000개의 게시글을 요청해도 50개 까지만 나온다")
    void listPostsSizeIsClampedToMax() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50));
    }
}
