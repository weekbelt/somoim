package me.weekbelt.runningflex.modules.account.controller;

import me.weekbelt.runningflex.infra.MockMvcTest;
import me.weekbelt.runningflex.modules.account.Account;
import me.weekbelt.runningflex.modules.account.repository.AccountRepository;
import me.weekbelt.runningflex.modules.account.WithAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@MockMvcTest
class UpdateAccountProfileControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll(); 
    }

    @WithAccount("joohyuk")
    @DisplayName("프로필 수정 폼")
    @Test
    public void updateProfileForm() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("joohyuk")
    @DisplayName("프로필 수정 하기 - 입력값 정상")
    @Test
    public void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post("/settings/profile")
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Account joohyuk = accountRepository.findByNickname("joohyuk").get();
        assertThat(joohyuk.getBio()).isEqualTo(bio);
    }

    @WithAccount("joohyuk")
    @DisplayName("프로필 수정 하기 - 입력값 에러")
    @Test
    public void updateProfile_error() throws Exception {
        String bio = "길게 소개를 수정하는 경우, 길게 소개를 수정하는 경우, 길게 소개를 수정하는 경우, 길게 소개를 수정하는 경우";
        mockMvc.perform(post("/settings/profile")
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account joohyuk = accountRepository.findByNickname("joohyuk").get();
        assertThat(joohyuk.getBio()).isNull();
    }

}