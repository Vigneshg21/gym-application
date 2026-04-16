package com.codexgym.docs;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HtmlToPdf {

    private HtmlToPdf() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: HtmlToPdf <source-html> <output-pdf>");
        }

        Path source = Path.of(args[0]).toAbsolutePath().normalize();
        Path target = Path.of(args[1]).toAbsolutePath().normalize();

        if (!Files.exists(source)) {
            throw new IllegalArgumentException("Source HTML not found: " + source);
        }

        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }

        String html = Files.readString(source, StandardCharsets.UTF_8);
        render(html, source, target);
    }

    private static void render(String html, Path source, Path target) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(target)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, source.getParent().toUri().toString());
            builder.toStream(outputStream);
            builder.run();
        } catch (Exception exception) {
            throw new IOException("Failed to generate PDF " + target, exception);
        }
    }
}
