package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.exception.AppException;
import com.toy.store.model.GachaRecord;
import com.toy.store.model.Member;
import com.toy.store.model.Transaction;
import com.toy.store.repository.GachaRecordRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.TransactionRepository;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 會員紀錄控制器：交易紀錄、抽獎紀錄
 */
@Controller
@RequiredArgsConstructor
public class RecordController {

    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final GachaRecordRepository gachaRecordRepository;

    /**
     * 交易紀錄頁面
     */
    @GetMapping("/transactions")
    public String transactions(@CurrentUser TokenService.TokenInfo info, Model model) {
        if (info == null) {
            return "redirect:/login";
        }

        Member member = memberRepository.findByUsername(info.getUsername())
                .orElseThrow(() -> new AppException("找不到會員資料"));
        List<Transaction> transactions = transactionRepository.findByMemberIdOrderByTimestampDesc(member.getId());

        model.addAttribute("transactions", transactions);
        model.addAttribute("member", member);
        return "transactions";
    }

    /**
     * 抽獎紀錄頁面
     */
    @GetMapping("/gacha-history")
    public String gachaHistory(@CurrentUser TokenService.TokenInfo info, Model model) {
        if (info == null) {
            return "redirect:/login";
        }

        Member member = memberRepository.findByUsername(info.getUsername())
                .orElseThrow(() -> new AppException("找不到會員資料"));
        List<GachaRecord> records = gachaRecordRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());

        model.addAttribute("records", records);
        model.addAttribute("member", member);
        return "gacha-history";
    }
}
