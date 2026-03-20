package com.example.moneymanager.service;

import com.example.moneymanager.dto.request.UserRequestDTO;
import com.example.moneymanager.dto.response.UserResponseDTO;
import com.example.moneymanager.entity.User;
import com.example.moneymanager.helper.exception.ResourceNotFoundException;
import com.example.moneymanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.activation.url}")
    private String activationURL;

    public UserResponseDTO registerProfile(UserRequestDTO userDTO) {
        User newUser = toEntity(userDTO);
        newUser.setActivationToken(UUID.randomUUID().toString());
        newUser = userRepository.save(newUser);
        //send activation email
        String activationLink = activationURL + "/activate?token=" + newUser.getActivationToken();
        String subject = "🎉 Kích hoạt tài khoản Money Manager của bạn";
        String body = """
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background-color: #f4f6f8;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: auto;
                            background: #ffffff;
                            border-radius: 10px;
                            overflow: hidden;
                            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #2196F3, #1565C0);
                            color: white;
                            padding: 20px;
                            text-align: center;
                            font-size: 22px;
                            font-weight: bold;
                        }
                        .content {
                            padding: 25px;
                            color: #333;
                            line-height: 1.6;
                        }
                        .highlight {
                            color: #1565C0;
                            font-weight: bold;
                        }
                        .button {
                            display: inline-block;
                            margin-top: 20px;
                            padding: 12px 20px;
                            background: #2196F3;
                            color: white;
                            text-decoration: none;
                            border-radius: 6px;
                            font-weight: bold;
                        }
                        .footer {
                            text-align: center;
                            font-size: 12px;
                            color: #888;
                            padding: 15px;
                            background: #f1f1f1;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            🎉 Chào mừng bạn đến với Money Manager
                        </div>
                        <div class="content">
                            <p>Xin chào <span class="highlight">%s</span>,</p>
                
                            <p>Cảm ơn bạn đã đăng ký tài khoản!</p>
                
                            <p>Vui lòng xác nhận email của bạn để bắt đầu sử dụng ứng dụng quản lý tài chính.</p>
                
                            <a href="%s" class="button">Kích hoạt tài khoản</a>
                
                            <p style="margin-top:20px;">
                                Nếu nút không hoạt động, bạn có thể copy link sau vào trình duyệt:
                            </p>
                            <p style="word-break: break-all;">%s</p>
                
                            <p style="margin-top:20px;">Chúc bạn quản lý tài chính thật hiệu quả! 💰</p>
                        </div>
                        <div class="footer">
                            Đây là email tự động, vui lòng không trả lời.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(newUser.getFullName(), activationLink, activationLink);
        emailService.sendEmail(newUser.getEmail(), subject, body);
        return toDTO(newUser);
    }

    public User toEntity(UserRequestDTO userRequestDTO) {
        return User.builder()
                .fullName(userRequestDTO.getFullName())
                .email(userRequestDTO.getEmail())
                .password(passwordEncoder.encode(userRequestDTO.getPassword()))
                .profileImageUrl(userRequestDTO.getProfileImageUrl())
                .createdAt(userRequestDTO.getCreatedAt())
                .updatedAt(userRequestDTO.getUpdatedAt())
                .build();
    }

    public UserResponseDTO toDTO(User user) {

        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken) {
        return userRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    userRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email) {
        return userRepository.findByEmail(email)
                .map(User::getIsActive)
                .orElse(false);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
    }

    public UserResponseDTO getPublicUser(String email) {
        User currentUser = null;
        if (email == null) {
            currentUser = getCurrentUser();
        } else {
            currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        return toDTO(currentUser);
    }

    public User findUserByEmail(String email) {
        User user = this.userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is not activated");
        }
        return user;
    }
}
