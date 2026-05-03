package com.dentalclinic.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dentalclinic.common.ApiResponse;
import com.dentalclinic.domain.*;
import com.dentalclinic.entity.*;
import com.dentalclinic.service.ClinicService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final ClinicService clinicService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(ClinicService clinicService, PasswordEncoder passwordEncoder) {
        this.clinicService = clinicService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public ApiResponse<List<UserAccount>> users() {
        return ApiResponse.ok(clinicService.users().selectList(new QueryWrapper<UserAccount>().orderByDesc("created_at")));
    }

    @PostMapping("/users")
    public ApiResponse<UserAccount> createUser(@RequestBody UserCreateRequest request) {
        UserAccount account = new UserAccount();
        account.username = request.username();
        account.passwordHash = passwordEncoder.encode(request.password() == null ? "123456" : request.password());
        account.role = request.role();
        account.status = AccountStatus.ENABLED;
        account.createdAt = LocalDateTime.now();
        clinicService.users().insert(account);
        if (request.role() == Role.DOCTOR) {
            DoctorProfile profile = new DoctorProfile();
            profile.userId = account.id;
            profile.name = request.name();
            profile.department = "\u53e3\u8154\u7efc\u5408\u79d1";
            profile.title = "\u533b\u5e08";
            profile.reviewStatus = ReviewStatus.PENDING;
            profile.rating = 5.0;
            clinicService.doctors().insert(profile);
        }
        if (request.role() == Role.PATIENT) {
            PatientProfile profile = new PatientProfile();
            profile.userId = account.id;
            profile.name = request.name();
            clinicService.patients().insert(profile);
        }
        return ApiResponse.ok(account);
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        UserAccount account = clinicService.users().selectById(id);
        account.status = request.status();
        clinicService.users().updateById(account);
        return ApiResponse.ok();
    }

    @PutMapping("/users/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        clinicService.resetPassword(id, request.password() == null ? "123456" : request.password());
        return ApiResponse.ok();
    }

    @GetMapping("/doctors")
    public ApiResponse<List<DoctorProfile>> doctors() {
        return ApiResponse.ok(clinicService.doctors().selectList(new QueryWrapper<DoctorProfile>().orderByAsc("review_status")));
    }

    @PutMapping("/doctors/{id}/review")
    public ApiResponse<Void> reviewDoctor(@PathVariable Long id, @RequestBody DoctorReviewRequest request) {
        clinicService.reviewDoctor(id, request.status(), request.comment());
        return ApiResponse.ok();
    }

    @GetMapping("/qualifications")
    public ApiResponse<List<DoctorQualification>> qualifications() {
        return ApiResponse.ok(clinicService.qualifications().selectList(new QueryWrapper<DoctorQualification>().orderByDesc("submitted_at")));
    }

    @GetMapping("/medicines")
    public ApiResponse<List<Medicine>> medicines() {
        return ApiResponse.ok(clinicService.medicines().selectList(new QueryWrapper<Medicine>().orderByAsc("id")));
    }

    @PostMapping("/medicines")
    public ApiResponse<Medicine> createMedicine(@RequestBody Medicine medicine) {
        clinicService.medicines().insert(medicine);
        return ApiResponse.ok(medicine);
    }

    @PutMapping("/medicines/{id}")
    public ApiResponse<Medicine> updateMedicine(@PathVariable Long id, @RequestBody Medicine medicine) {
        medicine.id = id;
        clinicService.medicines().updateById(medicine);
        return ApiResponse.ok(medicine);
    }

    @DeleteMapping("/medicines/{id}")
    public ApiResponse<Void> deleteMedicine(@PathVariable Long id) {
        clinicService.medicines().deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/announcements")
    public ApiResponse<List<Announcement>> announcements() {
        return ApiResponse.ok(clinicService.announcements().selectList(new QueryWrapper<Announcement>().orderByDesc("created_at")));
    }

    @PostMapping("/announcements")
    public ApiResponse<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        announcement.createdAt = LocalDateTime.now();
        clinicService.announcements().insert(announcement);
        return ApiResponse.ok(announcement);
    }

    @PutMapping("/announcements/{id}")
    public ApiResponse<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.id = id;
        clinicService.announcements().updateById(announcement);
        return ApiResponse.ok(announcement);
    }

    @DeleteMapping("/announcements/{id}")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Long id) {
        clinicService.announcements().deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/records")
    public ApiResponse<List<MedicalRecord>> records() {
        return ApiResponse.ok(clinicService.records().selectList(new QueryWrapper<MedicalRecord>().orderByDesc("created_at")));
    }

    @GetMapping("/prescriptions")
    public ApiResponse<List<Prescription>> prescriptions() {
        return ApiResponse.ok(clinicService.prescriptions().selectList(new QueryWrapper<Prescription>().orderByDesc("created_at")));
    }

    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders() {
        return ApiResponse.ok(clinicService.orders().selectList(new QueryWrapper<MedicineOrder>().orderByDesc("created_at"))
            .stream().map(clinicService::orderWithItems).toList());
    }

    @PutMapping("/orders/{id}/status")
    public ApiResponse<Void> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusRequest request) {
        clinicService.updateOrderStatus(id, request.status());
        return ApiResponse.ok();
    }

    @GetMapping("/appointments")
    public ApiResponse<List<Map<String, Object>>> appointments() {
        return ApiResponse.ok(clinicService.appointments().selectList(new QueryWrapper<Appointment>().orderByDesc("visit_date"))
            .stream().map(clinicService::appointmentView).toList());
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(clinicService.adminStats());
    }

    @GetMapping("/logs")
    public ApiResponse<List<OperationLog>> logs() {
        return ApiResponse.ok(clinicService.logs().selectList(new QueryWrapper<OperationLog>().orderByDesc("created_at").last("limit 100")));
    }

    public record UserCreateRequest(String username, String password, Role role, String name) {}
    public record StatusRequest(AccountStatus status) {}
    public record PasswordRequest(String password) {}
    public record DoctorReviewRequest(ReviewStatus status, String comment) {}
    public record OrderStatusRequest(OrderStatus status) {}
}
