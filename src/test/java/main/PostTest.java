package main;

import junit.framework.TestCase;
import org.junit.Test;
import main.model.entity.Post;
import main.model.entity.PostComment;
import main.model.entity.User;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@DisplayName("Testing posts")
public class PostTest extends TestCase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    PostService postService;

    @MockBean
    PostRepository postRepository;

    @MockBean
    PostCommentRepository postCommentRepository;

    @MockBean
    UserRepository userRepository;

    @LocalServerPort
    private String port;

    @BeforeEach
    public void setUp() throws Exception {
        User user = new User("Иван Петров", "$2a$12$YW0ZASfRD8b2ftH4H2ydxuM0JxJT5w794PONETkCp9WdNg0fkeYV.",
                "IvanPetrov@ya.ru");

        Post post = new Post(1, (byte) 1, user, "title", "postText");
        PostComment comment = new PostComment(user, post, "Коммент");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());
        doReturn(new PageImpl<>(List.of(post))).when(postRepository).search(any(), anyString());
        doReturn(Optional.of(post)).when(postRepository).getPostById(1);
        doReturn(List.of(comment)).when(postCommentRepository).searchByPost(any());
    }

    @Test
    @DisplayName("Testing run Spring Boot")
    public void contextLoads() throws Exception {
    }

    @Test
    @DisplayName("Controller @NotNull")
    public void controllerNotNull() throws Exception {
        assertThat(postService).isNotNull();
    }

    @Test
    @DisplayName("Checking the error noAuthentication in the response")
    public void checkErrorsInResponse() throws Exception {
        doThrow(UsernameNotFoundException.class).when(userRepository).findByEmail(anyString());
        //noAuthentication
        this.mockMvc.perform(get(port + "/api/post/my"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get post by id. (200 OK)")
    @WithMockUser
    public void shouldReturnPost() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(this.port + "/api/v1/post/my"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
