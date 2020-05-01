package me.weekbelt.runningflex.web.controller.account;

import me.weekbelt.runningflex.domain.account.Account;
import me.weekbelt.runningflex.domain.account.AccountRepository;
import me.weekbelt.runningflex.domain.account.AccountService;
import me.weekbelt.runningflex.mail.EmailMessage;
import me.weekbelt.runningflex.web.BasicControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
class AccountControllerTest extends BasicControllerTest {

    @DisplayName("회원 가입 화면")
    @Test
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());      // 인증된 사용자가 없는지 확인
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    public void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "joohyuk")
                .param("email", "email....")
                .param("password", "12345")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().hasErrors())
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    public void signUpSubmit_with_correct_input() throws Exception {
        // 회원 가입 요청이 제대로 작동하는지 화인
        mockMvc.perform(post("/sign-up")
                .param("nickname", "joohyuk")
                .param("email", "vfrvfr4207@hanmail.net")
                .param("password", "12345678")
                .with(csrf()))      // POST 요청시 CSRF값을 비교하여 같으면 요청을 처리하고 다르면 403에러가 발생한다.
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("joohyuk"));

        // 저장된 계정의 Password값이 인코딩이 되어있는지 확인
        Account account = accountService.findByEmail("vfrvfr4207@hanmail.net");
        assertThat(account).isNotNull();
        assertThat(account.getPassword()).isNotEqualTo("12345678");
        assertThat(account.getEmailCheckToken()).isNotNull();

        // 가입 된 회원이 존재하는지 확인
        assertThat(accountRepository.existsByEmail("vfrvfr4207@hanmail.net")).isTrue();
        // 회원가입시 JavaMailSender 통해 SimpleMailMessage 호출되는지
        then(emailService).should().sendEmail(any(EmailMessage.class));

    }

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    public void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "asdfasdfasdf")
                .param("email", "wfsdf@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    public void checkEmailToken_with_correct_input() throws Exception {
        Account account = Account.builder()
                .email("vfrvfr4207@hanmail.net")
                .password("12345678")
                .nickname("joohyuk")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername(account.getNickname()));
    }

}