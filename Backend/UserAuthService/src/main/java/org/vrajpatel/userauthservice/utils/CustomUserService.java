package org.vrajpatel.userauthservice.utils;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.vrajpatel.userauthservice.Exception.AuthenticationOauth2ProcessingException;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.model.AuthProvider;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.utils.Oauth2UserInfo.OAuth2UserInfoFactory;
import org.vrajpatel.userauthservice.utils.Oauth2UserInfo.UserInfo;

import javax.naming.AuthenticationException;
import java.util.Date;
import java.util.Optional;

@Component
public class CustomUserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException, InternalAuthenticationServiceException {
        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2User oAuth2User = super.loadUser(userRequest);

        try{
            return processOauth2User(userRequest,oAuth2User);
        }
        catch (OAuth2AuthenticationException ex){
            throw ex;
        }
        catch (Exception ex){
            throw new InternalAuthenticationServiceException(ex.getMessage(),ex.getCause());
        }
    }

    private OAuth2User processOauth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) throws AuthenticationOauth2ProcessingException {

        UserInfo userInfo= OAuth2UserInfoFactory.getUserInfo(userRequest.getClientRegistration().getRegistrationId(),oAuth2User.getAttributes());
        if(StringUtils.isEmpty(userInfo.getEmail())){
            throw new AuthenticationOauth2ProcessingException("Email not found from the Oauth2 Provider");
        }

        Optional<User> userOptional=userRepository.findByEmail(userInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(!user.getAuthProvider().equals(AuthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId()))) {
                throw new AuthenticationOauth2ProcessingException("Looks like you're signed up with " +
                        user.getAuthProvider() + " account. Please use your " + user.getAuthProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, userInfo);
        } else {
            user = registerNewUser(userRequest, userInfo);
        }
        return UserPrincipal.create(user,oAuth2User.getAttributes());
    }

    @Transactional
    public User registerNewUser(OAuth2UserRequest oAuth2UserRequest, UserInfo userInfo) {
        User user = new User();


        user.setAuthProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(userInfo.getId());
        user.setFirstName(userInfo.getName());

        user.setEmail(userInfo.getEmail().toLowerCase());
        user.setPhotoUrl(userInfo.getPhotoUrl());
        user.setCreatedAt(new Date());
        return userRepository.save(user);
    }

    @Transactional
    public User updateExistingUser(User existingUser, UserInfo userInfo) {

        existingUser.setFirstName(userInfo.getName());
        return userRepository.save(existingUser);
    }
}
