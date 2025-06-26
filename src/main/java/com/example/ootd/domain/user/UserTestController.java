package com.example.ootd.domain.user;


import com.example.ootd.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

}
