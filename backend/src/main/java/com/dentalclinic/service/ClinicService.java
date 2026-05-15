package com.dentalclinic.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dentalclinic.common.BusinessException;
import com.dentalclinic.domain.*;
import com.dentalclinic.entity.*;
import com.dentalclinic.mapper.*;
import com.dentalclinic.security.SecurityUtils;
import com.dentalclinic.security.UserPrincipal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ClinicService {
    private final UserAccountMapper users;
    private final PatientProfileMapper patients;
    private final DoctorProfileMapper doctors;
    private final DoctorQualificationMapper qualifications;
    private final DoctorScheduleMapper schedules;
    private final AnnouncementMapper announcements;
    private final MedicineMapper medicines;
    private final CartItemMapper cartItems;
    private final MedicineOrderMapper orders;
    private final OrderItemMapper orderItems;
    private final AppointmentMapper appointments;
    private final MedicalRecordMapper records;
    private final PrescriptionMapper prescriptions;
    private final PrescriptionItemMapper prescriptionItems;
    private final MessageMapper messages;
    private final DoctorReviewMapper reviews;
    private final SymptomRuleMapper symptomRules;
    private final AiConfigMapper aiConfigs;
    private final MedicationReminderMapper reminders;
    private final OperationLogMapper logs;
    private final PasswordEncoder passwordEncoder;
    private final AiConsultationService aiConsultationService;

    public ClinicService(UserAccountMapper users, PatientProfileMapper patients, DoctorProfileMapper doctors,
                         DoctorQualificationMapper qualifications, DoctorScheduleMapper schedules,
                         AnnouncementMapper announcements, MedicineMapper medicines, CartItemMapper cartItems,
                         MedicineOrderMapper orders, OrderItemMapper orderItems, AppointmentMapper appointments,
                         MedicalRecordMapper records, PrescriptionMapper prescriptions,
                         PrescriptionItemMapper prescriptionItems, MessageMapper messages,
                         DoctorReviewMapper reviews, SymptomRuleMapper symptomRules, AiConfigMapper aiConfigs,
                         MedicationReminderMapper reminders, OperationLogMapper logs,
                         PasswordEncoder passwordEncoder, AiConsultationService aiConsultationService) {
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
        this.aiConfigs = aiConfigs;
        this.reminders = reminders;
        this.logs = logs;
        this.passwordEncoder = passwordEncoder;
        this.aiConsultationService = aiConsultationService;
    }

    public UserAccountMapper users() {
        return users;
    }

    public PatientProfileMapper patients() {
        return patients;
    }

    public DoctorProfileMapper doctors() {
        return doctors;
    }

    public DoctorQualificationMapper qualifications() {
        return qualifications;
    }

    public DoctorScheduleMapper schedules() {
        return schedules;
    }

    public AnnouncementMapper announcements() {
        return announcements;
    }

    public MedicineMapper medicines() {
        return medicines;
    }

    public CartItemMapper cartItems() {
        return cartItems;
    }

    public MedicineOrderMapper orders() {
        return orders;
    }

    public AppointmentMapper appointments() {
        return appointments;
    }

    public MedicalRecordMapper records() {
        return records;
    }

    public PrescriptionMapper prescriptions() {
        return prescriptions;
    }

    public MessageMapper messages() {
        return messages;
    }

    public OperationLogMapper logs() {
        return logs;
    }

    public AiConfigMapper aiConfigs() {
        return aiConfigs;
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

    public List<Map<String, Object>> adminUserViews() {
        return users.selectList(new QueryWrapper<UserAccount>().orderByDesc("created_at")).stream().map(account -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("account", account);
            row.put("linkedName", "-");
            row.put("linkedType", "-");
            row.put("doctorName", "-");
            row.put("patientName", "-");
            row.put("department", "-");
            row.put("title", "-");
            row.put("reviewStatus", null);
            if (account.role == Role.DOCTOR) {
                DoctorProfile doctor = doctorByUser(account.id);
                row.put("linkedName", doctor.name);
                row.put("linkedType", "医生资料");
                row.put("doctorName", doctor.name);
                row.put("department", doctor.department);
                row.put("title", doctor.title);
                row.put("reviewStatus", doctor.reviewStatus);
            } else if (account.role == Role.PATIENT) {
                PatientProfile patient = patientByUser(account.id);
                row.put("linkedName", patient.name);
                row.put("linkedType", "患者资料");
                row.put("patientName", patient.name);
            } else {
                row.put("linkedName", account.username);
                row.put("linkedType", "管理员账号");
            }
            return row;
        }).toList();
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
            throw new BusinessException("医生资质尚未审核通过");
        }
        return doctor;
    }

    public PatientProfile patientByUser(Long userId) {
        PatientProfile profile = patients.selectOne(new QueryWrapper<PatientProfile>().eq("user_id", userId));
        if (profile == null) {
            throw new BusinessException("patient profile not found");
        }
        if (profile.balance == null) {
            profile.balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
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

    public List<Announcement> doctorAnnouncements() {
        currentDoctor(false);
        return publishedAnnouncements();
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

    public List<DoctorSchedule> myDoctorSchedules() {
        DoctorProfile doctor = currentDoctor(true);
        return schedules.selectList(new QueryWrapper<DoctorSchedule>()
            .eq("doctor_id", doctor.id)
            .orderByAsc("work_date", "start_time"));
    }

    public List<Map<String, Object>> adminSchedules(Long doctorId, LocalDate workDate) {
        QueryWrapper<DoctorSchedule> wrapper = new QueryWrapper<>();
        if (doctorId != null) {
            wrapper.eq("doctor_id", doctorId);
        }
        if (workDate != null) {
            wrapper.eq("work_date", workDate);
        }
        return schedules.selectList(wrapper.orderByAsc("doctor_id", "work_date", "start_time"))
            .stream()
            .map(schedule -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("schedule", schedule);
                row.put("doctor", doctors.selectById(schedule.doctorId));
                row.put("editable", schedule.bookedCount == null || schedule.bookedCount == 0);
                return row;
            })
            .toList();
    }

    public DoctorSchedule createMySchedule(DoctorSchedule request) {
        DoctorProfile doctor = currentDoctor(true);
        validateScheduleEditable(request.workDate, request.bookedCount);
        request.doctorId = doctor.id;
        request.bookedCount = request.bookedCount == null ? 0 : request.bookedCount;
        schedules.insert(request);
        log("CREATE_SCHEDULE", doctor.name + " " + request.workDate + " " + request.startTime + "-" + request.endTime);
        return request;
    }

    public DoctorSchedule adminCreateSchedule(DoctorSchedule request) {
        DoctorProfile doctor = requireDoctorProfile(request.doctorId);
        validateScheduleEditable(request.workDate, 0);
        request.bookedCount = request.bookedCount == null ? 0 : request.bookedCount;
        schedules.insert(request);
        log("ADMIN_CREATE_SCHEDULE", doctor.name + " " + request.workDate + " " + request.startTime + "-" + request.endTime);
        return request;
    }

    public DoctorSchedule updateMySchedule(Long id, DoctorSchedule request) {
        DoctorProfile doctor = currentDoctor(true);
        DoctorSchedule existing = schedules.selectById(id);
        if (existing == null || !Objects.equals(existing.doctorId, doctor.id)) {
            throw new BusinessException("排班记录不存在或无权操作");
        }
        validateScheduleEditable(existing.workDate, existing.bookedCount);
        validateScheduleEditable(request.workDate, existing.bookedCount);
        existing.workDate = request.workDate;
        existing.startTime = request.startTime;
        existing.endTime = request.endTime;
        existing.capacity = request.capacity;
        schedules.updateById(existing);
        log("UPDATE_SCHEDULE", doctor.name + " schedule=" + id);
        return existing;
    }

    public DoctorSchedule adminUpdateSchedule(Long id, DoctorSchedule request) {
        DoctorSchedule existing = schedules.selectById(id);
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        validateScheduleEditable(existing.workDate, existing.bookedCount);
        validateScheduleEditable(request.workDate, existing.bookedCount);
        DoctorProfile doctor = requireDoctorProfile(request.doctorId);
        existing.doctorId = doctor.id;
        existing.workDate = request.workDate;
        existing.startTime = request.startTime;
        existing.endTime = request.endTime;
        existing.capacity = request.capacity;
        schedules.updateById(existing);
        log("ADMIN_UPDATE_SCHEDULE", doctor.name + " schedule=" + id);
        return existing;
    }

    public void deleteMySchedule(Long id) {
        DoctorProfile doctor = currentDoctor(true);
        DoctorSchedule existing = schedules.selectById(id);
        if (existing == null || !Objects.equals(existing.doctorId, doctor.id)) {
            throw new BusinessException("排班记录不存在或无权操作");
        }
        validateScheduleEditable(existing.workDate, existing.bookedCount);
        schedules.deleteById(id);
        log("DELETE_SCHEDULE", doctor.name + " schedule=" + id);
    }

    public void adminDeleteSchedule(Long id) {
        DoctorSchedule existing = schedules.selectById(id);
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        validateScheduleEditable(existing.workDate, existing.bookedCount);
        schedules.deleteById(id);
        log("ADMIN_DELETE_SCHEDULE", "schedule=" + id);
    }

    private void validateScheduleEditable(LocalDate workDate, Integer bookedCount) {
        if (workDate == null || workDate.isBefore(LocalDate.now())) {
            throw new BusinessException("只能修改未来日期的排班");
        }
        if (bookedCount != null && bookedCount > 0) {
            throw new BusinessException("该排班已有预约，不能修改或删除");
        }
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

    private DoctorProfile requireDoctorProfile(Long doctorId) {
        if (doctorId == null) {
            throw new BusinessException("请选择医生");
        }
        DoctorProfile doctor = doctors.selectById(doctorId);
        if (doctor == null) {
            throw new BusinessException("医生不存在");
        }
        return doctor;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP);
    }

    private void ensurePatientBalance(PatientProfile patient, BigDecimal amount, String scene) {
        BigDecimal normalized = normalizeMoney(amount);
        if (patient.balance == null) {
            patient.balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (patient.balance.compareTo(normalized) < 0) {
            throw new BusinessException(scene + "余额不足，请先充值");
        }
    }

    private DoctorSchedule requireSchedulableSlot(Long doctorId, LocalDate visitDate, LocalTime timeSlot, Long currentScheduleId) {
        DoctorSchedule schedule = schedules.selectList(new QueryWrapper<DoctorSchedule>()
                .eq("doctor_id", doctorId)
                .eq("work_date", visitDate)
                .orderByAsc("start_time"))
            .stream()
            .filter(item -> !timeSlot.isBefore(item.startTime) && timeSlot.isBefore(item.endTime))
            .findFirst()
            .orElseThrow(() -> new BusinessException("所选日期或时间不在医生排班范围内"));
        if (!Objects.equals(schedule.id, currentScheduleId) && (schedule.bookedCount == null ? 0 : schedule.bookedCount) >= schedule.capacity) {
            throw new BusinessException("当前排班号源已满，请重新选择");
        }
        return schedule;
    }

    private void increaseScheduleBookedCount(Long scheduleId, int delta) {
        if (scheduleId == null) {
            return;
        }
        DoctorSchedule schedule = schedules.selectById(scheduleId);
        if (schedule == null) {
            return;
        }
        schedule.bookedCount = Math.max(0, (schedule.bookedCount == null ? 0 : schedule.bookedCount) + delta);
        schedules.updateById(schedule);
    }

    private void releaseScheduleSlot(Appointment appointment) {
        if (appointment.scheduleId == null) {
            return;
        }
        DoctorSchedule schedule = schedules.selectById(appointment.scheduleId);
        if (schedule == null) {
            return;
        }
        schedule.bookedCount = Math.max(0, (schedule.bookedCount == null ? 0 : schedule.bookedCount) - 1);
        schedules.updateById(schedule);
    }

    @Transactional
    public MedicineOrder createOrder(List<OrderLine> lines, String deliveryMethod) {
        PatientProfile patient = currentPatient();
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("order items required");
        }
        List<PreparedOrderItem> preparedItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            Medicine medicine = requireMedicine(line.medicineId());
            int quantity = Math.max(1, line.quantity());
            if (Boolean.FALSE.equals(medicine.onShelf) || medicine.stock < quantity) {
                throw new BusinessException(medicine.name + " stock is insufficient");
            }
            preparedItems.add(new PreparedOrderItem(medicine, quantity));
            total = total.add(medicine.price.multiply(BigDecimal.valueOf(quantity)));
        }
        ensurePatientBalance(patient, total, "药品购买");
        patient.balance = patient.balance.subtract(total);
        patients.updateById(patient);

        MedicineOrder order = new MedicineOrder();
        order.orderNo = "DCO" + System.currentTimeMillis();
        order.patientId = patient.id;
        order.deliveryMethod = StringUtils.hasText(deliveryMethod) ? deliveryMethod : "到店自取";
        order.status = OrderStatus.PAID;
        order.createdAt = LocalDateTime.now();
        order.totalAmount = total;
        orders.insert(order);

        for (PreparedOrderItem preparedItem : preparedItems) {
            Medicine medicine = preparedItem.medicine();
            int quantity = preparedItem.quantity();
            medicine.stock -= quantity;
            medicines.updateById(medicine);
            OrderItem item = new OrderItem();
            item.orderId = order.id;
            item.medicineId = medicine.id;
            item.medicineName = medicine.name;
            item.unitPrice = medicine.price;
            item.quantity = quantity;
            orderItems.insert(item);
        }
        log("CREATE_ORDER", order.orderNo + " amount=" + total);
        return order;
    }

    public List<Map<String, Object>> myOrders() {
        PatientProfile patient = currentPatient();
        return orders.selectList(new QueryWrapper<MedicineOrder>().eq("patient_id", patient.id).orderByDesc("created_at")).stream()
            .map(this::orderWithItems)
            .toList();
    }

    public Map<String, Object> orderWithItems(MedicineOrder order) {
        List<OrderItem> items = orderItems.selectList(new QueryWrapper<OrderItem>().eq("order_id", order.id));
        String medicineNames = items.stream()
            .map(item -> item.medicineName + " x" + item.quantity)
            .collect(Collectors.joining("，"));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("order", order);
        row.put("items", items);
        row.put("medicineNames", medicineNames);
        row.put("createdAt", order.createdAt);
        return row;
    }

    public void updateOrderStatus(Long orderId, OrderStatus status) {
        MedicineOrder order = orders.selectById(orderId);
        if (order == null) {
            throw new BusinessException("order not found");
        }
        OrderStatus previousStatus = order.status;
        order.status = status;
        orders.updateById(order);
        if (status == OrderStatus.REFUNDED && previousStatus != OrderStatus.REFUNDED) {
            refundOrderToBalance(order);
            restoreOrderStock(order.id);
        }
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
        DoctorSchedule schedule = requireSchedulableSlot(doctorId, visitDate, timeSlot, null);
        Long conflicts = appointments.selectCount(new QueryWrapper<Appointment>()
            .eq("doctor_id", doctorId)
            .eq("visit_date", visitDate)
            .eq("time_slot", timeSlot)
            .in("status", List.of(AppointmentStatus.SUBMITTED, AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULED)));
        if (conflicts > 0) {
            throw new BusinessException("time slot is already booked");
        }
        BigDecimal fee = normalizeMoney(doctor.consultationFee);
        ensurePatientBalance(patient, fee, "挂号预约");
        patient.balance = patient.balance.subtract(fee);
        patients.updateById(patient);
        Appointment appointment = new Appointment();
        appointment.patientId = patient.id;
        appointment.doctorId = doctorId;
        appointment.scheduleId = schedule.id;
        appointment.visitDate = visitDate;
        appointment.timeSlot = timeSlot;
        appointment.symptoms = symptoms;
        appointment.demand = demand;
        appointment.feeAmount = fee;
        appointment.feeRefunded = false;
        appointment.status = AppointmentStatus.SUBMITTED;
        appointment.createdAt = LocalDateTime.now();
        appointments.insert(appointment);
        increaseScheduleBookedCount(schedule.id, 1);
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
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("appointment", appointment);
        row.put("doctor", doctors.selectById(appointment.doctorId));
        row.put("patient", patients.selectById(appointment.patientId));
        row.put("schedule", appointment.scheduleId == null ? null : schedules.selectById(appointment.scheduleId));
        row.put("reviewed", appointment.id != null && reviews.selectCount(new QueryWrapper<DoctorReview>().eq("appointment_id", appointment.id)) > 0);
        return row;
    }

    public void cancelAppointment(Long id) {
        Appointment appointment = appointments.selectById(id);
        PatientProfile patient = currentPatient();
        if (appointment == null || !Objects.equals(appointment.patientId, patient.id)) {
            throw new BusinessException("appointment not found");
        }
        if (!List.of(AppointmentStatus.SUBMITTED, AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULED).contains(appointment.status)) {
            throw new BusinessException("当前预约状态不支持取消");
        }
        appointment.status = AppointmentStatus.CANCELLED;
        appointment.statusReason = "患者已取消预约。";
        appointments.updateById(appointment);
        releaseScheduleSlot(appointment);
        refundAppointmentFeeIfNeeded(appointment, "患者取消预约，挂号费已退回余额。");
    }

    public void rescheduleAppointment(Long id, LocalDate visitDate, LocalTime timeSlot) {
        Appointment appointment = appointments.selectById(id);
        PatientProfile patient = currentPatient();
        if (appointment == null || !Objects.equals(appointment.patientId, patient.id)) {
            throw new BusinessException("appointment not found");
        }
        Long conflicts = appointments.selectCount(new QueryWrapper<Appointment>()
            .eq("doctor_id", appointment.doctorId)
            .eq("visit_date", visitDate)
            .eq("time_slot", timeSlot)
            .ne("id", appointment.id)
            .in("status", List.of(AppointmentStatus.SUBMITTED, AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULED)));
        if (conflicts > 0) {
            throw new BusinessException("该时间段已被预约，请重新选择");
        }
        DoctorSchedule nextSchedule = requireSchedulableSlot(appointment.doctorId, visitDate, timeSlot, appointment.scheduleId);
        if (!Objects.equals(appointment.scheduleId, nextSchedule.id)) {
            releaseScheduleSlot(appointment);
            increaseScheduleBookedCount(nextSchedule.id, 1);
            appointment.scheduleId = nextSchedule.id;
        }
        appointment.visitDate = visitDate;
        appointment.timeSlot = timeSlot;
        appointment.status = AppointmentStatus.RESCHEDULED;
        appointment.statusReason = "患者已改期，请以最新预约时间为准。";
        appointments.updateById(appointment);
    }

    public List<Map<String, Object>> myRecords() {
        PatientProfile patient = currentPatient();
        return records.selectList(new QueryWrapper<MedicalRecord>().eq("patient_id", patient.id).orderByDesc("created_at"))
            .stream()
            .map(this::recordWithPrescription)
            .toList();
    }

    public List<Map<String, Object>> myDoctorRecordViews(Long patientId, LocalDate dateFrom, LocalDate dateTo) {
        DoctorProfile doctor = currentDoctor(true);
        QueryWrapper<MedicalRecord> wrapper = new QueryWrapper<MedicalRecord>().eq("doctor_id", doctor.id);
        if (patientId != null) {
            wrapper.eq("patient_id", patientId);
        }
        if (dateFrom != null) {
            wrapper.ge("created_at", dateFrom.atStartOfDay());
        }
        if (dateTo != null) {
            wrapper.le("created_at", dateTo.atTime(23, 59, 59));
        }
        return records.selectList(wrapper.orderByDesc("created_at"))
            .stream()
            .map(this::recordWithPrescription)
            .toList();
    }

    public Map<String, Object> myDoctorRecordDetail(Long recordId) {
        DoctorProfile doctor = currentDoctor(true);
        MedicalRecord record = records.selectById(recordId);
        if (record == null || !Objects.equals(record.doctorId, doctor.id)) {
            throw new BusinessException("病例不存在或无权查看");
        }
        return recordWithPrescription(record);
    }

    public Map<String, Object> recordWithPrescription(MedicalRecord record) {
        List<Prescription> prescriptionRows = prescriptions.selectList(new QueryWrapper<Prescription>().eq("record_id", record.id));
        List<Map<String, Object>> prescriptionViews = prescriptionRows.stream().map(prescription -> {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("prescription", prescription);
            view.put("items", prescriptionItems.selectList(new QueryWrapper<PrescriptionItem>().eq("prescription_id", prescription.id)));
            return view;
        }).toList();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("record", record);
        view.put("patient", patients.selectById(record.patientId));
        view.put("doctor", doctors.selectById(record.doctorId));
        view.put("appointment", record.appointmentId == null ? null : appointments.selectById(record.appointmentId));
        view.put("prescriptions", prescriptionViews);
        return view;
    }

    public Map<String, Object> prescriptionWithItems(Prescription prescription) {
        List<PrescriptionItem> items = prescriptionItems.selectList(new QueryWrapper<PrescriptionItem>().eq("prescription_id", prescription.id));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("prescription", prescription);
        view.put("record", prescription.recordId == null ? null : records.selectById(prescription.recordId));
        view.put("patient", patients.selectById(prescription.patientId));
        view.put("doctor", doctors.selectById(prescription.doctorId));
        view.put("items", items);
        view.put("medicineNames", items.stream().map(item -> item.medicineName + " x" + (item.days == null ? "-" : item.days)).collect(Collectors.joining("，")));
        view.put("riskFlag", auditPrescription(items));
        return view;
    }

    public Map<String, Object> adminOrderView(MedicineOrder order) {
        Map<String, Object> row = orderWithItems(order);
        row.put("patient", patients.selectById(order.patientId));
        row.put("paymentStatus", orderPaymentStatus(order.status));
        row.put("deliveryStatus", orderDeliveryStatus(order.status));
        return row;
    }

    public List<Map<String, Object>> myPatientMessageViews() {
        PatientProfile patient = currentPatient();
        return messages.selectList(new QueryWrapper<Message>().eq("patient_id", patient.id).orderByDesc("created_at"))
            .stream()
            .map(message -> {
                boolean systemNotice = Optional.ofNullable(message.question).orElse("").startsWith("【预约通知】");
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("message", message);
                row.put("doctor", message.doctorId == null ? null : doctors.selectById(message.doctorId));
                row.put("systemNotice", systemNotice);
                row.put("replyStatus", systemNotice ? "系统通知" : StringUtils.hasText(message.reply) ? "已回复" : "待回复");
                return row;
            })
            .toList();
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
        Map<String, Object> aiResult = aiConsultationService.consult(symptoms, matched, recommendedDoctors, recommendedMedicines);
        Map<String, Object> result = new LinkedHashMap<>(aiResult);
        result.put("rules", matched);
        result.put("recommendedDepartments", departments);
        result.put("recommendedDoctors", recommendedDoctors);
        result.put("recommendedMedicines", recommendedMedicines);
        return result;
    }

    public List<Map<String, Object>> myReminders() {
        PatientProfile patient = currentPatient();
        List<MedicationReminder> rows = reminders.selectList(new QueryWrapper<MedicationReminder>()
            .eq("patient_id", patient.id)
            .orderByDesc("expected_run_out_date", "start_date"));
        LocalDate warnDate = LocalDate.now().plusDays(2);
        Map<Long, MedicationReminder> merged = new LinkedHashMap<>();
        for (MedicationReminder reminder : rows) {
            if (reminder.expectedRunOutDate != null && !reminder.expectedRunOutDate.isAfter(warnDate) && Boolean.FALSE.equals(reminder.warned)) {
                reminder.warned = true;
                reminders.updateById(reminder);
            }
            MedicationReminder existing = merged.get(reminder.medicineId);
            if (existing == null || isReminderNewer(reminder, existing)) {
                merged.put(reminder.medicineId, reminder);
            }
        }
        return merged.values().stream()
            .sorted(Comparator.comparing((MedicationReminder row) -> row.expectedRunOutDate, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(this::reminderView)
            .toList();
    }

    public List<Map<String, Object>> myReminderAlerts() {
        return myReminders().stream()
            .filter(row -> !"normal".equals(row.get("level")))
            .toList();
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
        if (status == AppointmentStatus.CONFIRMED && appointment.status != AppointmentStatus.SUBMITTED) {
            throw new BusinessException("only submitted appointments can be confirmed");
        }
        if (status == AppointmentStatus.REJECTED && appointment.status != AppointmentStatus.SUBMITTED) {
            throw new BusinessException("only submitted appointments can be rejected");
        }
        if (visitDate != null || timeSlot != null) {
            LocalDate nextDate = visitDate == null ? appointment.visitDate : visitDate;
            LocalTime nextTime = timeSlot == null ? appointment.timeSlot : timeSlot;
            Long conflicts = appointments.selectCount(new QueryWrapper<Appointment>()
                .eq("doctor_id", doctor.id)
                .eq("visit_date", nextDate)
                .eq("time_slot", nextTime)
                .ne("id", appointment.id)
                .in("status", List.of(AppointmentStatus.SUBMITTED, AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULED)));
            if (conflicts > 0) {
                throw new BusinessException("该时间段已被预约，请重新选择");
            }
            DoctorSchedule nextSchedule = requireSchedulableSlot(
                doctor.id,
                nextDate,
                nextTime,
                appointment.scheduleId
            );
            if (!Objects.equals(appointment.scheduleId, nextSchedule.id)) {
                releaseScheduleSlot(appointment);
                increaseScheduleBookedCount(nextSchedule.id, 1);
                appointment.scheduleId = nextSchedule.id;
            }
            status = AppointmentStatus.RESCHEDULED;
        }
        appointment.status = status;
        if (visitDate != null) {
            appointment.visitDate = visitDate;
        }
        if (timeSlot != null) {
            appointment.timeSlot = timeSlot;
        }
        appointment.statusReason = appointmentNotice(appointment, doctor, status, reason);
        appointments.updateById(appointment);
        if (status == AppointmentStatus.REJECTED) {
            releaseScheduleSlot(appointment);
            refundAppointmentFeeIfNeeded(appointment, "医生已拒绝预约，挂号费已退回余额。");
        }
        createAppointmentNotification(appointment, doctor);
        log("DOCTOR_UPDATE_APPOINTMENT", id + " -> " + status);
    }

    private String appointmentNotice(Appointment appointment, DoctorProfile doctor, AppointmentStatus status, String reason) {
        if (StringUtils.hasText(reason)) {
            return reason;
        }
        if (status == AppointmentStatus.CONFIRMED) {
            return "预约已确认，请于 " + appointment.visitDate + " " + appointment.timeSlot + " 到诊，接诊医生：" + doctor.name + "。";
        }
        if (status == AppointmentStatus.REJECTED) {
            return "预约暂无法接诊，请重新选择其他时间或医生。";
        }
        if (status == AppointmentStatus.RESCHEDULED) {
            return "预约时间已调整为 " + appointment.visitDate + " " + appointment.timeSlot + "，如不方便可取消后重新预约。";
        }
        if (status == AppointmentStatus.COMPLETED) {
            return "本次就诊已完成，病例和处方可在患者端查看。";
        }
        if (status == AppointmentStatus.CANCELLED) {
            return "预约已取消。";
        }
        if (status == AppointmentStatus.NO_SHOW) {
            return "系统记录为爽约，如有疑问请联系诊所。";
        }
        return "预约状态已更新为 " + status + "。";
    }

    private String orderPaymentStatus(OrderStatus status) {
        if (status == OrderStatus.PENDING_PAY) {
            return "待支付";
        }
        if (status == OrderStatus.PAID || status == OrderStatus.SHIPPED || status == OrderStatus.COMPLETED) {
            return "已支付";
        }
        if (status == OrderStatus.REFUND_REQUESTED) {
            return "退款申请中";
        }
        if (status == OrderStatus.REFUNDED) {
            return "已退款";
        }
        return "已取消";
    }

    private String orderDeliveryStatus(OrderStatus status) {
        if (status == OrderStatus.PENDING_PAY || status == OrderStatus.PAID) {
            return "待发货";
        }
        if (status == OrderStatus.SHIPPED) {
            return "配送中";
        }
        if (status == OrderStatus.COMPLETED) {
            return "已完成";
        }
        if (status == OrderStatus.REFUND_REQUESTED) {
            return "退款处理中";
        }
        if (status == OrderStatus.REFUNDED) {
            return "已退款";
        }
        return "已取消";
    }

    private void createAppointmentNotification(Appointment appointment, DoctorProfile doctor) {
        Message notification = new Message();
        notification.patientId = appointment.patientId;
        notification.doctorId = doctor.id;
        notification.question = "【预约通知】" + appointment.statusReason;
        notification.reply = "系统自动通知";
        notification.createdAt = LocalDateTime.now();
        notification.repliedAt = LocalDateTime.now();
        messages.insert(notification);
    }

    @Transactional
    public MedicalRecord saveRecord(MedicalRecord record) {
        DoctorProfile doctor = currentDoctor(true);
        boolean isNew = record.id == null;
        record.doctorId = doctor.id;
        if (isNew) {
            if (record.createdAt == null) {
                record.createdAt = LocalDateTime.now();
            }
            records.insert(record);
        } else {
            MedicalRecord existing = records.selectById(record.id);
            if (existing == null || !Objects.equals(existing.doctorId, doctor.id)) {
                throw new BusinessException("病例不存在或无权修改");
            }
            record.createdAt = existing.createdAt;
            if (record.patientId == null) {
                record.patientId = existing.patientId;
            }
            if (record.appointmentId == null) {
                record.appointmentId = existing.appointmentId;
            }
            records.updateById(record);
        }
        if (isNew && record.appointmentId != null) {
            Appointment appointment = appointments.selectById(record.appointmentId);
            if (appointment == null || !Objects.equals(appointment.doctorId, doctor.id)) {
                throw new BusinessException("预约记录不存在或无权操作");
            }
            if (!Objects.equals(appointment.status, AppointmentStatus.COMPLETED)) {
                appointment.status = AppointmentStatus.COMPLETED;
                appointment.statusReason = "本次就诊已完成，医生已保存病例记录。";
                appointments.updateById(appointment);
                createAppointmentNotification(appointment, doctor);
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
            if (Boolean.FALSE.equals(medicine.onShelf) || medicine.stock <= 0) {
                throw new BusinessException(medicine.name + " 库存不足，无法开具");
            }
            medicine.stock -= 1;
            medicines.updateById(medicine);
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

    private boolean isReminderNewer(MedicationReminder candidate, MedicationReminder existing) {
        if (candidate.expectedRunOutDate == null) {
            return false;
        }
        if (existing.expectedRunOutDate == null) {
            return true;
        }
        if (candidate.expectedRunOutDate.isAfter(existing.expectedRunOutDate)) {
            return true;
        }
        if (candidate.expectedRunOutDate.isEqual(existing.expectedRunOutDate)) {
            LocalDate candidateStart = candidate.startDate == null ? LocalDate.MIN : candidate.startDate;
            LocalDate existingStart = existing.startDate == null ? LocalDate.MIN : existing.startDate;
            return candidateStart.isAfter(existingStart);
        }
        return false;
    }

    private Map<String, Object> reminderView(MedicationReminder reminder) {
        Medicine medicine = medicines.selectById(reminder.medicineId);
        long daysLeft = reminder.expectedRunOutDate == null ? Long.MAX_VALUE
            : java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), reminder.expectedRunOutDate);
        String level = "normal";
        String title = reminder.medicineName + " 当前暂无补药提醒";
        String message = "按当前处方估算，药量暂时充足。";
        String actionText = "回购";
        if (daysLeft < 0) {
            level = "overdue";
            title = reminder.medicineName + " 可能已经用完";
            message = "已超过预计用完时间，建议根据医嘱尽快复诊或补药。";
            actionText = "立即补药";
        } else if (daysLeft == 0) {
            level = "urgent";
            title = reminder.medicineName + " 预计今天用完";
            message = "建议今天完成补药，避免中断当前用药计划。";
            actionText = "立即补药";
        } else if (daysLeft <= 2) {
            level = "soon";
            title = reminder.medicineName + " 预计 " + daysLeft + " 天后用完";
            message = "建议提前安排补药或复诊，保持用药连续性。";
            actionText = "提前补药";
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reminder", reminder);
        row.put("medicine", medicine);
        row.put("daysLeft", daysLeft);
        row.put("level", level);
        row.put("title", title);
        row.put("message", message);
        row.put("actionText", actionText);
        return row;
    }

    private void refundAppointmentFeeIfNeeded(Appointment appointment, String reason) {
        if (Boolean.TRUE.equals(appointment.feeRefunded)) {
            return;
        }
        BigDecimal fee = normalizeMoney(appointment.feeAmount);
        if (fee.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        PatientProfile patient = patients.selectById(appointment.patientId);
        patient.balance = patient.balance.add(fee);
        patients.updateById(patient);
        appointment.feeRefunded = true;
        appointments.updateById(appointment);
        log("REFUND_APPOINTMENT_FEE", "appointment=" + appointment.id + ", amount=" + fee + ", reason=" + reason);
    }

    private void refundOrderToBalance(MedicineOrder order) {
        PatientProfile patient = patients.selectById(order.patientId);
        BigDecimal amount = normalizeMoney(order.totalAmount);
        patient.balance = patient.balance.add(amount);
        patients.updateById(patient);
        log("REFUND_ORDER_BALANCE", order.orderNo + " amount=" + amount);
    }

    private void restoreOrderStock(Long orderId) {
        List<OrderItem> items = orderItems.selectList(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        for (OrderItem item : items) {
            Medicine medicine = medicines.selectById(item.medicineId);
            if (medicine != null) {
                medicine.stock += item.quantity;
                medicines.updateById(medicine);
            }
        }
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

    public void reviewDoctor(Long doctorId, Long appointmentId, Integer rating, String comment) {
        PatientProfile patient = currentPatient();
        Appointment appointment = appointments.selectById(appointmentId);
        if (appointment == null || !Objects.equals(appointment.patientId, patient.id) || !Objects.equals(appointment.doctorId, doctorId)) {
            throw new BusinessException("预约记录不存在或无权评分");
        }
        if (appointment.status != AppointmentStatus.COMPLETED) {
            throw new BusinessException("仅已完成的就诊可以评分");
        }
        if (reviews.selectCount(new QueryWrapper<DoctorReview>().eq("appointment_id", appointmentId)) > 0) {
            throw new BusinessException("该次就诊已完成评分");
        }
        DoctorReview review = new DoctorReview();
        review.patientId = patient.id;
        review.doctorId = doctorId;
        review.appointmentId = appointmentId;
        review.rating = Math.max(1, Math.min(5, rating));
        review.comment = comment;
        review.createdAt = LocalDateTime.now();
        reviews.insert(review);
        List<DoctorReview> all = reviews.selectList(new QueryWrapper<DoctorReview>().eq("doctor_id", doctorId));
        DoctorProfile doctor = doctors.selectById(doctorId);
        doctor.rating = BigDecimal.valueOf(all.stream().mapToInt(row -> row.rating).average().orElse(5.0))
            .setScale(1, RoundingMode.HALF_UP)
            .doubleValue();
        doctors.updateById(doctor);
    }

    @Transactional
    public PatientProfile rechargeBalance(BigDecimal amount) {
        PatientProfile patient = currentPatient();
        BigDecimal normalized = normalizeMoney(amount);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于 0");
        }
        patient.balance = patient.balance.add(normalized);
        patients.updateById(patient);
        log("RECHARGE_BALANCE", "patient=" + patient.id + ", amount=" + normalized);
        return patient;
    }

    public Map<String, Object> adminStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<Appointment> appointmentRows = appointments.selectList(new QueryWrapper<Appointment>());
        List<MedicineOrder> orderRows = orders.selectList(new QueryWrapper<MedicineOrder>());
        List<MedicalRecord> recordRows = records.selectList(new QueryWrapper<MedicalRecord>());
        stats.put("users", users.selectCount(new QueryWrapper<UserAccount>()));
        stats.put("doctors", doctors.selectCount(new QueryWrapper<DoctorProfile>()));
        stats.put("appointments", appointmentRows.size());
        stats.put("orders", orderRows.size());
        stats.put("records", recordRows.size());
        stats.put("activePatients", Stream.of(
                appointmentRows.stream().map(row -> row.patientId),
                orderRows.stream().map(row -> row.patientId),
                recordRows.stream().map(row -> row.patientId))
            .flatMap(s -> s)
            .collect(Collectors.toCollection(LinkedHashSet::new))
            .size());
        stats.put("appointmentStatusCounts", appointmentRows.stream().collect(Collectors.groupingBy(
            row -> row.status.name(), LinkedHashMap::new, Collectors.counting())));
        stats.put("orderStatusCounts", orderRows.stream().collect(Collectors.groupingBy(
            row -> row.status.name(), LinkedHashMap::new, Collectors.counting())));
        stats.put("peakHours", appointmentRows.stream().collect(Collectors.groupingBy(
            row -> row.timeSlot == null ? "未知" : String.format("%02d:00", row.timeSlot.getHour()),
            LinkedHashMap::new,
            Collectors.counting())));
        stats.put("commonSymptoms", appointmentRows.stream()
            .map(row -> Optional.ofNullable(row.symptoms).orElse("未填写"))
            .collect(Collectors.groupingBy(text -> text, LinkedHashMap::new, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(6)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new)));
        stats.put("medicineSales", orderItems.selectList(new QueryWrapper<OrderItem>()).stream()
            .collect(Collectors.groupingBy(item -> item.medicineName, LinkedHashMap::new, Collectors.summingInt(item -> item.quantity))));
        stats.put("doctorWorkload", doctors.selectList(new QueryWrapper<DoctorProfile>()).stream().map(doctor -> {
            Map<String, Object> row = new LinkedHashMap<>();
            long total = appointmentRows.stream().filter(item -> Objects.equals(item.doctorId, doctor.id)).count();
            long completed = appointmentRows.stream().filter(item -> Objects.equals(item.doctorId, doctor.id) && item.status == AppointmentStatus.COMPLETED).count();
            long noShow = appointmentRows.stream().filter(item -> Objects.equals(item.doctorId, doctor.id) && item.status == AppointmentStatus.NO_SHOW).count();
            row.put("doctor", doctor);
            row.put("appointmentCount", total);
            row.put("completedCount", completed);
            row.put("noShowCount", noShow);
            return row;
        }).sorted((left, right) -> Long.compare((Long) right.get("appointmentCount"), (Long) left.get("appointmentCount"))).limit(6).toList());
        return stats;
    }

    private String auditPrescription(List<PrescriptionItem> items) {
        if (items == null || items.isEmpty()) {
            return "未开药";
        }
        List<String> flags = new ArrayList<>();
        if (items.size() > 3) {
            flags.add("药品种类较多");
        }
        if (items.stream().anyMatch(item -> item.days != null && item.days > 14)) {
            flags.add("用药天数较长");
        }
        if (items.stream().anyMatch(item -> item.frequency == null || item.frequency.isBlank())) {
            flags.add("频次缺失");
        }
        return flags.isEmpty() ? "未发现明显异常" : String.join("，", flags);
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

    private record PreparedOrderItem(Medicine medicine, int quantity) {}
    public record OrderLine(Long medicineId, int quantity) {}
}
