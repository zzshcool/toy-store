package com.toy.store.service;

import com.toy.store.dto.SignupRequest;
import com.toy.store.exception.AppException;
import com.toy.store.model.Member;
import com.toy.store.model.MemberLevel;
import com.toy.store.model.Transaction;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.mapper.MemberLevelMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 會員業務服務
 * 處理註冊、登入、資料更新與儲值等核心邏輯
 */
@Service
@Slf4j
public class MemberService {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final TransactionService transactionService;
    private final EmailVerificationService emailVerificationService;
    private final SignInService signInService;

    public MemberService(
            MemberMapper memberMapper,
            MemberLevelMapper memberLevelMapper,
            PasswordEncoder encoder,
            TokenService tokenService,
            @Lazy TransactionService transactionService,
            EmailVerificationService emailVerificationService,
            @Lazy SignInService signInService) {
        this.memberMapper = memberMapper;
        this.memberLevelMapper = memberLevelMapper;
        this.encoder = encoder;
        this.tokenService = tokenService;
        this.transactionService = transactionService;
        this.emailVerificationService = emailVerificationService;
        this.signInService = signInService;
    }

    /**
     * 處理會員登入
     * 
     * @return 登入成功後生成的 Token
     */
    public String login(String username, String password, String captcha, HttpSession session,
            HttpServletResponse response) {
        // 驗證碼校驗
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
            throw new AppException("驗證碼不正確");
        }

        Member member = memberMapper.findByUsername(username)
                .orElseThrow(() -> new AppException("帳號或密碼錯誤"));

        if (!encoder.matches(password, member.getPassword())) {
            throw new AppException("帳號或密碼錯誤");
        }

        // 生成 Token
        String token = tokenService.createToken(member.getUsername(), TokenService.ROLE_USER);

        // 設定 Cookie
        Cookie cookie = new Cookie("AUTH_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600 * 24 * 7); // 7 days
        response.addCookie(cookie);

        // 更新最後登入時間
        member.setLastLoginTime(LocalDateTime.now());
        memberMapper.update(member);

        // 觸發每日簽到與任務
        try {
            signInService.processDailySignIn(member.getId());
        } catch (Exception e) {
            log.error("Failed to process daily sign-in for member {}: {}", member.getUsername(), e.getMessage());
        }

        return token;
    }

    /**
     * 驗證登入（不需驗證碼，供 API 登入使用）
     */
    public Member validateLogin(String username, String password) {
        Member member = memberMapper.findByUsername(username).orElse(null);
        if (member == null || !encoder.matches(password, member.getPassword())) {
            return null;
        }
        // 更新最後登入時間
        member.setLastLoginTime(LocalDateTime.now());
        memberMapper.update(member);
        return member;
    }

    /**
     * 處理會員註冊
     */
    @Transactional
    public void register(SignupRequest signUpRequest, HttpSession session) {
        if (Boolean.TRUE.equals(memberMapper.existsByUsername(signUpRequest.getUsername()))) {
            throw new AppException("此帳號已被註冊");
        }

        if (Boolean.TRUE.equals(memberMapper.existsByEmail(signUpRequest.getEmail()))) {
            throw new AppException("此 Email 已被使用");
        }

        // 驗證碼校驗
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(signUpRequest.getCaptcha())) {
            throw new AppException("驗證碼不正確");
        }

        // 條款同意校驗
        if (!signUpRequest.isAgreedTerms()) {
            throw new AppException("您必須同意會員條款與隱私權政策才能註冊");
        }

        // Email 驗證狀態校驗
        if (!emailVerificationService.isEmailVerified(signUpRequest.getEmail(), session)) {
            throw new AppException("您的 Email 尚未通過驗證");
        }

