package com.studyhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.config.VnPayConfig;
import com.studyhub.dto.EnrollRequestDTO;
import com.studyhub.dto.PaymentResultDTO;
import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.enums.PaymentMethod;
import com.studyhub.enums.UserRole;
import com.studyhub.enums.UserStatus;
import com.studyhub.model.Course;
import com.studyhub.model.Enrollment;
import com.studyhub.model.User;
import com.studyhub.repository.CourseRepository;
import com.studyhub.repository.EnrollmentRepository;
import com.studyhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final VnPayConfig vnPayConfig;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    private static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("HMAC error: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResultDTO enroll(EnrollRequestDTO dto, User registrar) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + dto.getCourseId()));

        User enrolledUser = resolveOrCreateEnrolledUser(dto.getFullName(), dto.getEmail());

        if (enrolledUser != null && enrollmentRepository.existsByUserAndCourse_IdAndStatus(
                enrolledUser, course.getId(), EnrollmentStatus.APPROVED)) {
            throw new IllegalArgumentException("This person is already enrolled and approved for this course.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setUser(enrolledUser);
        enrollment.setFullName(dto.getFullName());
        enrollment.setEmail(dto.getEmail());
        enrollment.setMobile(dto.getMobile());
        enrollment.setEnrollReason(dto.getEnrollNote());
        enrollment.setPaymentMethod(dto.getPaymentMethod());
        enrollment.setFee(course.getPrice());

        boolean isFree = course.getPrice().compareTo(BigDecimal.ZERO) == 0;
        enrollment.setStatus(isFree ? EnrollmentStatus.APPROVED : EnrollmentStatus.PENDING);

        Enrollment saved = enrollmentRepository.save(enrollment);

        emailService.sendEnrollmentNotificationToEnrollee(dto.getEmail(), dto.getFullName(), course.getTitle());
        if (!registrar.getEmail().equalsIgnoreCase(dto.getEmail())) {
            emailService.sendEnrollmentConfirmationToRegistrar(registrar, dto.getFullName(), course.getTitle());
        }

        if (isFree) {
            return new PaymentResultDTO(true, "/enrollments/me", saved.getId());
        }

        if (dto.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            String payUrl = createPayOSUrl(saved);
            return new PaymentResultDTO(true, payUrl, saved.getId());
        }

        return new PaymentResultDTO(true, null, saved.getId());
    }

    private User resolveOrCreateEnrolledUser(String fullName, String email) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            String rawPassword = generatePassword();
            User newUser = User.builder()
                    .fullName(fullName)
                    .email(email)
                    .username(generateUsername(email))
                    .password(passwordEncoder.encode(rawPassword))
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(newUser);
            emailService.sendNewAccountEmail(newUser, rawPassword);
            return newUser;
        });
    }

    private String generateUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (!userRepository.existsByUsername(base)) return base;
        String candidate;
        do {
            candidate = base + ThreadLocalRandom.current().nextInt(100, 9999);
        } while (userRepository.existsByUsername(candidate));
        return candidate;
    }

    private String generatePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    public String createVnPayUrl(Long enrollmentId, String ipAddress) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));

        try {
            Map<String, String> params = new TreeMap<>();
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "pay");
            params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            params.put("vnp_Amount", String.valueOf(enrollment.getFee().longValue() * 100));
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_TxnRef", String.valueOf(enrollment.getId()));
            params.put("vnp_OrderInfo", "Enrollment #" + enrollment.getId());
            params.put("vnp_OrderType", "other");
            params.put("vnp_Locale", "vn");
            params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            params.put("vnp_IpAddr", ipAddress);
            params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII);
                hashData.append(entry.getKey()).append("=").append(encodedValue).append("&");
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII))
                        .append("=").append(encodedValue).append("&");
            }

            String secureHash = hmacSHA512(
                    vnPayConfig.getHashSecret(),
                    hashData.substring(0, hashData.length() - 1)
            );

            return vnPayConfig.getPaymentUrl() + "?"
                    + query.substring(0, query.length() - 1)
                    + "&vnp_SecureHash=" + secureHash;

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create VnPay URL: " + e.getMessage());
        }
    }

    @Transactional
    public void handlePayOSWebhook(Map<String, Object> payload) {
        String receivedSignature = String.valueOf(payload.get("signature"));

        // Verify signature
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        String signData = "amount=" + data.get("amount")
                + "&cancelUrl=" + data.get("cancelUrl")
                + "&description=" + data.get("description")
                + "&orderCode=" + data.get("orderCode")
                + "&returnUrl=" + data.get("returnUrl");

        String expectedSignature = hmacSHA256(checksumKey, signData);
        if (!expectedSignature.equals(receivedSignature)) {
            throw new IllegalArgumentException("Invalid webhook signature.");
        }

        String status = String.valueOf(data.get("status"));
        if (!"PAID".equalsIgnoreCase(status) && !"SUCCESS".equalsIgnoreCase(status)) {
            return;
        }

        Long orderCode = Long.parseLong(String.valueOf(data.get("orderCode")));
        markPaid(orderCode);
    }

    private void markPaid(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));
        enrollment.setStatus(EnrollmentStatus.APPROVED);
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public boolean handleVnPayReturn(Map<String, String> params) {
        if (!verifyVnPaySignature(params)) {
            return false;
        }
        if (!"00".equals(params.get("vnp_ResponseCode"))) {
            return false;
        }
        markPaid(Long.parseLong(params.get("vnp_TxnRef")));
        return true;
    }

    private String createPayOSUrl(Enrollment enrollment) {
        try {
            long orderCode = System.currentTimeMillis() % 1000000000L;
            int amount = enrollment.getFee().intValue();
            String description = "DK" + enrollment.getId();
            String returnUrl = baseUrl + "/payment/payos/return?enrollmentId=" + enrollment.getId();
            String cancelUrl = baseUrl + "/payment/payos/cancel?enrollmentId=" + enrollment.getId();

            String signature = hmacSHA256(
                    checksumKey,
                    "amount=" + amount
                            + "&cancelUrl=" + cancelUrl
                            + "&description=" + description
                            + "&orderCode=" + orderCode
                            + "&returnUrl=" + returnUrl
            );

            Map<String, Object> body = new TreeMap<>();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("returnUrl", returnUrl);
            body.put("cancelUrl", cancelUrl);
            body.put("signature", signature);
            body.put("items", List.of(Map.of(
                    "name", enrollment.getCourse().getTitle(),
                    "quantity", 1,
                    "price", amount
            )));

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-merchant.payos.vn/v2/payment-requests"))
                    .header("Content-Type", "application/json")
                    .header("x-client-id", clientId)
                    .header("x-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(response.body());
            if (!"00".equals(json.path("code").asText())) {
                throw new IllegalArgumentException("PayOS error: " + json.path("desc").asText());
            }

            return json.path("data").path("checkoutUrl").asText();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create PayOS URL: " + e.getMessage());
        }
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("HMAC error: " + e.getMessage());
        }
    }

    private boolean verifyVnPaySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        Map<String, String> filtered = new TreeMap<>(params);
        filtered.remove("vnp_SecureHash");
        filtered.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : filtered.entrySet()) {
            hashData.append(entry.getKey()).append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII))
                    .append("&");
        }

        String expectedHash = hmacSHA512(
                vnPayConfig.getHashSecret(),
                hashData.substring(0, hashData.length() - 1)
        );

        return expectedHash.equalsIgnoreCase(receivedHash);
    }
}