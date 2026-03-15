package com.codexgym.gym.application;

import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.dto.MemberResponse;
import com.codexgym.gym.dto.TelegramConnectSessionResponse;
import com.codexgym.gym.dto.TelegramConnectionSyncResponse;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.TelegramConnectionToken;
import com.codexgym.gym.exception.ApiException;
import com.codexgym.gym.repository.MemberRepository;
import com.codexgym.gym.repository.TelegramConnectionTokenRepository;
import com.codexgym.gym.service.GymMapper;
import com.codexgym.gym.service.telegram.TelegramGateway;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TelegramConnectionService {

    private static final String CONNECT_PREFIX = "connect_";

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final TelegramConnectionTokenRepository telegramConnectionTokenRepository;
    private final NotificationProperties notificationProperties;
    private final TelegramGateway telegramGateway;
    private final GymMapper gymMapper;
    private final Clock clock;

    @Transactional
    public TelegramConnectSessionResponse createConnectSession(UUID memberId) {
        NotificationProperties.TelegramProperties properties = notificationProperties.getTelegram();
        ensureConnectFlowConfigured(properties);

        Member member = memberService.findMemberEntity(memberId);
        telegramConnectionTokenRepository.deleteByMember_IdAndConsumedAtIsNull(memberId);

        TelegramConnectionToken token = new TelegramConnectionToken();
        token.setMember(member);
        token.setToken(generateConnectToken());
        token.setExpiresAt(LocalDateTime.now(clock).plusMinutes(properties.getConnectTokenTtlMinutes()));
        TelegramConnectionToken savedToken = telegramConnectionTokenRepository.save(token);

        return new TelegramConnectSessionResponse(
                member.getId(),
                member.getFullName(),
                savedToken.getToken(),
                buildDeepLink(savedToken.getToken(), properties),
                savedToken.getExpiresAt(),
                member.getTelegramChatId()
        );
    }

    @Transactional
    public MemberResponse linkMemberToChat(UUID memberId, String telegramChatId) {
        Member member = memberService.findMemberEntity(memberId);
        String normalizedChatId = normalizeChatId(telegramChatId);

        Optional<Member> existingMember = memberRepository.findByTelegramChatId(normalizedChatId);
        if (existingMember.isPresent() && !existingMember.get().getId().equals(memberId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Telegram chat ID is already linked to another member");
        }

        member.setTelegramChatId(normalizedChatId);
        telegramConnectionTokenRepository.deleteByMember_IdAndConsumedAtIsNull(memberId);
        return gymMapper.toResponse(memberRepository.save(member));
    }

    @Transactional
    public TelegramConnectionSyncResponse syncConnections() {
        NotificationProperties.TelegramProperties properties = notificationProperties.getTelegram();
        ensureConnectFlowConfigured(properties);

        List<TelegramGateway.TelegramUpdate> updates = telegramGateway.getUpdates();
        List<TelegramConnectionSyncResponse.TelegramConnectionResult> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now(clock);
        int matchedConnectRequests = 0;
        int linkedMembers = 0;

        for (TelegramGateway.TelegramUpdate update : updates) {
            String tokenValue = extractConnectToken(update);
            if (tokenValue == null) {
                continue;
            }

            matchedConnectRequests++;
            TelegramGateway.TelegramMessage message = update.message();
            String chatId = message == null || message.chat() == null || message.chat().id() == null
                    ? null
                    : String.valueOf(message.chat().id());

            Optional<TelegramConnectionToken> tokenOptional = telegramConnectionTokenRepository.findByToken(tokenValue);
            if (tokenOptional.isEmpty()) {
                results.add(new TelegramConnectionSyncResponse.TelegramConnectionResult(
                        null,
                        null,
                        chatId,
                        "IGNORED",
                        "No pending connect request matches this Telegram start code"
                ));
                continue;
            }

            TelegramConnectionToken token = tokenOptional.get();
            Member member = token.getMember();

            if (chatId == null) {
                results.add(new TelegramConnectionSyncResponse.TelegramConnectionResult(
                        member.getId(),
                        member.getFullName(),
                        null,
                        "IGNORED",
                        "Telegram update did not include a private chat ID"
                ));
                continue;
            }

            if (token.getConsumedAt() != null) {
                results.add(new TelegramConnectionSyncResponse.TelegramConnectionResult(
                        member.getId(),
                        member.getFullName(),
                        member.getTelegramChatId(),
                        "ALREADY_LINKED",
                        "This Telegram connect request was already used"
                ));
                continue;
            }

            if (token.getExpiresAt().isBefore(now)) {
                results.add(new TelegramConnectionSyncResponse.TelegramConnectionResult(
                        member.getId(),
                        member.getFullName(),
                        null,
                        "EXPIRED",
                        "The connect request expired before the member started the bot"
                ));
                continue;
            }

            Optional<Member> existingMember = memberRepository.findByTelegramChatId(chatId);
            if (existingMember.isPresent() && !existingMember.get().getId().equals(member.getId())) {
                results.add(new TelegramConnectionSyncResponse.TelegramConnectionResult(
                        member.getId(),
                        member.getFullName(),
                        chatId,
                        "CONFLICT",
                        "That Telegram chat is already linked to another member"
                ));
                continue;
            }

            member.setTelegramChatId(chatId);
            memberRepository.save(member);

            token.setChatId(chatId);
            token.setConsumedAt(now);
            telegramConnectionTokenRepository.save(token);

            linkedMembers++;
            results.add(new TelegramConnectionSyncResponse.TelegramConnectionResult(
                    member.getId(),
                    member.getFullName(),
                    chatId,
                    "LINKED",
                    "Telegram chat linked successfully"
            ));
        }

        return new TelegramConnectionSyncResponse(updates.size(), matchedConnectRequests, linkedMembers, results);
    }

    private void ensureConnectFlowConfigured(NotificationProperties.TelegramProperties properties) {
        if (!properties.isEnabled()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Telegram integration is disabled");
        }
        if (properties.isDryRun()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Telegram integration is in dry-run mode");
        }
        if (!hasText(properties.getBotToken())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Telegram bot token is not configured");
        }
        if (!hasText(properties.getBotUsername())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Telegram bot username is not configured");
        }
    }

    private String buildDeepLink(String token, NotificationProperties.TelegramProperties properties) {
        String baseUrl = properties.getDeepLinkBaseUrl() == null || properties.getDeepLinkBaseUrl().isBlank()
                ? "https://t.me"
                : properties.getDeepLinkBaseUrl().trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + properties.getBotUsername().trim() + "?start=" + CONNECT_PREFIX + token;
    }

    private String extractConnectToken(TelegramGateway.TelegramUpdate update) {
        if (update == null || update.message() == null || !hasText(update.message().text())) {
            return null;
        }

        String[] parts = update.message().text().trim().split("\\s+", 2);
        if (parts.length < 2) {
            return null;
        }

        String command = parts[0].trim();
        if (!command.equals("/start") && !command.startsWith("/start@")) {
            return null;
        }

        String payload = parts[1].trim();
        if (!payload.startsWith(CONNECT_PREFIX)) {
            return null;
        }

        String token = payload.substring(CONNECT_PREFIX.length()).trim();
        return token.isBlank() ? null : token;
    }

    private String generateConnectToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String normalizeChatId(String chatId) {
        return chatId == null ? null : chatId.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
