package com.codexgym.gym.service;

import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.messaging.NotificationAttachment;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberCardDocumentFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH);

    private final NotificationProperties notificationProperties;

    public NotificationAttachment buildMemberCard(Member member, Membership membership) {
        String membershipPlan = membership == null ? "Membership not active yet" : membership.getPlan().getName();
        String membershipExpiry = membership == null ? "Awaiting first enrollment" : membership.getEndDate().format(DATE_FORMATTER);
        String membershipStatus = membership == null ? "PENDING" : membership.getStatus().name().replace('_', ' ');
        String cardImage = buildMemberPhotoMarkup(member);
        String supportEmail = hasText(notificationProperties.getEmail().getFrom()) ? notificationProperties.getEmail().getFrom().trim() : "Front desk support";
        String supportTelegram = hasText(notificationProperties.getTelegram().getBotUsername())
                ? "@" + notificationProperties.getTelegram().getBotUsername().trim()
                : "Telegram bot not linked";
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>%s member card</title>
                    <style>
                      :root {
                        color-scheme: light;
                        font-family: "Segoe UI", sans-serif;
                        --ink: #12202c;
                        --muted: #61707b;
                        --line: rgba(18, 32, 44, 0.14);
                        --accent: #ff6b35;
                        --accent-soft: rgba(255, 107, 53, 0.14);
                        --surface: #fffaf3;
                        --shadow: 0 24px 60px rgba(18, 32, 44, 0.16);
                      }
                      * { box-sizing: border-box; }
                      body {
                        margin: 0;
                        padding: 24px;
                        background: linear-gradient(160deg, #fbf7ef 0%%, #efe1ce 100%%);
                        color: var(--ink);
                      }
                      .card-shell {
                        max-width: 920px;
                        margin: 0 auto;
                        padding: 28px;
                        border-radius: 28px;
                        background: linear-gradient(145deg, rgba(255, 250, 243, 0.98), rgba(245, 232, 214, 0.96));
                        border: 1px solid var(--line);
                        box-shadow: var(--shadow);
                      }
                      .hero {
                        display: grid;
                        grid-template-columns: 220px minmax(0, 1fr);
                        gap: 24px;
                        align-items: stretch;
                      }
                      .photo {
                        min-height: 280px;
                        border-radius: 24px;
                        overflow: hidden;
                        background: linear-gradient(160deg, rgba(255, 107, 53, 0.12), rgba(17, 124, 143, 0.10));
                        display: flex;
                        align-items: center;
                        justify-content: center;
                      }
                      .photo img {
                        width: 100%%;
                        height: 100%%;
                        object-fit: cover;
                      }
                      .photo__placeholder {
                        display: grid;
                        place-items: center;
                        width: 100%%;
                        height: 100%%;
                        font-size: 48px;
                        font-weight: 700;
                        color: #9a4d1d;
                        letter-spacing: 0.08em;
                      }
                      .brand {
                        display: flex;
                        justify-content: space-between;
                        gap: 16px;
                        align-items: start;
                      }
                      .brand__eyebrow {
                        margin: 0;
                        color: var(--accent);
                        font-size: 13px;
                        font-weight: 700;
                        letter-spacing: 0.16em;
                        text-transform: uppercase;
                      }
                      h1, h2, p {
                        margin: 0;
                      }
                      h1 {
                        margin-top: 8px;
                        font-size: 34px;
                      }
                      .status-pill {
                        padding: 10px 14px;
                        border-radius: 999px;
                        background: var(--accent-soft);
                        color: #b24c18;
                        font-weight: 700;
                      }
                      .meta-grid,
                      .detail-grid {
                        display: grid;
                        grid-template-columns: repeat(3, minmax(0, 1fr));
                        gap: 16px;
                        margin-top: 24px;
                      }
                      .meta-card,
                      .detail-card {
                        padding: 18px;
                        border-radius: 20px;
                        border: 1px solid var(--line);
                        background: rgba(255, 255, 255, 0.86);
                      }
                      .meta-card span,
                      .detail-card span {
                        color: var(--muted);
                        font-size: 12px;
                        letter-spacing: 0.08em;
                        text-transform: uppercase;
                      }
                      .meta-card strong,
                      .detail-card strong {
                        display: block;
                        margin-top: 8px;
                        font-size: 20px;
                      }
                      .contact {
                        margin-top: 24px;
                        padding: 18px;
                        border-radius: 20px;
                        border: 1px solid var(--line);
                        background: rgba(18, 32, 44, 0.03);
                      }
                      .contact p + p { margin-top: 6px; }
                      .muted { color: var(--muted); }
                      @media (max-width: 760px) {
                        .hero,
                        .meta-grid,
                        .detail-grid { grid-template-columns: 1fr; }
                      }
                    </style>
                  </head>
                  <body>
                    <main class="card-shell">
                      <section class="hero">
                        <div class="photo">%s</div>
                        <div>
                          <div class="brand">
                            <div>
                              <p class="brand__eyebrow">%s</p>
                              <h1>Member Card</h1>
                              <p class="muted">Valid gym identity and membership summary</p>
                            </div>
                            <div class="status-pill">%s</div>
                          </div>

                          <div class="meta-grid">
                            <article class="meta-card">
                              <span>Member</span>
                              <strong>%s</strong>
                              <p class="muted">%s</p>
                            </article>
                            <article class="meta-card">
                              <span>Member ID</span>
                              <strong>%s</strong>
                              <p class="muted">Joined %s</p>
                            </article>
                            <article class="meta-card">
                              <span>Membership</span>
                              <strong>%s</strong>
                              <p class="muted">Expires %s</p>
                            </article>
                          </div>

                          <div class="detail-grid">
                            <article class="detail-card">
                              <span>Phone</span>
                              <strong>%s</strong>
                            </article>
                            <article class="detail-card">
                              <span>Email</span>
                              <strong>%s</strong>
                            </article>
                            <article class="detail-card">
                              <span>Telegram</span>
                              <strong>%s</strong>
                            </article>
                          </div>

                          <div class="contact">
                            <p><strong>Gym details</strong></p>
                            <p class="muted">Support email: %s</p>
                            <p class="muted">Telegram bot: %s</p>
                          </div>
                        </div>
                      </section>
                    </main>
                  </body>
                </html>
                """.formatted(
                escapeHtml(member.getMemberCode()),
                cardImage,
                escapeHtml(notificationProperties.getBrandName()),
                escapeHtml(membershipStatus),
                escapeHtml(member.getFullName()),
                escapeHtml(member.getStatus().name().replace('_', ' ')),
                escapeHtml(member.getMemberCode()),
                escapeHtml(member.getCreatedAt().toLocalDate().format(DATE_FORMATTER)),
                escapeHtml(membershipPlan),
                escapeHtml(membershipExpiry),
                escapeHtml(member.getPhoneNumber()),
                escapeHtml(valueOrFallback(member.getEmail(), "No email linked")),
                escapeHtml(valueOrFallback(member.getTelegramChatId(), "Not linked yet")),
                escapeHtml(supportEmail),
                escapeHtml(supportTelegram)
        );

        return new NotificationAttachment(
                member.getMemberCode() + "-card.html",
                "text/html",
                html.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String buildMemberPhotoMarkup(Member member) {
        if (member.getProfileImage() == null || member.getProfileImage().length == 0) {
            return "<div class=\"photo__placeholder\">" + escapeHtml(initials(member)) + "</div>";
        }

        String contentType = hasText(member.getProfileImageContentType()) ? member.getProfileImageContentType() : "image/jpeg";
        String dataUrl = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(member.getProfileImage());
        return "<img src=\"" + dataUrl + "\" alt=\"" + escapeHtml(member.getFullName()) + "\" />";
    }

    private String initials(Member member) {
        StringBuilder initials = new StringBuilder();
        if (hasText(member.getFirstName())) {
            initials.append(Character.toUpperCase(member.getFirstName().charAt(0)));
        }
        if (hasText(member.getLastName())) {
            initials.append(Character.toUpperCase(member.getLastName().charAt(0)));
        }
        return initials.isEmpty() ? "GF" : initials.toString();
    }

    private String valueOrFallback(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}