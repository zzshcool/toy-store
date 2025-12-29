package com.toy.store.service;

import com.toy.store.model.MemberMessage;
import com.toy.store.repository.MemberMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 消息通知服務
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MemberMessageRepository messageRepository;

    /**
     * 發送系統消息
     */
    public void sendSystemMessage(Long memberId, String title, String content) {
        MemberMessage msg = new MemberMessage();
        msg.setMemberId(memberId);
        msg.setType(MemberMessage.MessageType.SYSTEM);
        msg.setTitle(title);
        msg.setContent(content);
        messageRepository.save(msg);
    }

    /**
     * 發送中獎通知
     */
    public void sendPrizeMessage(Long memberId, String prizeName, String referenceId) {
        MemberMessage msg = new MemberMessage();
        msg.setMemberId(memberId);
        msg.setType(MemberMessage.MessageType.PRIZE);
        msg.setTitle("🎉 恭喜中獎！");
        msg.setContent("您抽中了【" + prizeName + "】，獎品已進入盒櫃！");
        msg.setReferenceId(referenceId);
        msg.setActionUrl("/cabinet");
        messageRepository.save(msg);
    }

    /**
     * 發送發貨通知
     */
    public void sendShippingMessage(Long memberId, String trackingNumber, String status) {
        MemberMessage msg = new MemberMessage();
        msg.setMemberId(memberId);
        msg.setType(MemberMessage.MessageType.SHIPPING);
        msg.setTitle("📦 發貨狀態更新");
        msg.setContent("您的包裹 " + trackingNumber + " " + status);
        msg.setReferenceId(trackingNumber);
        msg.setActionUrl("/cabinet");
        messageRepository.save(msg);
    }

    /**
     * 發送升級通知
     */
    public void sendLevelUpMessage(Long memberId, String newLevel) {
        MemberMessage msg = new MemberMessage();
        msg.setMemberId(memberId);
        msg.setType(MemberMessage.MessageType.LEVEL_UP);
        msg.setTitle("⬆️ 會員等級提升！");
        msg.setContent("恭喜您升級至【" + newLevel + "】，享受更多專屬優惠！");
        msg.setActionUrl("/profile");
        messageRepository.save(msg);
    }

    /**
     * 發送警告通知（如紅利即將過期）
     */
    public void sendWarningMessage(Long memberId, String title, String content) {
        MemberMessage msg = new MemberMessage();
        msg.setMemberId(memberId);
        msg.setType(MemberMessage.MessageType.WARNING);
        msg.setTitle("⚠️ " + title);
        msg.setContent(content);
        messageRepository.save(msg);
    }

    /**
     * 取得會員所有消息
     */
    public List<MemberMessage> getMessages(Long memberId) {
        return messageRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /**
     * 取得未讀消息
     */
    public List<MemberMessage> getUnreadMessages(Long memberId) {
        return messageRepository.findByMemberIdAndReadFalseOrderByCreatedAtDesc(memberId);
    }

    /**
     * 取得未讀數量
     */
    public long getUnreadCount(Long memberId) {
        return messageRepository.countByMemberIdAndReadFalse(memberId);
    }

    /**
     * 標記為已讀
     */
    @Transactional
    public void markAsRead(Long messageId) {
        messageRepository.findById(messageId).ifPresent(msg -> {
            msg.setRead(true);
            messageRepository.save(msg);
        });
    }

    /**
     * 標記全部已讀
     */
    @Transactional
    public void markAllAsRead(Long memberId) {
        List<MemberMessage> unread = messageRepository.findByMemberIdAndReadFalseOrderByCreatedAtDesc(memberId);
        unread.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(unread);
    }
}
