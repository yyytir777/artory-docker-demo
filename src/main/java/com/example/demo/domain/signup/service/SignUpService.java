//package com.example.demo.domain.signup.service;
//
//import com.example.demo.domain.member.entity.Member;
//import com.example.demo.domain.member.repository.MemberRepository;
//import com.example.demo.domain.signup.dto.SignUpRequestDto;
//import com.example.demo.global.error.ErrorCode;
//import com.example.demo.global.error.exception.SignupException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Service
//public class SignUpService {
//
//    @Transactional
//    public void signUp(SignUpRequestDto signUpRequestDto){
//        //이메일
//        if (!isValidEmail(signUpRequestDto.getEmail())) {
//            throw new SignupException(ErrorCode.INVALID_EMAIL_FORMAT);
//        }
//        //비밀번호
//        if (!isValidPassword(signUpRequestDto.getPassword())) {
//            throw new SignupException(ErrorCode.INVALID_PASSWORD_FORMAT);
//        }
//        // 비밀번호 일치여부
//        if (!signUpRequestDto.getPassword().equals(signUpRequestDto.getConfirmPassword())) {
//            throw new SignupException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
//        }
//        // 약관 동의
//        if (!signUpRequestDto.isTermsAgreed()) { // 이용약관 동의 확인 (필수)
//            throw new SignupException(ErrorCode.TERMS_AGREEMENT_REQUIRED);
//        }
//
//        if (!signUpRequestDto.isPrivacyPolicyAgreed()) { // 개인정보 취급방침 (필수)
//            throw new SignupException(ErrorCode.PRIVACY_POLICY_AGREEMENT_REQUIRED);
//        }
//
//        // 마케팅 정보 수신 동의 (선택)
//        boolean isMarketingAgreed = Optional.ofNullable(signUpRequestDto.isMarketingAgreed()).orElse(false);
//
//        Member member = new Member();
//        member.setEmail(signUpRequestDto.getEmail());
//        member.setPassword(signUpRequestDto.getPassword());
//        member.setConfirmPassword(signUpRequestDto.getConfirmPassword());
//        member.setMemberName(signUpRequestDto.getMemberName());
//        member.setPhoneNum(signUpRequestDto.getPhoneNum());
//        member.setBirth(signUpRequestDto.getBirth());
//
//
//    }
//    //이메일 유효성 검사
//    private boolean isValidEmail(String email) {
//        // 간단한 형식의 이메일 유효성 검사 정규식
//        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
//        Pattern pattern = Pattern.compile(emailRegex);
//        Matcher matcher = pattern.matcher(email);
//        return matcher.matches();
//    }
//    //비밀번호 형식
//    private boolean isValidPassword(String password) {
//        //최소 8자리 이상, 대문자, 소문자, 숫자, 특수문자가 각각 하나 이상 포함
//        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[?!*#]).{8,}$";
//        Pattern pattern = Pattern.compile(passwordRegex);
//        Matcher matcher = pattern.matcher(password);
//        return matcher.matches();
//    }
//}