package com.codexgym.gym.service.whatsapp;

public interface WhatsAppGateway {

    void sendMessage(String recipient, String messageBody);
}

