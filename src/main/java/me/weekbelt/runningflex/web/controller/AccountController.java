package me.weekbelt.runningflex.web.controller;

import lombok.RequiredArgsConstructor;
import me.weekbelt.runningflex.domain.account.Account;
import me.weekbelt.runningflex.domain.account.AccountRepository;
import me.weekbelt.runningflex.domain.account.AccountService;
import me.weekbelt.runningflex.web.dto.account.SignUpForm;
import me.weekbelt.runningflex.web.dto.account.SignUpFormValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    // 특정 컨트롤러에서 바인딩 또는 검증 설정을 변경하고 싶을 때 사용
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        accountService.processNewAccount(signUpForm);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model){
        String view = "account/checked-email";
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!account.getEmailCheckToken().equals(token)){
            model.addAttribute("error", "wrong.token");
            return view;
        }

        account.setEmailVerified(true);
        account.setJoinedAt(LocalDateTime.now());
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());

        return view;
    }
}
