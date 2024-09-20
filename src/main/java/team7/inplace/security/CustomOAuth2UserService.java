package team7.inplace.security;

import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest)
        throws OAuth2AuthenticationException {
        String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        if (!provider.equals(UserType.KAKAO.getProvider())) {
            return null;
        }

        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        KakaoOAuthResponse kakaoOAuthResponse = new KakaoOAuthResponse(oAuth2User.getAttributes());

        CustomOAuth2User customOAuth2User = updateUser(kakaoOAuthResponse);
        return customOAuth2User;
    }

    private CustomOAuth2User updateUser(KakaoOAuthResponse kakaoOAuthResponse) {
        User existUser = userRepository.findByUsername(kakaoOAuthResponse.getEmail());
        if (existUser != null) {
            existUser.updateInfo(kakaoOAuthResponse.getNickname());
            userRepository.save(existUser);
        }
        return new CustomOAuth2User(kakaoOAuthResponse.getEmail(), kakaoOAuthResponse.getNickname(), UserType.KAKAO);
    }
}
