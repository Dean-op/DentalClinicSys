package com.dentalclinic.controller;

import com.dentalclinic.common.ApiResponse;
import com.dentalclinic.domain.OrderStatus;
import com.dentalclinic.entity.*;
import com.dentalclinic.service.ClinicService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {
    private final ClinicService clinicService;

    public PatientController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @GetMapping("/profile")
    public ApiResponse<PatientProfile> profile() {
        return ApiResponse.ok(clinicService.currentPatient());
    }

    @PutMapping("/profile")
    public ApiResponse<PatientProfile> saveProfile(@RequestBody PatientProfile request) {
        PatientProfile profile = clinicService.currentPatient();
        profile.name = request.name;
        profile.gender = request.gender;
        profile.phone = request.phone;
        profile.address = request.address;
        profile.allergyHistory = request.allergyHistory;
        clinicService.patients.updateById(profile);
        return ApiResponse.ok(profile);
    }

    @GetMapping("/announcements")
    public ApiResponse<List<Announcement>> announcements() {
        return ApiResponse.ok(clinicService.publishedAnnouncements());
    }

    @GetMapping("/doctors")
    public ApiResponse<List<Map<String, Object>>> doctors() {
        return ApiResponse.ok(clinicService.approvedDoctors());
    }

    @GetMapping("/doctors/{id}")
    public ApiResponse<Map<String, Object>> doctor(@PathVariable Long id) {
        return ApiResponse.ok(clinicService.doctorCard(clinicService.doctors.selectById(id)));
    }

    @GetMapping("/medicines")
    public ApiResponse<List<Medicine>> medicines() {
        return ApiResponse.ok(clinicService.shelfMedicines());
    }

    @PostMapping("/cart")
    public ApiResponse<CartItem> addCart(@RequestBody CartRequest request) {
        return ApiResponse.ok(clinicService.addCart(request.medicineId(), request.quantity()));
    }

    @GetMapping("/cart")
    public ApiResponse<List<Map<String, Object>>> cart() {
        return ApiResponse.ok(clinicService.cart());
    }

    @DeleteMapping("/cart/{id}")
    public ApiResponse<Void> removeCart(@PathVariable Long id) {
        clinicService.cartItems.deleteById(id);
        return ApiResponse.ok();
    }

    @PostMapping("/orders")
    public ApiResponse<MedicineOrder> createOrder(@RequestBody OrderRequest request) {
        return ApiResponse.ok(clinicService.createOrder(request.items(), request.deliveryMethod()));
    }

    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders() {
        return ApiResponse.ok(clinicService.myOrders());
    }

    @PutMapping("/orders/{id}/refund")
    public ApiResponse<Void> refund(@PathVariable Long id) {
        clinicService.updateOrderStatus(id, OrderStatus.REFUND_REQUESTED);
        return ApiResponse.ok();
    }

    @PutMapping("/orders/{id}/delivery")
    public ApiResponse<Void> delivery(@PathVariable Long id, @RequestBody DeliveryRequest request) {
        clinicService.changeDelivery(id, request.deliveryMethod());
        return ApiResponse.ok();
    }

    @PostMapping("/appointments")
    public ApiResponse<Appointment> createAppointment(@RequestBody AppointmentRequest request) {
        return ApiResponse.ok(clinicService.createAppointment(
            request.doctorId(),
            LocalDate.parse(request.visitDate()),
            LocalTime.parse(request.timeSlot()),
            request.symptoms(),
            request.demand()
        ));
    }

    @GetMapping("/appointments")
    public ApiResponse<List<Map<String, Object>>> appointments() {
        return ApiResponse.ok(clinicService.myAppointments());
    }

    @PutMapping("/appointments/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        clinicService.cancelAppointment(id);
        return ApiResponse.ok();
    }

    @PutMapping("/appointments/{id}/reschedule")
    public ApiResponse<Void> reschedule(@PathVariable Long id, @RequestBody RescheduleRequest request) {
        clinicService.rescheduleAppointment(id, LocalDate.parse(request.visitDate()), LocalTime.parse(request.timeSlot()));
        return ApiResponse.ok();
    }

    @GetMapping("/records")
    public ApiResponse<List<MedicalRecord>> records() {
        return ApiResponse.ok(clinicService.myRecords());
    }

    @PostMapping("/ai/consult")
    public ApiResponse<Map<String, Object>> consult(@RequestBody ConsultRequest request) {
        return ApiResponse.ok(clinicService.consult(request.symptoms()));
    }

    @GetMapping("/reminders")
    public ApiResponse<List<MedicationReminder>> reminders() {
        return ApiResponse.ok(clinicService.myReminders());
    }

    @PostMapping("/messages")
    public ApiResponse<Message> createMessage(@RequestBody Message request) {
        PatientProfile patient = clinicService.currentPatient();
        request.patientId = patient.id;
        request.createdAt = java.time.LocalDateTime.now();
        clinicService.messages.insert(request);
        return ApiResponse.ok(request);
    }

    @GetMapping("/messages")
    public ApiResponse<List<Message>> messages() {
        PatientProfile patient = clinicService.currentPatient();
        return ApiResponse.ok(clinicService.messages.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Message>().eq("patient_id", patient.id).orderByDesc("created_at")
        ));
    }

    @PostMapping("/reviews")
    public ApiResponse<Void> review(@RequestBody ReviewRequest request) {
        clinicService.reviewDoctor(request.doctorId(), request.rating(), request.comment());
        return ApiResponse.ok();
    }

    public record CartRequest(Long medicineId, Integer quantity) {}
    public record OrderRequest(List<ClinicService.OrderLine> items, String deliveryMethod) {}
    public record DeliveryRequest(String deliveryMethod) {}
    public record AppointmentRequest(Long doctorId, String visitDate, String timeSlot, String symptoms, String demand) {}
    public record RescheduleRequest(String visitDate, String timeSlot) {}
    public record ConsultRequest(String symptoms) {}
    public record ReviewRequest(Long doctorId, Integer rating, String comment) {}
}
