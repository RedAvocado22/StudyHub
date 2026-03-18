package com.studyhub.config;

import com.studyhub.enums.*;
import com.studyhub.model.*;
import com.studyhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Setting> categories = seedSettings();
        List<User> users = seedUsers();
        List<Course> courses = seedCourses(categories, users);
        seedEnrollments(courses, users);
    }

    private List<Setting> seedSettings() {
        Setting courseCategory = settingRepository.save(Setting.builder()
                .name("Course Category")
                .status(SettingStatus.ACTIVE)
                .priority(1)
                .build());

        return settingRepository.saveAll(List.of(
                Setting.builder().name("Programming").type(courseCategory).priority(1).status(SettingStatus.ACTIVE).build(),
                Setting.builder().name("Design").type(courseCategory).priority(2).status(SettingStatus.ACTIVE).build(),
                Setting.builder().name("Marketing").type(courseCategory).priority(3).status(SettingStatus.ACTIVE).build(),
                Setting.builder().name("Data Science").type(courseCategory).priority(4).status(SettingStatus.ACTIVE).build(),
                Setting.builder().name("Business").type(courseCategory).priority(5).status(SettingStatus.ACTIVE).build()
        ));
    }

    private List<User> seedUsers() {
        String pw = passwordEncoder.encode("password123");
        return userRepository.saveAll(List.of(
                User.builder().fullName("Admin User").username("admin").email("admin@studyhub.com")
                        .password(pw).role(UserRole.ADMIN).status(UserStatus.ACTIVE).mobile("0900000001")
                        .profileImage(avatar("Admin User", "dc3545")).build(),
                User.builder().fullName("Manager One").username("manager1").email("manager1@studyhub.com")
                        .password(pw).role(UserRole.MANAGER).status(UserStatus.ACTIVE).mobile("0900000002")
                        .profileImage(avatar("Manager One", "fd7e14")).build(),
                User.builder().fullName("Manager Two").username("manager2").email("manager2@studyhub.com")
                        .password(pw).role(UserRole.MANAGER).status(UserStatus.ACTIVE).mobile("0900000003")
                        .profileImage(avatar("Manager Two", "fd7e14")).build(),
                User.builder().fullName("Marketing User").username("marketing1").email("marketing1@studyhub.com")
                        .password(pw).role(UserRole.MARKETING).status(UserStatus.ACTIVE).mobile("0900000004")
                        .profileImage(avatar("Marketing User", "6f42c1")).build(),
                User.builder().fullName("Alice Nguyen").username("alice").email("alice@studyhub.com")
                        .password(pw).role(UserRole.MEMBER).status(UserStatus.ACTIVE).mobile("0900000005")
                        .profileImage(avatar("Alice Nguyen", "0d6efd")).build(),
                User.builder().fullName("Bob Tran").username("bob").email("bob@studyhub.com")
                        .password(pw).role(UserRole.MEMBER).status(UserStatus.ACTIVE).mobile("0900000006")
                        .profileImage(avatar("Bob Tran", "0d6efd")).build(),
                User.builder().fullName("Carol Le").username("carol").email("carol@studyhub.com")
                        .password(pw).role(UserRole.MEMBER).status(UserStatus.ACTIVE).mobile("0900000007")
                        .profileImage(avatar("Carol Le", "0d6efd")).build(),
                User.builder().fullName("Dave Pham").username("dave").email("dave@studyhub.com")
                        .password(pw).role(UserRole.MEMBER).status(UserStatus.ACTIVE).mobile("0900000008")
                        .profileImage(avatar("Dave Pham", "0d6efd")).build(),
                User.builder().fullName("Eve Hoang").username("eve").email("eve@studyhub.com")
                        .password(pw).role(UserRole.MEMBER).status(UserStatus.ACTIVE).mobile("0900000009")
                        .profileImage(avatar("Eve Hoang", "0d6efd")).build(),
                User.builder().fullName("Unverified User").username("unverified").email("unverified@studyhub.com")
                        .password(pw).role(UserRole.MEMBER).status(UserStatus.UNVERIFIED).mobile("0900000010")
                        .profileImage(avatar("Unverified User", "6c757d"))
                        .verificationToken("dummy-verify-token-abc123").build()
        ));
    }

    private String avatar(String name, String bgHex) {
        return "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=" + bgHex + "&color=fff&size=200&bold=true";
    }

    private List<Course> seedCourses(List<Setting> categories, List<User> users) {
        User manager1 = users.get(1);
        User manager2 = users.get(2);
        Setting programming = categories.get(0);
        Setting design      = categories.get(1);
        Setting marketing   = categories.get(2);
        Setting dataScience = categories.get(3);

        Course java = saveCourse("Java Programming Fundamentals",
                "A comprehensive introduction to Java programming for absolute beginners.",
                new BigDecimal("499000.00"), CourseLevel.BEGINNER, 40, programming, manager1, true,
                "https://picsum.photos/seed/java/800/450");
        addJavaChapters(java);

        Course spring = saveCourse("Spring Boot Masterclass",
                "Build production-ready web applications with Spring Boot and Spring Security.",
                new BigDecimal("799000.00"), CourseLevel.INTERMEDIATE, 60, programming, manager1, true,
                "https://picsum.photos/seed/spring/800/450");
        addSpringChapters(spring);

        Course uiux = saveCourse("UI/UX Design Essentials",
                "Master the principles of user interface and experience design using modern tools.",
                new BigDecimal("350000.00"), CourseLevel.BEGINNER, 25, design, manager2, true,
                "https://picsum.photos/seed/uiux/800/450");
        addUiuxChapters(uiux);

        Course ml = saveCourse("Machine Learning with Python",
                "Explore machine learning algorithms and data science pipelines with real-world projects.",
                new BigDecimal("999000.00"), CourseLevel.ADVANCED, 80, dataScience, manager2, false,
                "https://picsum.photos/seed/machinelearning/800/450");
        addMlChapters(ml);

        Course dm = saveCourse("Digital Marketing Strategy",
                "Master SEO, SEM, social media marketing and email campaign best practices.",
                new BigDecimal("450000.00"), CourseLevel.INTERMEDIATE, 30, marketing, manager1, true,
                "https://picsum.photos/seed/digitalmarketing/800/450");
        addMarketingChapters(dm);

        return List.of(java, spring, uiux, ml, dm);
    }

    private Course saveCourse(String title, String desc, BigDecimal price, CourseLevel level,
                               int durationHours, Setting category, User manager, boolean published,
                               String thumbnailUrl) {
        return courseRepository.save(Course.builder()
                .title(title).description(desc).price(price).level(level)
                .durationHours(durationHours).category(category).manager(manager)
                .published(published).thumbnailUrl(thumbnailUrl).build());
    }

    // Real YouTube embed URLs (public educational videos)
    private static final String YT = "https://www.youtube.com/embed/";
    private static final String JAVA_INTRO    = YT + "eIrMbAQSU34"; // Mosh – Java Tutorial for Beginners
    private static final String JAVA_OOP      = YT + "xk4_1vDrzzo"; // Mosh – Java OOP
    private static final String SPRING_INTRO  = YT + "9SGDpanrc8U"; // Amigoscode – Spring Boot
    private static final String SPRING_SEC    = YT + "her_7pa0vrg"; // Amigoscode – Spring Security
    private static final String PYTHON_INTRO  = YT + "_uQrJ0TkZlc"; // Mosh – Python for Beginners
    private static final String PYTHON_VIZ    = YT + "a9UrKTVEeZA"; // Corey Schafer – Matplotlib
    private static final String ML_TREES      = YT + "7VeUPuFGJHk"; // StatQuest – Decision Trees
    private static final String DESIGN_INTRO  = YT + "c9Wg6Cb_YlU"; // DesignCourse – UI/UX
    private static final String FIGMA_INTRO   = YT + "FTlczfR23mA"; // DesignCourse – Figma
    private static final String MARKETING_101 = YT + "bixR-KIJKYM"; // HubSpot – Digital Marketing
    private static final String SOCIAL_ADS    = YT + "0BKIX8hPFaE"; // Facebook/Instagram Ads

    private void addJavaChapters(Course course) {
        Chapter ch1 = chapter("Getting Started", "Java and development environment setup", 1, course);
        lesson("What is Java?", LessonContentType.VIDEO, JAVA_INTRO, null, 10, 1, ch1, true);
        lesson("Installing JDK and IDE", LessonContentType.VIDEO, JAVA_INTRO, null, 8, 2, ch1, false);
        lesson("Writing Your First Program", LessonContentType.TEXT, null,
                "<h3>Hello World</h3><p>Create a class with a <code>main</code> method and print to console.</p>",
                5, 3, ch1, false);

        Chapter ch2 = chapter("Java Fundamentals", "Variables, control flow and methods", 2, course);
        lesson("Variables and Data Types", LessonContentType.VIDEO, JAVA_INTRO, null, 12, 1, ch2, false);
        lesson("Control Flow Statements", LessonContentType.PDF, "/uploads/files/StudyHub_Specs.pdf", null, 10, 2, ch2, false);
        lesson("Methods and Parameters", LessonContentType.VIDEO, JAVA_INTRO, null, 15, 3, ch2, false);

        Chapter ch3 = chapter("Object-Oriented Programming", "Classes, objects and inheritance", 3, course);
        lesson("Classes and Objects", LessonContentType.VIDEO, JAVA_OOP, null, 18, 1, ch3, true);
        lesson("Inheritance and Interfaces", LessonContentType.TEXT, null,
                "<h3>OOP Concepts</h3><p>Inheritance allows classes to reuse code through parent-child relationships.</p>",
                12, 2, ch3, false);
    }

    private void addSpringChapters(Course course) {
        Chapter ch1 = chapter("Spring Boot Basics", "Project setup and core concepts", 1, course);
        lesson("What is Spring Boot?", LessonContentType.VIDEO, SPRING_INTRO, null, 10, 1, ch1, true);
        lesson("Spring Initializr Setup", LessonContentType.VIDEO, SPRING_INTRO, null, 8, 2, ch1, false);

        Chapter ch2 = chapter("Building REST APIs", "Controllers, DTOs and response handling", 2, course);
        lesson("Creating Controllers", LessonContentType.VIDEO, SPRING_INTRO, null, 15, 1, ch2, false);
        lesson("Request Mapping and DTOs", LessonContentType.TEXT, null,
                "<h3>DTOs</h3><p>Data Transfer Objects decouple your domain model from the API contract.</p>",
                10, 2, ch2, false);
        lesson("Global Exception Handling", LessonContentType.PDF, "/uploads/files/StudyHub_Screens.pdf", null, 12, 3, ch2, false);

        Chapter ch3 = chapter("Spring Security", "Authentication and authorization", 3, course);
        lesson("Authentication Basics", LessonContentType.VIDEO, SPRING_SEC, null, 20, 1, ch3, true);
        lesson("Role-Based Authorization", LessonContentType.TEXT, null,
                "<h3>Authorization</h3><p>Use <code>SecurityConfig</code> to restrict access by role.</p>",
                15, 2, ch3, false);
    }

    private void addUiuxChapters(Course course) {
        Chapter ch1 = chapter("Design Fundamentals", "Color theory, typography and layout", 1, course);
        lesson("Color Theory", LessonContentType.VIDEO, DESIGN_INTRO, null, 12, 1, ch1, true);
        lesson("Typography Basics", LessonContentType.VIDEO, DESIGN_INTRO, null, 10, 2, ch1, false);
        lesson("Layout and Spacing", LessonContentType.PDF, "/uploads/files/StudyHub_Specs.pdf", null, 8, 3, ch1, false);

        Chapter ch2 = chapter("Wireframing and Prototyping", "Tools and techniques for wireframes", 2, course);
        lesson("Creating Wireframes", LessonContentType.VIDEO, FIGMA_INTRO, null, 15, 1, ch2, false);
        lesson("Prototyping with Figma", LessonContentType.TEXT, null,
                "<h3>Figma</h3><p>Figma is a cloud-based design tool ideal for collaborative UI prototyping.</p>",
                10, 2, ch2, false);
    }

    private void addMlChapters(Course course) {
        Chapter ch1 = chapter("Python for Data Science", "NumPy, Pandas and data visualization", 1, course);
        lesson("NumPy and Pandas Crash Course", LessonContentType.VIDEO, PYTHON_INTRO, null, 20, 1, ch1, true);
        lesson("Data Visualization", LessonContentType.VIDEO, PYTHON_VIZ, null, 18, 2, ch1, false);

        Chapter ch2 = chapter("Machine Learning Basics", "Supervised and unsupervised learning", 2, course);
        lesson("Supervised vs Unsupervised", LessonContentType.TEXT, null,
                "<h3>ML Types</h3><p>Supervised learning uses labeled data while unsupervised finds patterns.</p>",
                15, 1, ch2, false);
        lesson("Linear Regression", LessonContentType.PDF, "/uploads/files/StudyHub_Screens.pdf", null, 20, 2, ch2, false);
        lesson("Decision Trees and Random Forests", LessonContentType.VIDEO, ML_TREES, null, 25, 3, ch2, false);
    }

    private void addMarketingChapters(Course course) {
        Chapter ch1 = chapter("Digital Marketing 101", "Foundations and core channels", 1, course);
        lesson("Overview of Digital Marketing", LessonContentType.VIDEO, MARKETING_101, null, 10, 1, ch1, true);
        lesson("SEO Fundamentals", LessonContentType.TEXT, null,
                "<h3>SEO</h3><p>Improve organic search rankings through keyword research and on-page optimization.</p>",
                12, 2, ch1, false);

        Chapter ch2 = chapter("Social Media Marketing", "Paid and organic social strategies", 2, course);
        lesson("Facebook and Instagram Ads", LessonContentType.VIDEO, SOCIAL_ADS, null, 15, 1, ch2, false);
        lesson("Content Calendar Planning", LessonContentType.PDF, "/uploads/files/StudyHub_Specs.pdf", null, 10, 2, ch2, false);
    }

    private Chapter chapter(String title, String description, int order, Course course) {
        return chapterRepository.save(Chapter.builder()
                .title(title).description(description).order(order).course(course).build());
    }

    private void lesson(String title, LessonContentType type, String url, String text,
                        int duration, int order, Chapter chapter, boolean preview) {
        lessonRepository.save(Lesson.builder()
                .title(title).contentType(type).contentUrl(url).contentText(text)
                .durationMinutes(duration).order(order).chapter(chapter).preview(preview).build());
    }

    private void seedEnrollments(List<Course> courses, List<User> users) {
        Course java   = courses.get(0);
        Course spring = courses.get(1);
        Course uiux   = courses.get(2);
        Course ml     = courses.get(3);
        Course dm     = courses.get(4);

        User alice = users.get(4);
        User bob   = users.get(5);
        User carol = users.get(6);
        User dave  = users.get(7);
        User eve   = users.get(8);

        enrollmentRepository.saveAll(List.of(
                enrollment(java,   alice, EnrollmentStatus.APPROVED, PaymentMethod.BANK_TRANSFER,     "Wants to learn programming",           new BigDecimal("65.00")),
                enrollment(java,   bob,   EnrollmentStatus.APPROVED, PaymentMethod.INTERNET_BANKING,  "Career change into development",       new BigDecimal("30.00")),
                enrollment(spring, alice, EnrollmentStatus.PENDING,  PaymentMethod.BANK_TRANSFER,     "Building web applications",            BigDecimal.ZERO),
                enrollment(uiux,   dave,  EnrollmentStatus.APPROVED, PaymentMethod.BANK_TRANSFER,     "Improve design skills",                new BigDecimal("100.00")),
                enrollment(uiux,   eve,   EnrollmentStatus.APPROVED, PaymentMethod.INTERNET_BANKING,  "Freelance design work",                new BigDecimal("45.00")),
                enrollment(ml,     bob,   EnrollmentStatus.PENDING,  PaymentMethod.BANK_TRANSFER,     "Data science career path",             BigDecimal.ZERO),
                enrollment(dm,     carol, EnrollmentStatus.APPROVED, PaymentMethod.INTERNET_BANKING,  "Grow business online",                 new BigDecimal("80.00")),
                enrollment(java,   carol, EnrollmentStatus.APPROVED, PaymentMethod.BANK_TRANSFER,     "Programming fundamentals",             new BigDecimal("15.00")),
                enrollment(dm,     dave,  EnrollmentStatus.PENDING,  PaymentMethod.BANK_TRANSFER,     "Marketing for startup",                BigDecimal.ZERO),
                Enrollment.builder()
                        .course(spring).user(carol).fullName(carol.getFullName()).email(carol.getEmail())
                        .mobile(carol.getMobile()).enrollReason("Work requirement")
                        .paymentMethod(PaymentMethod.INTERNET_BANKING).fee(spring.getPrice())
                        .status(EnrollmentStatus.REJECTED)
                        .rejectedNotes("Payment confirmation not received within 48 hours.")
                        .progress(BigDecimal.ZERO).build()
        ));
    }

    private Enrollment enrollment(Course course, User user, EnrollmentStatus status,
                                   PaymentMethod payment, String reason, BigDecimal progress) {
        return Enrollment.builder()
                .course(course).user(user).fullName(user.getFullName()).email(user.getEmail())
                .mobile(user.getMobile()).enrollReason(reason).paymentMethod(payment)
                .fee(course.getPrice()).status(status).progress(progress).build();
    }
}
