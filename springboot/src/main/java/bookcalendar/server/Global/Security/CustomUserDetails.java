package bookcalendar.server.global.Security;


import bookcalendar.server.Domain.Member.Entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Integer memberId;
    private final String nickname;
    private final String role;

    public CustomUserDetails(Member member) {
        this.memberId = member.getMemberId();
        this.nickname = member.getNickName();
        this.role = member.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> "ROLE_" + role);
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return nickname; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}