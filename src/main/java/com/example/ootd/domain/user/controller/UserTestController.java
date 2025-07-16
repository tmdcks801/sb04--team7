package com.example.ootd.domain.user.controller;


import com.example.ootd.security.CustomUserDetails;
import com.example.ootd.security.jwt.JwtService;
import com.example.ootd.security.jwt.blacklist.InMemoryBlackList;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트용 컨트롤러 입니다. 추후 삭제 예정입니다.
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class UserTestController {
  private final SessionRegistry sessionRegistry;
  private final JavaMailSender mailSender;
  private final InMemoryBlackList blackList;
  private final JwtService jwtService;
  @GetMapping("/me")
  public String whoAmI(Authentication authentication) {
    return "현재 로그인한 사용자: " + authentication.getName();
  }

  @GetMapping("/sessions")
  public List<Map<String, Object>> getAllLoggedInUsers() {
    List<Object> principals = sessionRegistry.getAllPrincipals();
    List<Map<String, Object>> result = new ArrayList<>();

    for (Object principal : principals) {
      List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);

      for (SessionInformation session : sessions) {
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("sessionId", session.getSessionId());
        sessionInfo.put("lastRequest", session.getLastRequest());
        sessionInfo.put("isExpired", session.isExpired());
        sessionInfo.put("principal", ((CustomUserDetails) session.getPrincipal()).getUser().getEmail());

        result.add(sessionInfo);
      }
    }

    return result;
  }

  @PostMapping("/sessions/invalidate/me")
  public String invalidateMySession(HttpServletRequest request) {
    var session = request.getSession(false);
    if (session == null) {
      return "세션이 존재하지 않습니다.";
    }

    String sessionId = session.getId();
    SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionId);

    if (sessionInfo != null) {
      sessionInfo.expireNow();
    }

    session.invalidate();

    SecurityContextHolder.clearContext();

    return "현재 사용자의 세션이 강제로 만료(로그아웃)되었습니다.";
  }

  @GetMapping("/sendEmail")
  public String send(){
    String tempPassword = "temp";
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo("kevinheo0413@gmail.com");
    message.setSubject("[OOTD] 임시 비밀번호 안내");
    message.setText("임시 비밀번호는 다음과 같습니다:\n\n" + tempPassword + "\n\n로그인 후 비밀번호를 꼭 변경해주세요.");

    mailSender.send(message);

    return "메일을 보냈습니다.";
  }

  @PostMapping("/blacklist")
  public void addTokenToBlacklist(@RequestBody TokenDto token){
    System.out.println(token);
    blackList.addToBlacklist(token.token(), jwtService.extractExpiry(token.token()));
  }
}
