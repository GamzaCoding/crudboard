package com.example.crudboard.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

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
    @Autowired ObjectMapper objectMapper; // DTO 객체를 JSON으로 직렬화 할 때 쓰는 도구, 지금 테스트에서는 문자열 JSON을 직접 써서 실제로는 사용하지 않음

    @Test
    @DisplayName("Create api 실행시 201이 반환된다.")
    void createPost_returns201AndLocation() throws Exception {
        String body = """
                {
                    "title": "hello",
                    "content": "first post"
                }
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/posts/\\d+")));
    }

    @Test
    @DisplayName("Read api 실행시 202가 반환된다.")
    void getPost_return200AndJson() throws Exception {
        String createBody = """
                {
                    "title": "hello",
                    "content": "first post"
                }
                """;

        String location = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("hello"))
                .andExpect(jsonPath("$.content").value("first post"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("DB에 없는 read 요청 시 ApiExceptionHandler 통해 에러가 처리된다.")
    void getNonExistingPost_returns404AndErrorCode() throws Exception {
        mockMvc.perform(get("/api/posts/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("Post not found")))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Update api 실행시 204 반환 및 데이터 변경이 성공한다. ")
    void updatePost_return204_andChangesData() throws Exception {
        String createBody = """
                {
                    "title": "hello",
                    "content": "first post"
                }
                """;

        String location = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        String updateBody = """
                {
                    "title": "updated title",
                    "content": "updated content"
                }
                """;

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updated title"))
                .andExpect(jsonPath("$.content").value("updated content"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Delete api 실행시 204 반환, 삭제 후 조회 시 404가 반환된다.")
    void deletePost_returns204_andThen404OnGet() throws Exception {
        String createBody = """
                {
                    "title": "hello",
                    "content": "first post"
                }
                """;

        String location = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");


        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());

        // 삭제 후 조회시 404 에러코드 확인
        mockMvc.perform(get(location))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @DisplayName("")
    @Test
    void listPost_withKeyword_filtersResult() throws Exception {
        String firstSampleBody = """
                {
                    "title": "spring",
                    "content": "boot"
                }
                """;
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstSampleBody))
                .andExpect(status().isCreated());

        String secondSampleBody = """
                {
                    "title": "java",
                    "content": "jpa"
                }
                """;
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondSampleBody))
                .andExpect(status().isCreated());

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
    @DisplayName("title에 공백문자 입력 시 400 예외 + VALIDATION_ERROR 확인")
    void createPost_validationFail_returns400AndDetails() throws Exception {
        String body = """
                {
                    "title": "",
                    "content": "ok"
                }
                """;
        mockMvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.title", not(emptyOrNullString())))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
