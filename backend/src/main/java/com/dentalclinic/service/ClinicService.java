package com.dentalclinic.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dentalclinic.common.BusinessException;
import com.dentalclinic.domain.*;
import com.dentalclinic.entity.*;
import com.dentalclinic.mapper.*;
import com.dentalclinic.security.SecurityUtils;
import com.dentalclinic.security.UserPrincipal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ClinicService {
    public final UserAccountMapper users;
    public final PatientProfileMapper patients;
    public final DoctorProfileMapper doctors;
    public final DoctorQualificationMapper qualifications;
    public final DoctorScheduleMapper schedules;
    public final AnnouncementMapper announcements;
    public final MedicineMapper medicines;
    public final CartItemMapper cartItems;
    public final MedicineOrderMapper orders;
    public final OrderItemMapper orderItems;
    public final AppointmentMapper appointments;
    public final MedicalRecordMapper records;
    public final PrescriptionMapper prescriptions;
    public final PrescriptionItemMapper prescriptionItems;
    public final MessageMapper messages;
    public final DoctorReviewMapper reviews;
    public final SymptomRuleMapper symptomRules;
    public final MedicationReminderMapper reminders;
    public final OperationLogMapper logs;
    private final PasswordEncoder passwordEncoder;

    public ClinicService(UserAccountMapper users, PatientProfileMapper patients, DoctorProfileMapper doctors,
                         DoctorQualificationMapper qualifications, DoctorScheduleMapper schedules,
                         AnnouncementMapper announcements, MedicineMapper medicines, CartItemMapper cartItems,
                         MedicineOrderMapper orders, OrderItemMapper orderItems, AppointmentMapper appointments,
                         MedicalRecordMapper records, PrescriptionMapper prescriptions,
                         PrescriptionItemMapper prescriptionItems, MessageMapper messages,
                         DoctorReviewMapper reviews, SymptomRuleMapper symptomRules,
                         MedicationReminderMapper reminders, OperationLogMapper logs,
                         PasswordEncoder passwordEncoder) {
        this.users = users;
        this.patients = patients;
        this.doctors = doctors;
        this.qualifications = qualifications;
        this.schedules = schedules;
        this.announcements = announcements;
        this.medicines = medicines;
        this.cartItems = cartItems;
        this.orders = orders;
        this.orderItems = orderItems;
        this.appointments = appointments;
        this.records = records;
        this.prescriptions = prescriptions;
        this.prescriptionItems = prescriptionItems;
        this.messages = messages;
        this.reviews = reviews;
        this.symptomRules = symptomRules;
        this.reminders = reminders;
        this.logs = logs;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount registerPatient(String username, String password, String name, String phone) {
        if (findUser(username) != null) {
            throw new BusinessException("username already exists");
        }
        UserAccount account = new UserAccount();
        account.username = username;
        account.passwordHash = passwordEncoder.encode(password);
        account.role = Role.PATIENT;
        account.status = AccountStatus.ENABLED;
        account.createdAt = LocalDateTime.now();
        users.insert(account);

        PatientProfile profile = new PatientProfile();
        profile.userId = account.id;
        profile.name = StringUtils.hasText(name) ? name : username;
        profile.phone = phone;
        patients.insert(profile);
        return account;
    }

    public UserAccount findUser(String username) {
        return users.selectOne(new QueryWrapper<UserAccount>().eq("username", username));
    }

    public Map<String, Object> currentMe() {
        UserPrincipal principal = SecurityUtils.current();
        return meOf(principal);
    }

    public Map<String, Object> meOf(UserPrincipal principal) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", principal.id);
        result.put("username", principal.getUsername());
        result.put("role", principal.role);
        if (principal.role == Role.PATIENT) {
            result.put("profile", patientByUser(principal.id));
        }
        if (principal.role == Role.DOCTOR) {
            result.put("profile", doctorByUser(principal.id));
        }
        return result;
    }

    public PatientProfile currentPatient() {
        UserPrincipal principal = SecurityUtils.current();
        if (principal.role != Role.PATIENT) {
            throw new BusinessException("patient role required");
        }
        return patientByUser(principal.id);
    }

    public DoctorProfile currentDoctor(boolean requireApproved) {
        UserPrincipal principal = SecurityUtils.current();
        if (principal.role != Role.DOCTOR) {
            throw new BusinessException("doctor role required");
        }
        DoctorProfile doctor = doctorByUser(principal.id);
        if (requireApproved && doctor.reviewStatus != ReviewStatus.APPROVED) {
            throw new BusinessException("doctor qualification is not approved");
        }
        return doctor;
    }

    public PatientProfile patientByUser(Long userId) {
        PatientProfile profile = patients.selectOne(new QueryWrapper<PatientProfile>().eq("user_id", userId));
        if (profile == null) {
            throw new BusinessException("patient profile not found");
        }
        return profile;
    }

    public DoctorProfile doctorByUser(Long userId) {
        DoctorProfile profile = doctors.selectOne(new QueryWrapper<DoctorProfile>().eq("user_id", userId));
        if (profile == null) {
            throw new BusinessException("doctor profile not found");
        }
        return profile;
    }

    public List<Announcement> publishedAnnouncements() {
        return announcements.selectList(new QueryWrapper<Announcement>()
            .eq("status", AnnouncementStatus.PUBLISHED)
            .orderByDesc("publish_at"));
    }

    public List<Map<String, Object>> approvedDoctors() {
        return doctors.selectList(new QueryWrapper<DoctorProfile>().eq("review_status", ReviewStatus.APPROVED))
            .stream().map(this::doctorCard).toList();
    }

    public Map<String, Object> doctorCard(DoctorProfile doctor) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("doctor", doctor);
        card.put("schedules", schedules.selectList(new QueryWrapper<DoctorSchedule>().eq("doctor_id", doctor.id).ge("work_date", LocalDate.now())));
        card.put("reviews", reviews.selectList(new QueryWrapper<DoctorReview>().eq("doctor_id", doctor.id).last("limit 5")));
        return card;
    }

    public List<Medicine> shelfMedicines() {
        return medicines.selectList(new QueryWrapper<Medicine>().eq("on_shelf", true).gt("stock", 0));
    }

    public CartItem addCart(Long medicineId, Integer quantity) {
        PatientProfile patient = currentPatient();
        Medicine medicine = requireMedicine(medicineId);
        if (Boolean.FALSE.equals(medicine.onShelf) || medicine.stock <= 0) {
            throw new BusinessException("medicine is unavailable");
        }
        CartItem item = cartItems.selectOne(new QueryWrapper<CartItem>()
            .eq("patient_id", patient.id).eq("medicine_id", medicineId));
        if (item == null) {
            item = new CartItem();
            item.patientId = patient.id;
            item.medicineId = medicineId;
            item.quantity = Math.max(1, quantity == null ? 1 : quantity);
            cartItems.insert(item);
        } else {
            item.quantity += Math.max(1, quantity == null ? 1 : quantity);
            cartItems.updateById(item);
        }
        return item;
    }

    public List<Map<String, Object>> cart() {
        PatientProfile patient = currentPatient();
        return cartItems.selectList(new QueryWrapper<CartItem>().eq("patient_id", patient.id)).stream()
            .map(item -> Map.<String, Object>of("item", item, "medicine", medicines.selectById(item.medicineId)))
            .toList();
    }

    @Transactional
    public MedicineOrder createOrder(List<OrderLine> lines, String deliveryMethod) {
        PatientProfile patient = currentPatient();
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("order items required");
        }
        MedicineOrder order = new MedicineOrder();
        order.orderNo = "DCO" + System.currentTimeMillis();
        order.patientId = patient.id;
        order.deliveryMethod = StringUtils.hasText(deliveryMethod) ? deliveryMethod : "到店自取";
        order.status = OrderStatus.PAID;
        order.createdAt = LocalDateTime.now();
        order.totalAmount = BigDecimal.ZERO;
        orders.insert(order);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            Medicine medicine = requireMedicine(line.medicineId());
            int quantity = Math.max(1, line.quantity());
            if (Boolean.FALSE.equals(medicine.onShelf) || medicine.stock < quantity) {
                throw new BusinessException(medicine.name + " stock is insufficient");
            }
            medicine.stock -= quantity;
            medicines.updateById(medicine);
            OrderItem item = new OrderItem();
            item.orderId = order.id;
            item.medicineId = medicine.id;
            item.medicineName = medicine.name;
            item.unitPrice = medicine.price;
            item.quantity = quantity;
            orderItems.insert(item);
            total = total.add(medicine.price.multiply(BigDecimal.valueOf(quantity)));
        }
        order.totalAmount = total;
        orders.updateById(order);
        log("CREATE_ORDER", order.orderNo);
        return order;
    }

    public List<Map<String, Object>> myOrders() {
        PatientProfile patient = currentPatient();
        return orders.selectList(new QueryWrapper<MedicineOrder>().eq("patient_id", patient.id).orderByDesc("created_at")).stream()
            .map(this::orderWithItems)
            .toList();
    }

    public Map<String, Object> orderWithItems(MedicineOrder order) {
        return Map.of("order", order, "items", orderItems.selectList(new QueryWrapper<OrderItem>().eq("order_id", order.id)));
    }

    public void updateOrderStatus(Long orderId, OrderStatus status) {
        MedicineOrder order = orders.selectById(orderId);
        if (order == null) {
            throw new BusinessException("order not found");
        }
        order.status = status;
        orders.updateById(order);
        log("UPDATE_ORDER", order.orderNo + " -> " + status);
    }

    public void changeDelivery(Long orderId, String deliveryMethod) {
        MedicineOrder order = orders.selectById(orderId);
        if (order == null) {
            throw new BusinessException("order not found");
        }
        order.deliveryMethod = deliveryMethod;
        orders.updateById(order);
    }

    @Transactional
    public Appointment createAppointment(Long doctorId, LocalDate visitDate, LocalTime timeSlot, String symptoms, String demand) {
        PatientProfile patient = currentPatient();
        DoctorProfile doctor = doctors.selectById(doctorId);
        if (doctor == null || doctor.reviewStatus != ReviewStatus.APPROVED) {
            throw new BusinessException("doctor is unavailable");
        }
        Long conflicts = appointments.selectCount(new QueryWrapper<Appointment>()
            .eq("doctor_id", doctorId)
            .eq("visit_date", visitDate)
            .eq("time_slot", timeSlot)
            .in("status", List.of(AppointmentStatus.SUBMITTED, AppointmentStatus.CONFIRMED)));
        if (conflicts > 0) {
            throw new BusinessException("time slot is already booked");
        }
        Appointment appointment = new Appointment();
        appointment.patientId = patient.id;
        appointment.doctorId = doctorId;
        appointment.visitDate = visitDate;
        appointment.timeSlot = timeSlot;
        appointment.symptoms = symptoms;
        appointment.demand = demand;
        appointment.status = AppointmentStatus.SUBMITTED;
        appointment.createdAt = LocalDateTime.now();
        appointments.insert(appointment);
        log("CREATE_APPOINTMENT", "doctor=" + doctorId + ", date=" + visitDate + " " + timeSlot);
        return appointment;
    }

    public List<Map<String, Object>> myAppointments() {
        PatientProfile patient = currentPatient();
        return appointments.selectList(new QueryWrapper<Appointment>().eq("patient_id", patient.id).orderByDesc("visit_date")).stream()
            .map(this::appointmentView)
            .toList();
    }

    public Map<String, Object> appointmentView(Appointment appointment) {
        return Map.of(
            "appointment", appointment,
            "doctor", doctors.selectById(appointment.doctorId),
            "patient", patients.selectById(appointment.patientId)
        );
    }

    public void cancelAppointment(Long id) {
        Appointment appointment = appointments.selectById(id);
        PatientProfile patient = currentPatient();
        if (appointment == null || !Objects.equals(appointment.patientId, patient.id)) {
            throw new BusinessException("appointment not found");
        }
        appointment.status = AppointmentStatus.CANCELLED;
        appointments.updateById(appointment);
    }

    public void rescheduleAppointment(Long id, LocalDate visitDate, LocalTime timeSlot) {
        Appointment appointment = appointments.selectById(id);
        PatientProfile patient = currentPatient();
        if (appointment == null || !Objects.equals(appointment.patientId, patient.id)) {
            throw new BusinessException("appointment not found");
        }
        appointment.visitDate = visitDate;
        appointment.timeSlot = timeSlot;
        appointment.status = AppointmentStatus.RESCHEDULED;
        appointments.updateById(appointment);
    }

    public List<MedicalRecord> myRecords() {
        PatientProfile patient = currentPatient();
        return records.selectList(new QueryWrapper<MedicalRecord>().eq("patient_id", patient.id).orderByDesc("created_at"));
    }

    public Map<String, Object> consult(String symptoms) {
        String normalized = Optional.ofNullable(symptoms).orElse("").toLowerCase(Locale.ROOT);
        List<SymptomRule> matched = symptomRules.selectList(new QueryWrapper<SymptomRule>()).stream()
            .filter(rule -> Arrays.stream(rule.keywords.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(keyword -> normalized.contains(keyword.toLowerCase(Locale.ROOT))))
            .toList();
        if (matched.isEmpty()) {
            matched = symptomRules.selectList(new QueryWrapper<SymptomRule>().last("limit 1"));
        }
        Set<String> departments = matched.stream().map(rule -> rule.recommendedDepartment).collect(Collectors.toCollection(LinkedHashSet::new));
        List<DoctorProfile> recommendedDoctors = departments.isEmpty()
            ? approvedDoctors().stream().map(row -> (DoctorProfile) row.get("doctor")).toList()
            : doctors.selectList(new QueryWrapper<DoctorProfile>().eq("review_status", ReviewStatus.APPROVED).in("department", departments));
        Set<Long> medicineIds = matched.stream()
            .flatMap(rule -> Arrays.stream(Optional.ofNullable(rule.recommendedMedicineIds).orElse("").split(",")))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(Long::valueOf)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Medicine> recommendedMedicines = medicineIds.isEmpty()
            ? List.of()
            : medicines.selectBatchIds(medicineIds);
        return Map.of(
            "disclaimer", "AI建议仅为初步参考，不能替代医生诊断。如疼痛明显、出血、发热或肿胀加重，请及时就医。",
            "rules", matched,
            "recommendedDepartments", departments,
            "recommendedDoctors", recommendedDoctors,
            "recommendedMedicines", recommendedMedicines
        );
    }

    public List<MedicationReminder> myReminders() {
        PatientProfile patient = currentPatient();
        return reminders.selectList(new QueryWrapper<MedicationReminder>().eq("patient_id", patient.id).orderByAsc("expected_run_out_date"));
    }

    public List<Map<String, Object>> doctorAppointments(LocalDate date) {
        DoctorProfile doctor = currentDoctor(true);
        QueryWrapper<Appointment> wrapper = new QueryWrapper<Appointment>().eq("doctor_id", doctor.id);
        if (date != null) {
            wrapper.eq("visit_date", date);
        }
        return appointments.selectList(wrapper.orderByAsc("visit_date", "time_slot")).stream()
            .map(this::appointmentView)
            .toList();
    }

    public void doctorUpdateAppointment(Long id, AppointmentStatus status, String reason, LocalDate visitDate, LocalTime timeSlot) {
        DoctorProfile doctor = currentDoctor(true);
        Appointment appointment = appointments.selectById(id);
        if (appointment == null || !Objects.equals(appointment.doctorId, doctor.id)) {
            throw new BusinessException("appointment not found");
        }
        appointment.status = status;
        appointment.statusReason = reason;
        if (visitDate != null) {
            appointment.visitDate = visitDate;
        }
        if (timeSlot != null) {
            appointment.timeSlot = timeSlot;
        }
        appointments.updateById(appointment);
        log("DOCTOR_UPDATE_APPOINTMENT", id + " -> " + status);
    }

    @Transactional
    public MedicalRecord saveRecord(MedicalRecord record) {
        DoctorProfile doctor = currentDoctor(true);
        record.doctorId = doctor.id;
        if (record.createdAt == null) {
            record.createdAt = LocalDateTime.now();
        }
        if (record.id == null) {
            records.insert(record);
        } else {
            records.updateById(record);
        }
        if (record.appointmentId != null) {
            Appointment appointment = appointments.selectById(record.appointmentId);
            if (appointment != null) {
                appointment.status = AppointmentStatus.COMPLETED;
                appointments.updateById(appointment);
            }
        }
        log("SAVE_RECORD", String.valueOf(record.id));
        return record;
    }

    @Transactional
    public Prescription createPrescription(Prescription prescription, List<PrescriptionItem> items) {
        DoctorProfile doctor = currentDoctor(true);
        prescription.doctorId = doctor.id;
        prescription.createdAt = LocalDateTime.now();
        prescriptions.insert(prescription);
        for (PrescriptionItem item : items) {
            Medicine medicine = requireMedicine(item.medicineId);
            item.prescriptionId = prescription.id;
            item.medicineName = medicine.name;
            prescriptionItems.insert(item);
            createMedicationReminder(prescription.patientId, medicine, item);
        }
        log("CREATE_PRESCRIPTION", String.valueOf(prescription.id));
        return prescription;
    }

    private void createMedicationReminder(Long patientId, Medicine medicine, PrescriptionItem item) {
        MedicationReminder reminder = new MedicationReminder();
        reminder.patientId = patientId;
        reminder.medicineId = medicine.id;
        reminder.medicineName = medicine.name;
        reminder.startDate = LocalDate.now();
        reminder.dailyDose = 2;
        reminder.totalQuantity = Math.max(1, Optional.ofNullable(item.days).orElse(3) * reminder.dailyDose);
        reminder.expectedRunOutDate = reminder.startDate.plusDays(Math.max(1, Optional.ofNullable(item.days).orElse(3)));
        reminder.warned = false;
        reminders.insert(reminder);
    }

    public Map<String, Object> doctorStats() {
        DoctorProfile doctor = currentDoctor(true);
        Long appointmentCount = appointments.selectCount(new QueryWrapper<Appointment>().eq("doctor_id", doctor.id));
        Long completed = appointments.selectCount(new QueryWrapper<Appointment>().eq("doctor_id", doctor.id).eq("status", AppointmentStatus.COMPLETED));
        Long noShow = appointments.selectCount(new QueryWrapper<Appointment>().eq("doctor_id", doctor.id).eq("status", AppointmentStatus.NO_SHOW));
        Long reviewCount = reviews.selectCount(new QueryWrapper<DoctorReview>().eq("doctor_id", doctor.id));
        return Map.of("appointmentCount", appointmentCount, "completedCount", completed, "noShowCount", noShow, "reviewCount", reviewCount, "rating", doctor.rating);
    }

    public void replyMessage(Long messageId, String reply) {
        DoctorProfile doctor = currentDoctor(true);
        Message message = messages.selectById(messageId);
        if (message == null || !Objects.equals(message.doctorId, doctor.id)) {
            throw new BusinessException("message not found");
        }
        message.reply = reply;
        message.repliedAt = LocalDateTime.now();
        messages.updateById(message);
    }

    public void reviewDoctor(Long doctorId, Integer rating, String comment) {
        PatientProfile patient = currentPatient();
        DoctorReview review = new DoctorReview();
        review.patientId = patient.id;
        review.doctorId = doctorId;
        review.rating = Math.max(1, Math.min(5, rating));
        review.comment = comment;
        review.createdAt = LocalDateTime.now();
        reviews.insert(review);
        List<DoctorReview> all = reviews.selectList(new QueryWrapper<DoctorReview>().eq("doctor_id", doctorId));
        DoctorProfile doctor = doctors.selectById(doctorId);
        doctor.rating = all.stream().mapToInt(row -> row.rating).average().orElse(5.0);
        doctors.updateById(doctor);
    }

    public Map<String, Object> adminStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("users", users.selectCount(new QueryWrapper<UserAccount>()));
        stats.put("doctors", doctors.selectCount(new QueryWrapper<DoctorProfile>()));
        stats.put("appointments", appointments.selectCount(new QueryWrapper<Appointment>()));
        stats.put("orders", orders.selectCount(new QueryWrapper<MedicineOrder>()));
        stats.put("records", records.selectCount(new QueryWrapper<MedicalRecord>()));
        stats.put("medicineSales", orderItems.selectList(new QueryWrapper<OrderItem>()).stream()
            .collect(Collectors.groupingBy(item -> item.medicineName, LinkedHashMap::new, Collectors.summingInt(item -> item.quantity))));
        stats.put("commonSymptoms", appointments.selectList(new QueryWrapper<Appointment>()).stream()
            .map(row -> Optional.ofNullable(row.symptoms).orElse("未填写"))
            .collect(Collectors.groupingBy(text -> text, LinkedHashMap::new, Collectors.counting())));
        return stats;
    }

    public void reviewDoctor(Long doctorId, ReviewStatus status, String comment) {
        DoctorProfile doctor = doctors.selectById(doctorId);
        if (doctor == null) {
            throw new BusinessException("doctor not found");
        }
        doctor.reviewStatus = status;
        doctors.updateById(doctor);
        DoctorQualification qualification = qualifications.selectOne(new QueryWrapper<DoctorQualification>().eq("doctor_id", doctorId).last("limit 1"));
        if (qualification != null) {
            qualification.status = status;
            qualification.reviewComment = comment;
            qualification.reviewedAt = LocalDateTime.now();
            qualifications.updateById(qualification);
        }
        log("REVIEW_DOCTOR", doctor.name + " -> " + status);
    }

    public void resetPassword(Long userId, String password) {
        UserAccount account = users.selectById(userId);
        if (account == null) {
            throw new BusinessException("user not found");
        }
        account.passwordHash = passwordEncoder.encode(password);
        users.updateById(account);
        log("RESET_PASSWORD", account.username);
    }

    public Medicine requireMedicine(Long id) {
        Medicine medicine = medicines.selectById(id);
        if (medicine == null) {
            throw new BusinessException("medicine not found");
        }
        return medicine;
    }

    public void log(String action, String detail) {
        OperationLog log = new OperationLog();
        try {
            UserPrincipal principal = SecurityUtils.current();
            log.operatorId = principal.id;
            log.operatorRole = principal.role.name();
        } catch (Exception ignored) {
            log.operatorRole = "SYSTEM";
        }
        log.action = action;
        log.detail = detail;
        log.createdAt = LocalDateTime.now();
        logs.insert(log);
    }

    public record OrderLine(Long medicineId, int quantity) {}
}
