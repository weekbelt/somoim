package me.weekbelt.runningflex.modules.society.controller;

import me.weekbelt.runningflex.infra.MockMvcTest;
import me.weekbelt.runningflex.modules.account.Account;
import me.weekbelt.runningflex.modules.account.service.AccountService;
import me.weekbelt.runningflex.modules.account.WithAccount;
import me.weekbelt.runningflex.modules.society.Society;
import me.weekbelt.runningflex.modules.society.SocietyFactory;
import me.weekbelt.runningflex.modules.society.service.SocietyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class UpdateSocietyStatusControllerTest {

    @Autowired
    SocietyFactory societyFactory;
    @Autowired
    AccountService accountService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    SocietyService societyService;

    @DisplayName("스터디 상태페이지")
    @WithAccount("joohyuk")
    @Test
    public void societyStatusForm() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/society";
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("society"))
                .andExpect(view().name("society/settings/society"));
    }

    @DisplayName("소모임 공개")
    @WithAccount("joohyuk")
    @Test
    public void publishSociety() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        assertThat(society.isPublished()).isFalse();
        assertThat(society.isClosed()).isFalse();
        assertThat(society.isRecruiting()).isFalse();

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/society/publish";
        mockMvc.perform(post(requestUrl)
                .with(csrf()))
                .andExpect(flash().attributeExists("message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/society/" + society.getEncodedPath() + "/settings/society"));

        Society findSociety = societyService.getSocietyToUpdateStatus(joohyuk, society.getEncodedPath());

        assertThat(findSociety.isPublished()).isTrue();
        assertThat(society.isClosed()).isFalse();
        assertThat(society.isRecruiting()).isFalse();
    }

    @DisplayName("소모임 닫기")
    @WithAccount("joohyuk")
    @Test
    public void closeSociety() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        societyService.publish(society);

        assertThat(society.isPublished()).isTrue();
        assertThat(society.isClosed()).isFalse();
        assertThat(society.isRecruiting()).isFalse();

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/society/close";
        mockMvc.perform(post(requestUrl)
                .with(csrf()))
                .andExpect(flash().attributeExists("message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/society/" + society.getEncodedPath() + "/settings/society"));

        Society findSociety = societyService.getSocietyToUpdateStatus(joohyuk, society.getEncodedPath());

        assertThat(findSociety.isPublished()).isTrue();
        assertThat(society.isClosed()).isTrue();
        assertThat(society.isRecruiting()).isFalse();
    }

    @DisplayName("소모임 인원 모집 시작 - 성공")
    @WithAccount("joohyuk")
    @Test
    public void startRecruit_Success() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        societyService.publish(society);

        assertThat(society.isPublished()).isTrue();
        assertThat(society.isClosed()).isFalse();
        assertThat(society.isRecruiting()).isFalse();

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/recruit/start";
        mockMvc.perform(post(requestUrl)
                .with(csrf()))
                .andExpect(flash().attribute("message", "인원 모집을 시작합니다."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/society/" + society.getEncodedPath() + "/settings/society"));

        Society findSociety = societyService.getSocietyToUpdateStatus(joohyuk, society.getEncodedPath());

        assertThat(findSociety.isPublished()).isTrue();
        assertThat(society.isClosed()).isFalse();
        assertThat(society.isRecruiting()).isTrue();
    }

    @DisplayName("소모임 인원 모집 시작 - 실패")
    @WithAccount("joohyuk")
    @Test
    public void startRecruit_Fail() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        societyService.publish(society);

        assertThat(society.isPublished()).isTrue();
        assertThat(society.isClosed()).isFalse();
        assertThat(society.isRecruiting()).isFalse();

        societyService.startRecruit(society); // 모집시작

        // 바로 또 모집 시작하는경우 실패
        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/recruit/start";
        mockMvc.perform(post(requestUrl)
                .with(csrf()))
                .andExpect(flash().attribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/society/" + society.getEncodedPath() + "/settings/society"));
    }

//    @DisplayName("소모임 인원 모집 종료 - 성공")
//    @WithAccount("joohyuk")
//    @Test
//    public void stopRecruit_Success() throws Exception {
//        Account joohyuk = accountService.getAccount("joohyuk");
//        Society society = societyFactory.createSociety("test", joohyuk);
//
//        societyService.publish(society);        // 소모임 공개
//
//        assertThat(society.isPublished()).isTrue();
//        assertThat(society.isRecruiting()).isFalse();
//        assertThat(society.isClosed()).isFalse();
//
//        societyService.startRecruit(society);    // 소모임 인원 모집 시작
//
//        assertThat(society.isPublished()).isTrue();
//        assertThat(society.isRecruiting()).isTrue();
//        assertThat(society.isClosed()).isFalse();
//
//        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/recruit/stop";
//        mockMvc.perform(post(requestUrl)
//                .with(csrf()))
//                .andExpect(flash().attribute("message", "인원 모집을 종료합니다."))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/society/" + society.getEncodedPath() + "/settings/society"));
//
//        Society findSociety = societyService.getSocietyToUpdateStatus(joohyuk, society.getEncodedPath());
//
//        assertThat(findSociety.isPublished()).isTrue();
//        assertThat(society.isRecruiting()).isFalse();
//        assertThat(society.isClosed()).isFalse();
//    }

    @DisplayName("소모임 인원 모집 종료 - 실패")
    @WithAccount("joohyuk")
    @Test
    public void stopRecruit_Success() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        societyService.publish(society);        // 소모임 공개

        assertThat(society.isPublished()).isTrue();
        assertThat(society.isRecruiting()).isFalse();
        assertThat(society.isClosed()).isFalse();

        societyService.startRecruit(society);    // 소모임 인원 모집 시작

        assertThat(society.isPublished()).isTrue();
        assertThat(society.isRecruiting()).isTrue();
        assertThat(society.isClosed()).isFalse();

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/recruit/stop";
        mockMvc.perform(post(requestUrl)
                .with(csrf()))
                .andExpect(flash().attribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/society/" + society.getEncodedPath() + "/settings/society"));

        Society findSociety = societyService.getSocietyToUpdateStatus(joohyuk, society.getEncodedPath());

        assertThat(findSociety.isPublished()).isTrue();
        assertThat(society.isRecruiting()).isTrue();
        assertThat(society.isClosed()).isFalse();
    }

    @DisplayName("소모임 경로 수정 - 성공")
    @WithAccount("joohyuk")
    @Test
    public void updateSocietyPath_Success() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/society/path";
        String newPath = "newTest";
        mockMvc.perform(post(requestUrl)
                .param("newPath", newPath)
                .with(csrf()))
                .andExpect(flash().attribute("message", "소모임 경로를 수정했습니다."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/society/" + society.getEncodedPath() + "/settings/society"));

        Society findSociety = societyService.getSocietyToUpdateStatus(joohyuk, newPath);
        assertThat(findSociety).isNotNull();
    }

    @DisplayName("소모임 경로 수정 - 실패")
    @WithAccount("joohyuk")
    @Test
    public void updateSocietyPath_Fail() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/society/path";
        String newPath = "!@#!@#";
        mockMvc.perform(post(requestUrl)
                .param("newPath", newPath)
                .with(csrf()))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("society"))
                .andExpect(model().attributeExists("societyPathError"))
                .andExpect(view().name("society/settings/society"))
                .andExpect(status().isOk());
    }

    @DisplayName("소모임 제목 수정 - 성공")
    @WithAccount("joohyuk")
    @Test
    public void updateSocietyTitle_Success() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/society/title";
        String newTitle = "newTest";
        mockMvc.perform(post(requestUrl)
                .param("newTitle", newTitle)
                .with(csrf()))
                .andExpect(flash().attribute("message", "소모임 이름을 수정했습니다."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/society/" + society.getEncodedPath() + "/settings/society"));

        Society findSociety = societyService.getSocietyToUpdateStatus(joohyuk, "test");
        assertThat(findSociety.getTitle()).isEqualTo(newTitle);
    }

    @DisplayName("소모임 제목 수정 - 실패")
    @WithAccount("joohyuk")
    @Test
    public void updateSocietyTitle_Fail() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/society/title";
        String newTitle = "newTestasdfahsdfhaslkjelfkjlskdjflaksdjflkajsldkjflaksjdlfkjslfdkjflkjlkjsdlfkjs";
        mockMvc.perform(post(requestUrl)
                .param("newTitle", newTitle)
                .with(csrf()))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("society"))
                .andExpect(model().attributeExists("societyTitleError"))
                .andExpect(status().isOk())
                .andExpect(view().name("society/settings/society"));
    }

    @DisplayName("소모임 삭제 - 성공")
    @WithAccount("joohyuk")
    @Test
    public void removeSociety_Success() throws Exception {
        Account joohyuk = accountService.getAccountByNickname("joohyuk");
        Society society = societyFactory.createSociety("test", joohyuk);

        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/society/remove";
        mockMvc.perform(post(requestUrl)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

//    @DisplayName("소모임 삭제 - 실패")
//    @WithAccount("joohyuk")
//    @Test
//    public void removeSociety_fail() throws Exception {
//        Account joohyuk = accountService.getAccount("joohyuk");
//        Society society = societyFactory.createSociety("test", joohyuk);
//
//        societyService.publish(society);  // 소모임 공개
//
//        String requestUrl = "/society/" + society.getEncodedPath() + "/settings/society/remove";
//        mockMvc.perform(post(requestUrl)
//                .with(csrf()))
//                .andExpect(model().hasErrors())
//                .andExpect(status().isBadRequest());
//    }
}
