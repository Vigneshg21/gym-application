package com.codexgym.gym.service;

import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.messaging.NotificationAttachment;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceDocumentFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH);
    private static final Locale INDIAN_ENGLISH = Locale.forLanguageTag("en-IN");

    private final NotificationProperties notificationProperties;

    public NotificationAttachment buildReceiptDocument(Invoice invoice) {
        Member member = invoice.getMember();
        String invoiceType = invoice.getType() == null ? "Invoice" : formatEnumLabel(invoice.getType().name());
        String invoiceStatus = invoice.getStatus() == null ? "Unknown" : formatEnumLabel(invoice.getStatus().name());
        String notesMarkup = hasText(invoice.getNotes())
                ? "<section class=\"card notes\"><h3>Notes</h3><p>" + escapeHtml(invoice.getNotes()).replace("\n", "<br />") + "</p></section>"
                : "";
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>%s - Payment Receipt</title>
                    <style>
                      :root {
                        color-scheme: light;
                        font-family: "Segoe UI", sans-serif;
                        --bg: #f6f0e6;
                        --card: #fffaf3;
                        --line: #e5d8c3;
                        --text: #192431;
                        --muted: #5c6972;
                        --accent: #ff6b35;
                      }
                      * { box-sizing: border-box; }
                      body {
                        margin: 0;
                        padding: 24px;
                        background: linear-gradient(160deg, #fbf7ef 0%%, #f1e4d1 100%%);
                        color: var(--text);
                      }
                      .sheet {
                        max-width: 860px;
                        margin: 0 auto;
                        padding: 28px;
                        border: 1px solid var(--line);
                        border-radius: 24px;
                        background: var(--card);
                      }
                      .brand {
                        color: var(--accent);
                        font-size: 13px;
                        font-weight: 700;
                        letter-spacing: 0.16em;
                        text-transform: uppercase;
                      }
                      h1, h2, h3, p { margin: 0; }
                      h1 { margin-top: 8px; font-size: 32px; }
                      .muted { color: var(--muted); }
                      .grid {
                        display: grid;
                        grid-template-columns: repeat(3, minmax(0, 1fr));
                        gap: 16px;
                        margin-top: 24px;
                      }
                      .card {
                        padding: 18px;
                        border: 1px solid var(--line);
                        border-radius: 18px;
                        background: #fff;
                      }
                      .card span {
                        color: var(--muted);
                        font-size: 13px;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                      }
                      .card strong {
                        display: block;
                        margin-top: 8px;
                        font-size: 18px;
                      }
                      table {
                        width: 100%%;
                        margin-top: 24px;
                        border-collapse: collapse;
                        overflow: hidden;
                        border: 1px solid var(--line);
                        border-radius: 18px;
                        background: #fff;
                      }
                      th, td {
                        padding: 14px 18px;
                        border-bottom: 1px solid var(--line);
                        text-align: left;
                      }
                      th {
                        background: rgba(255, 107, 53, 0.06);
                        color: var(--muted);
                        font-size: 13px;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                      }
                      tr:last-child td { border-bottom: 0; }
                      .notes { margin-top: 24px; }
                      .footer { margin-top: 24px; color: var(--muted); font-size: 14px; }
                    </style>
                  </head>
                  <body>
                    <main class="sheet">
                      <div class="brand">%s</div>
                      <h1>Payment Receipt</h1>
                      <p class="muted">%s for %s</p>

                      <section class="grid">
                        <article class="card">
                          <span>Member</span>
                          <strong>%s</strong>
                          %s
                        </article>
                        <article class="card">
                          <span>Invoice Type</span>
                          <strong>%s</strong>
                        </article>
                        <article class="card">
                          <span>Status</span>
                          <strong>%s</strong>
                          <div class="muted">Issued %s</div>
                        </article>
                      </section>

                      <table>
                        <thead>
                          <tr>
                            <th>Description</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr>
                            <td>Due date</td>
                            <td>%s</td>
                          </tr>
                          <tr>
                            <td>Base amount</td>
                            <td>%s</td>
                          </tr>
                          <tr>
                            <td>Tax</td>
                            <td>%s</td>
                          </tr>
                          <tr>
                            <td>Discount</td>
                            <td>%s</td>
                          </tr>
                          <tr>
                            <td>Total billed</td>
                            <td>%s</td>
                          </tr>
                          <tr>
                            <td>Total paid</td>
                            <td>%s</td>
                          </tr>
                          <tr>
                            <td>Balance due</td>
                            <td>%s</td>
                          </tr>
                        </tbody>
                      </table>

                      %s

                      <p class="footer">Generated by %s. This receipt can be downloaded, printed, or saved as PDF.</p>
                    </main>
                  </body>
                </html>
                """.formatted(
                escapeHtml(invoice.getInvoiceNumber()),
                escapeHtml(notificationProperties.getBrandName()),
                escapeHtml(invoice.getInvoiceNumber()),
                escapeHtml(member.getFullName()),
                escapeHtml(member.getFullName()),
                buildMemberContactMarkup(member),
                escapeHtml(invoiceType),
                escapeHtml(invoiceStatus),
                escapeHtml(invoice.getIssueDate().format(DATE_FORMATTER)),
                escapeHtml(invoice.getDueDate().format(DATE_FORMATTER)),
                escapeHtml(formatCurrency(invoice.getAmount())),
                escapeHtml(formatCurrency(invoice.getTaxAmount())),
                escapeHtml(formatCurrency(invoice.getDiscountAmount())),
                escapeHtml(formatCurrency(invoice.getTotalAmount())),
                escapeHtml(formatCurrency(invoice.getAmountPaid())),
                escapeHtml(formatCurrency(invoice.getBalanceDue())),
                notesMarkup,
                escapeHtml(notificationProperties.getBrandName())
        );

        return new NotificationAttachment(
                invoice.getInvoiceNumber() + "-receipt.html",
                "text/html",
                html.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String buildMemberContactMarkup(Member member) {
        StringBuilder markup = new StringBuilder();
        appendContact(markup, member.getPhoneNumber(), "Phone");
        appendContact(markup, member.getPreferredWhatsappNumber(), "WhatsApp");
        appendContact(markup, member.getEmail(), "Email");
        appendContact(markup, member.getTelegramChatId(), "Telegram");
        return markup.toString();
    }

    private void appendContact(StringBuilder markup, String value, String label) {
        if (!hasText(value)) {
            return;
        }
        markup.append("<div class=\"muted\" style=\"margin-top: 6px;\">")
                .append(escapeHtml(label))
                .append(": ")
                .append(escapeHtml(value))
                .append("</div>");
    }

    private String formatCurrency(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(INDIAN_ENGLISH);
        formatter.setCurrency(Currency.getInstance("INR"));
        return formatter.format(value == null ? BigDecimal.ZERO : value);
    }

    private String formatEnumLabel(String value) {
        return value.replace('_', ' ');
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