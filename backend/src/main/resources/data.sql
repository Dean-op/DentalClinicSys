INSERT IGNORE INTO user_account (id, username, password_hash, role, status, created_at) VALUES
(1, 'admin', '{noop}123456', 'ADMIN', 'ENABLED', NOW()),
(2, 'doctor_chen', '{noop}123456', 'DOCTOR', 'ENABLED', NOW()),
(3, 'patient_li', '{noop}123456', 'PATIENT', 'ENABLED', NOW());

INSERT IGNORE INTO patient_profile (id, user_id, name, gender, phone, address, allergy_history) VALUES
(1, 3, '李明', '男', '13800000001', '广州市天河区示例路 18 号', '青霉素过敏');

INSERT IGNORE INTO doctor_profile (id, user_id, name, title, department, specialty, introduction, review_status, rating) VALUES
(1, 2, '陈洁', '主治医师', '口腔综合科', '龋齿修复、牙髓炎、牙周基础治疗', '从事口腔临床工作 8 年，擅长常见口腔疾病诊疗与患者健康宣教。', 'APPROVED', 4.8);

INSERT IGNORE INTO doctor_qualification (id, doctor_id, certificate_type, file_path, status, review_comment, submitted_at, reviewed_at) VALUES
(1, 1, '执业医师资格证', '/uploads/demo-certificate.jpg', 'APPROVED', '资质完整，允许开展在线诊疗。', NOW(), NOW());

INSERT IGNORE INTO doctor_schedule (id, doctor_id, work_date, start_time, end_time, capacity, booked_count) VALUES
(1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '12:00:00', 8, 0),
(2, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', '18:00:00', 8, 0);

INSERT IGNORE INTO announcement (id, title, category, content, status, publish_at, created_at) VALUES
(1, '五一节后门诊时间安排', '节假日安排', '节后门诊恢复正常，上午 09:00-12:00，下午 14:00-18:00。', 'PUBLISHED', NOW(), NOW()),
(2, '儿童涂氟优惠活动', '优惠活动', '本月儿童涂氟项目享受体验价，建议提前在线预约。', 'PUBLISHED', NOW(), NOW()),
(3, '牙龈出血的常见原因', '健康科普', '牙龈出血可能与牙结石、牙龈炎、刷牙方式不当有关，建议定期洁牙并由医生评估。', 'PUBLISHED', NOW(), NOW());

INSERT IGNORE INTO medicine (id, name, effect, usage_instruction, price, stock, on_shelf) VALUES
(1, '复方氯己定含漱液', '辅助缓解牙龈炎、口腔异味', '每日 2 次，每次含漱 30 秒，遵医嘱使用。', 28.00, 120, 1),
(2, '布洛芬缓释胶囊', '缓解轻中度疼痛', '饭后服用，孕妇、胃溃疡患者需先咨询医生。', 18.50, 80, 1),
(3, '脱敏牙膏', '改善牙本质敏感', '每日刷牙使用，坚持 2-4 周观察效果。', 39.90, 60, 1);

INSERT IGNORE INTO symptom_rule (id, keywords, possible_cause, recommended_department, recommended_medicine_ids, advice) VALUES
(1, '牙痛,疼,痛,冷热刺激', '龋齿、牙髓炎或牙本质敏感', '口腔综合科', '2,3', '避免冷热刺激，保持口腔清洁，疼痛持续或夜间痛明显时尽快预约医生。'),
(2, '牙龈出血,出血,牙龈肿', '牙龈炎、牙结石或刷牙方式不当', '牙周科', '1', '使用软毛牙刷，避免用力横刷，建议进行牙周检查和洁治评估。'),
(3, '口臭,异味', '口腔卫生不佳、牙周炎或龋齿', '口腔综合科', '1', '加强舌苔和牙缝清洁，如长期口臭建议就诊排查。');

INSERT IGNORE INTO doctor_review (id, patient_id, doctor_id, rating, comment, created_at) VALUES
(1, 1, 1, 5, '医生解释很耐心，预约流程也方便。', NOW());

INSERT IGNORE INTO medication_reminder (id, patient_id, medicine_id, medicine_name, start_date, total_quantity, daily_dose, expected_run_out_date, warned) VALUES
(1, 1, 1, '复方氯己定含漱液', CURDATE(), 6, 2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 0);
