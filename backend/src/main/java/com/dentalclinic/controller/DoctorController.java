package com.dentalclinic.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dentalclinic.common.ApiResponse;
import com.dentalclinic.domain.AppointmentStatus;
import com.dentalclinic.entity.*;
import com.dentalclinic.service.ClinicService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {
    private final ClinicService clinicService;

    public DoctorController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @GetMapping("/profile")
    public ApiResponse<DoctorProfile> profile() {
        return ApiResponse.ok(clinicService.currentDoctor(false));
    }

    @PutMapping("/profile")
    public ApiResponse<DoctorProfile> saveProfile(@RequestBody DoctorProfile request) {
        DoctorProfile profile = clinicService.currentDoctor(false);
        profile.name = request.name;
        profile.title = request.title;
        profile.department = request.department;
        profile.specialty = request.specialty;
        profile.introduction = request.introduction;
        clinicService.doctors().updateById(profile);
        return ApiResponse.ok(profile);
    }

    @GetMapping("/schedules")
    public ApiResponse<List<DoctorSchedule>> schedules() {
        DoctorProfile doctor = clinicService.currentDoctor(false);
        return ApiResponse.ok(clinicService.schedules().selectList(
            new QueryWrapper<DoctorSchedule>().eq("doctor_id", doctor.id).orderByAsc("work_date", "start_time")
        ));
    }

    @PostMapping("/schedules")
    public ApiResponse<DoctorSchedule> createSchedule(@RequestBody DoctorSchedule request) {
        DoctorProfile doctor = clinicService.currentDoctor(false);
        request.doctorId = doctor.id;
        request.bookedCount = request.bookedCount == null ? 0 : request.bookedCount;
        clinicService.schedules().insert(request);
        return ApiResponse.ok(request);
    }

    @PutMapping("/schedules/{id}")
    public ApiResponse<DoctorSchedule> updateSchedule(@PathVariable Long id, @RequestBody DoctorSchedule request) {
        DoctorProfile doctor = clinicService.currentDoctor(false);
        request.id = id;
        request.doctorId = doctor.id;
        clinicService.schedules().updateById(request);
        return ApiResponse.ok(request);
    }

    @DeleteMapping("/schedules/{id}")
    public ApiResponse<Void> deleteSchedule(@PathVariable Long id) {
        clinicService.schedules().deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/appointments")
    public ApiResponse<List<Map<String, Object>>> appointments(@RequestParam(required = false) String date) {
        return ApiResponse.ok(clinicService.doctorAppointments(date == null ? null : LocalDate.parse(date)));
    }

    @PutMapping("/appointments/{id}")
    public ApiResponse<Void> updateAppointment(@PathVariable Long id, @RequestBody AppointmentAction request) {
        clinicService.doctorUpdateAppointment(
            id,
            request.status(),
            request.reason(),
            request.visitDate() == null ? null : LocalDate.parse(request.visitDate()),
            request.timeSlot() == null ? null : LocalTime.parse(request.timeSlot())
        );
        return ApiResponse.ok();
    }

    @PostMapping("/records")
    public ApiResponse<MedicalRecord> saveRecord(@RequestBody MedicalRecord record) {
        return ApiResponse.ok(clinicService.saveRecord(record));
    }

    @GetMapping("/patients/{patientId}/records")
    public ApiResponse<List<MedicalRecord>> patientHistory(@PathVariable Long patientId) {
        clinicService.currentDoctor(true);
        return ApiResponse.ok(clinicService.records().selectList(
            new QueryWrapper<MedicalRecord>().eq("patient_id", patientId).orderByDesc("created_at")
        ));
    }

    @PostMapping("/prescriptions")
    public ApiResponse<Prescription> createPrescription(@RequestBody PrescriptionRequest request) {
        return ApiResponse.ok(clinicService.createPrescription(request.prescription(), request.items()));
    }

    @GetMapping("/medicines")
    public ApiResponse<List<Medicine>> medicines() {
        clinicService.currentDoctor(true);
        return ApiResponse.ok(clinicService.shelfMedicines());
    }

    @GetMapping("/messages")
    public ApiResponse<List<Message>> messages() {
        DoctorProfile doctor = clinicService.currentDoctor(true);
        return ApiResponse.ok(clinicService.messages().selectList(
            new QueryWrapper<Message>().eq("doctor_id", doctor.id).orderByDesc("created_at")
        ));
    }

    @PutMapping("/messages/{id}/reply")
    public ApiResponse<Void> reply(@PathVariable Long id, @RequestBody ReplyRequest request) {
        clinicService.replyMessage(id, request.reply());
        return ApiResponse.ok();
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(clinicService.doctorStats());
    }

    public record AppointmentAction(AppointmentStatus status, String reason, String visitDate, String timeSlot) {}
    public record PrescriptionRequest(Prescription prescription, List<PrescriptionItem> items) {}
    public record ReplyRequest(String reply) {}
}
