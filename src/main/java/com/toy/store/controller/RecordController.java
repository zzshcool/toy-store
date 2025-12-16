package com.toy.store.controller;

import com.toy.store.model.Member;
import com.toy.store.model.Transaction;
import com.toy.store.model.GachaRecord;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.TransactionRepository;
import com.toy.store.repository.GachaRecordRepository;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 會員紀錄控制器：交易紀錄、抽獎紀錄
 */
@Controller
public class RecordController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private GachaRecordRepository gachaRecordRepository;

    /**
     * 交易紀錄頁面
     */
    @GetMapping("/transactions")
    public String transactions(HttpServletRequest request, Model model) {
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (info == null) {
            return "redirect:/login";
        }

        Member member = memberRepository.findByUsername(info.getUsername()).orElseThrow();
        List<Transaction> transactions = transactionRepository.findByMemberIdOrderByTimestampDesc(member.getId());

        model.addAttribute("transactions", transactions);
        model.addAttribute("member", member);
        return "transactions";
    }

    /**
     * 抽獎紀錄頁面
     */
    @GetMapping("/gacha-history")
    public String gachaHistory(HttpServletRequest request, Model model) {
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (info == null) {
            return "redirect:/login";
        }

        Member member = memberRepository.findByUsername(info.getUsername()).orElseThrow();
        List<GachaRecord> records = gachaRecordRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());

        model.addAttribute("records", records);
        model.addAttribute("member", member);
        return "gacha-history";
    }
}