        // 建立新會員
        Member member = new Member();
        member.setUsername(signUpRequest.getUsername());
        member.setEmail(signUpRequest.getEmail());
        member.setPhone(signUpRequest.getPhone());
        member.setRealName(signUpRequest.getRealName());
        member.setAddress(signUpRequest.getAddress());
        member.setGender(signUpRequest.getGender());
        member.setBirthday(signUpRequest.getBirthday());
        member.setPassword(encoder.encode(signUpRequest.getPassword()));
        member.setRole(Member.Role.USER);
        member.setPlatformWalletBalance(BigDecimal.ZERO);
        member.setCreatedAt(LocalDateTime.now());

        // 設定暱稱
        String nickname = signUpRequest.getNickname();
        member.setNickname(
                nickname != null && !nickname.trim().isEmpty() ? nickname.trim() : signUpRequest.getUsername());

        // 初始化會員等級 (自動指派最低等級)
        List<MemberLevel> levels = memberLevelMapper.findAll();
        if (!levels.isEmpty()) {
            member.setMemberLevelId(levels.get(0).getId());
        }

        memberMapper.insert(member);
    }

    /**
     * 更新個人資料
     */
    @Transactional
    public void updateProfile(String username, String nickname, String email, String phone,
            String realName, String address, String gender, java.time.LocalDate birthday) {
        Member member = memberMapper.findByUsername(username)
                .orElseThrow(() -> new AppException("會員不存在"));

        member.setNickname(nickname);
        member.setEmail(email);
        member.setPhone(phone);
        member.setRealName(realName);
        member.setAddress(address);
        member.setGender(gender);
        member.setBirthday(birthday);

        memberMapper.update(member);
    }

    /**
     * 處理儲值
     */
    @Transactional
    public Member topup(String username, BigDecimal amount, String paymentMethod) {
        Member member = memberMapper.findByUsername(username)
                .orElseThrow(() -> new AppException("會員不存在"));

        transactionService.updateWalletBalance(
                member.getId(),
                amount,
                Transaction.Type.RECHARGE,
                "TOPUP-" + paymentMethod);

        Long id = member.getId();
        if (id == null) {
            throw new AppException("會員儲存失敗");
        }
        return memberMapper.findById(id).orElseThrow();
    }

    /**
     * 發送驗證碼
     */
    public void sendVerifyCode(String email, HttpSession session) {
        if (Boolean.TRUE.equals(memberMapper.existsByEmail(email))) {
            throw new AppException("此 Email 已被註冊");
        }
        emailVerificationService.generateAndSaveCode(email, session);
    }

    /**
     * 驗證驗證碼
     */
    public boolean verifyCode(String email, String code, HttpSession session) {
        return emailVerificationService.verifyCode(email, code, session);
    }

    /**
     * 增加會員積分 (Points)
     */
    @Transactional
    public void addPoints(Long memberId, int amount) {
        if (memberId == null)
            return;
        memberMapper.findById(memberId).ifPresent(member -> {
            member.setPoints(member.getPoints() + amount);
            memberMapper.update(member);
            log.info("Member {} earned {} points", member.getUsername(), amount);
        });
    }

    /**
     * 增加會員紅利點數 (BonusPoints)
     */
    @Transactional
    public void addBonusPoints(Long memberId, int amount) {
        if (memberId == null)
            return;
        memberMapper.findById(memberId).ifPresent(member -> {
            member.setBonusPoints(member.getBonusPoints() + amount);
            memberMapper.update(member);
            log.info("Member {} earned {} bonus points", member.getUsername(), amount);
        });
    }

    /**
     * 根據 ID 查詢會員
     */
    public Optional<Member> findById(Long id) {
        return memberMapper.findById(id);
    }

    /**
     * 根據使用者名稱查詢會員
     */
    public Optional<Member> findByUsername(String username) {
        return memberMapper.findByUsername(username);
    }

    /**
     * 儲存會員
     */
    @Transactional
    public Member save(Member member) {
        if (member.getId() == null) {
            memberMapper.insert(member);
        } else {
            memberMapper.update(member);
        }
        return member;
    }

    /**
     * 查詢所有會員
     */
    public List<Member> findAll() {
        return memberMapper.findAll();
    }
}
