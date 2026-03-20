package com.example.moneymanager.service;

import com.example.moneymanager.dto.response.ExpenseResponseDTO;
import com.example.moneymanager.entity.User;
import com.example.moneymanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;


    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendDailyIncomeEeExpenseReminder() {
        log.info("Job started: Sending Daily Income Ee Expense Reminder");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
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
                                    background: linear-gradient(135deg, #4CAF50, #2E7D32);
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
                                    color: #2E7D32;
                                    font-weight: bold;
                                }
                                .button {
                                    display: inline-block;
                                    margin-top: 20px;
                                    padding: 12px 20px;
                                    background: #4CAF50;
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
                                    📊 Nhắc nhở quản lý tài chính hằng ngày
                                </div>
                                <div class="content">
                                    <p>Xin chào <span class="highlight">%s</span>,</p>
                        
                                    <p>Đã đến lúc cập nhật <b>thu nhập & chi tiêu</b> hôm nay của bạn rồi!</p>
                        
                                    <p>Việc ghi chép mỗi ngày sẽ giúp bạn:</p>
                                    <ul>
                                        <li>💰 Kiểm soát chi tiêu tốt hơn</li>
                                        <li>📈 Theo dõi tài chính rõ ràng</li>
                                        <li>🎯 Đạt mục tiêu tiết kiệm nhanh hơn</li>
                                    </ul>
                        
                                    <p>Chỉ mất <span class="highlight">1 phút</span> để hoàn thành thôi 😉</p>
                        
                                    <a href="%s/dashboard" class="button">Cập nhật ngay</a>
                        
                                    <p style="margin-top:20px;">Chúc bạn một ngày tuyệt vời! 🌟</p>
                                </div>
                                <div class="footer">
                                    Đây là email tự động, vui lòng không trả lời.
                                </div>
                            </div>
                        </body>
                        </html>
                        """.formatted(user.getFullName(), frontendUrl);

                emailService.sendEmail(user.getEmail(), "[Money Manager] Nhắc nhở cập nhật thu chi hôm nay \uD83D\uDCB0", body);
            } catch (Exception e) {
                log.error("Failed to send email to {}", user.getEmail(), e);
            }

            log.info("Job end: Sending Daily Income Ee Expense Reminder");

        }

    }


    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendDailyExpenseSummary() {
        log.info("Job Started: Sending Daily Expense Summary");
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate today = LocalDate.now();
        List<User> users = this.userRepository.findByIsActiveTrue();
        for (User user : users) {
            List<ExpenseResponseDTO> todaysExpense =
                    expenseService.getExpensesForUserOnDate(user.getId(), today);

            if (!todaysExpense.isEmpty()) {

                StringBuilder table = new StringBuilder();

                // 👉 Table header
                table.append("""
                            <table style="border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; margin-top: 15px;">
                                <thead>
                                    <tr style="background-color: #4CAF50; color: white;">
                                        <th style="padding: 10px; border: 1px solid #ddd;">#</th>
                                        <th style="padding: 10px; border: 1px solid #ddd;">Tên khoản chi</th>
                                        <th style="padding: 10px; border: 1px solid #ddd;">Số tiền</th>
                                        <th style="padding: 10px; border: 1px solid #ddd;">Danh mục</th>
                                    </tr>
                                </thead>
                                <tbody>
                        """);

                int i = 1;

                for (ExpenseResponseDTO expense : todaysExpense) {
                    table.append("<tr style='text-align:center;'>");

                    table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                            .append(i++)
                            .append("</td>");

                    table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                            .append(expense.getName())
                            .append("</td>");

                    table.append("<td style='border:1px solid #ddd;padding:8px;color:#e53935;font-weight:bold;'>")
                            .append(formatter.format(expense.getAmount()))
                            .append(" VND")
                            .append("</td>");

                    table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                            .append(expense.getCategory() != null
                                    ? expense.getCategory().getName()
                                    : "N/A")
                            .append("</td>");

                    table.append("</tr>");
                }

                table.append("""
                                </tbody>
                            </table>
                        """);


                BigDecimal total = todaysExpense.stream()
                        .map(ExpenseResponseDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                String totalHtml = "<p style='margin-top:15px;font-weight:bold;font-size:16px;'>Tổng chi: "
                        + formatter.format(total) + " VND 💰</p>";


                String body = """
                            <html>
                            <body style="font-family: Arial, sans-serif; background-color:#f4f6f8; padding:20px;">
                                <div style="max-width:600px;margin:auto;background:white;padding:20px;border-radius:10px;">
                        
                                    <h3 style="color:#2E7D32;">📊 Báo cáo chi tiêu ngày %s</h3>
                        
                                    <p>Xin chào <b>%s</b>, đây là các khoản chi của bạn hôm nay:</p>
                        
                                    %s
                        
                                    %s
                        
                                    <p style="margin-top:20px;">Chúc bạn quản lý tài chính hiệu quả! 💰</p>
                                </div>
                            </body>
                            </html>
                        """.formatted(user.getFullName(), today.format(dateFormatter), table.toString(), totalHtml);

                emailService.sendEmail(
                        user.getEmail(),
                        "[Money Manager] Báo cáo chi tiêu ngày " + today.format(dateFormatter) + " 📊",
                        body
                );
            }
        }
        log.info("Job End: Sending Daily Expense Summary");
    }
}
