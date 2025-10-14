package com.adrin.fc.service;

import com.adrin.fc.entity.Otp;
import com.adrin.fc.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final JavaMailSender mailSender;
    private final OtpRepository otpRepository;

    @Transactional
    public void sendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(1);

        otpRepository.deleteByEmail(email);

        Otp otpEntity = Otp.builder()
                .email(email)
                .otp(otp)
                .expiryTime(expiryTime)
                .build();

        otpRepository.save(otpEntity);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Verification Code");
        message.setText("Your OTP is: " + otp + "\nIt will expire in 60 seconds.");
        mailSender.send(message);
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        return otpRepository.findByEmail(email)
                .filter(o -> o.getExpiryTime().isAfter(LocalDateTime.now()))
                .filter(o -> o.getOtp().equals(otp))
                .map(o -> {
                    otpRepository.delete(o);
                    return true;
                })
                .orElse(false);
    }
}
