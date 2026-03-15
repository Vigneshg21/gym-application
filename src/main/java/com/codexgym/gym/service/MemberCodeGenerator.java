package com.codexgym.gym.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MemberCodeGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT);

    public String nextMemberCode() {
        return "MEM-" + LocalDate.now().format(DATE_FORMATTER) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }
}