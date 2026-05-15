INSERT INTO user_account (id, username, password_hash, role, status, created_at) VALUES
(1, 'admin', '{noop}123456', 'ADMIN', 'ENABLED', NOW()),
(2, 'doctor_chen', '{noop}123456', 'DOCTOR', 'ENABLED', NOW()),
(3, 'patient_li', '{noop}123456', 'PATIENT', 'ENABLED', NOW()),
(4, 'doctor_wang', '{noop}123456', 'DOCTOR', 'ENABLED', NOW()),
(5, 'doctor_zhao', '{noop}123456', 'DOCTOR', 'ENABLED', NOW()),
(6, 'patient_zhang', '{noop}123456', 'PATIENT', 'ENABLED', NOW()),
(7, 'patient_liu', '{noop}123456', 'PATIENT', 'ENABLED', NOW()),
(8, 'doctor_sun', '{noop}123456', 'DOCTOR', 'ENABLED', NOW()),
(9, 'doctor_lin', '{noop}123456', 'DOCTOR', 'ENABLED', NOW()) ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  password_hash = VALUES(password_hash),
  role = VALUES(role),
  status = VALUES(status),
  created_at = VALUES(created_at);

INSERT INTO patient_profile (id, user_id, name, gender, phone, address, allergy_history, balance) VALUES
(1, 3, '李明', '男', '13800000001', '广州市天河区示例路 18 号', '青霉素过敏', 320.00),
(2, 6, '张晓雨', '女', '13800000002', '广州市越秀区健康路 8 号', '无', 180.00),
(3, 7, '刘浩', '男', '13800000003', '广州市海珠区滨江路 66 号', '布洛芬不耐受', 260.00) ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  name = VALUES(name),
  gender = VALUES(gender),
  phone = VALUES(phone),
  address = VALUES(address),
  allergy_history = VALUES(allergy_history),
  balance = VALUES(balance);

INSERT INTO doctor_profile (id, user_id, name, gender, title, department, specialty, introduction, review_status, rating, consultation_fee) VALUES
(1, 2, '陈洁', '女', '主治医师', '口腔综合科', '龋齿修复、牙髓炎、牙周基础治疗', '从事口腔临床工作 8 年，擅长常见口腔疾病诊疗与患者健康宣教。', 'APPROVED', 4.8, 28.00),
(2, 4, '王启航', '男', '副主任医师', '牙周科', '牙龈炎、牙周炎、洁治与牙周维护', '长期负责牙周基础治疗与复诊维护，重视患者刷牙和牙线习惯管理。', 'APPROVED', 4.7, 35.00),
(3, 5, '赵宁', '女', '医师', '儿童口腔科', '儿童龋齿、窝沟封闭、涂氟', '关注儿童就诊体验，擅长儿童口腔预防和早期龋齿处理。', 'PENDING', 5.0, 22.00),
(4, 8, '孙雅', '女', '主治医师', '正畸科', '青少年早期矫治、隐形矫正咨询、牙列不齐评估', '擅长正畸初筛和矫治方案沟通，重视美观与咬合功能平衡。', 'APPROVED', 4.9, 40.00),
(5, 9, '林周', '男', '副主任医师', '修复种植科', '牙体缺损修复、嵌体修复、种植修复评估', '长期从事口腔修复与种植修复评估，擅长复杂缺牙修复设计。', 'APPROVED', 4.6, 45.00) ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  name = VALUES(name),
  gender = VALUES(gender),
  title = VALUES(title),
  department = VALUES(department),
  specialty = VALUES(specialty),
  introduction = VALUES(introduction),
  review_status = VALUES(review_status),
  rating = VALUES(rating),
  consultation_fee = VALUES(consultation_fee);

INSERT INTO doctor_qualification (id, doctor_id, certificate_type, file_path, status, review_comment, submitted_at, reviewed_at) VALUES
(1, 1, '执业医师资格证', '/uploads/demo-certificate.jpg', 'APPROVED', '资质完整，允许开展在线诊疗。', NOW(), NOW()),
(2, 2, '副主任医师职称证明', '/uploads/demo-periodontal-certificate.jpg', 'APPROVED', '资质完整，允许开展在线诊疗。', NOW(), NOW()),
(3, 3, '执业医师资格证', '/uploads/demo-child-certificate.jpg', 'PENDING', '待管理员审核。', NOW(), NULL),
(4, 4, '主治医师职称证明', '/uploads/demo-orthodontics-certificate.jpg', 'APPROVED', '资质完整，允许开展在线诊疗。', NOW(), NOW()),
(5, 5, '副主任医师职称证明', '/uploads/demo-implant-certificate.jpg', 'APPROVED', '资质完整，允许开展在线诊疗。', NOW(), NOW()) ON DUPLICATE KEY UPDATE
  doctor_id = VALUES(doctor_id),
  certificate_type = VALUES(certificate_type),
  file_path = VALUES(file_path),
  status = VALUES(status),
  review_comment = VALUES(review_comment),
  submitted_at = VALUES(submitted_at),
  reviewed_at = VALUES(reviewed_at);

INSERT INTO doctor_schedule (id, doctor_id, work_date, start_time, end_time, capacity, booked_count) VALUES
(1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '12:00:00', 8, 1),
(2, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', '18:00:00', 8, 0),
(3, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '18:00:00', 6, 1),
(4, 2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', '12:00:00', 6, 0),
(5, 3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '09:00:00', '12:00:00', 4, 0),
(6, 4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', '12:00:00', 5, 0),
(7, 4, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '14:00:00', '18:00:00', 5, 0),
(8, 5, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', '18:00:00', 6, 0),
(9, 5, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '09:00:00', '12:00:00', 6, 0) ON DUPLICATE KEY UPDATE
  doctor_id = VALUES(doctor_id),
  work_date = VALUES(work_date),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  capacity = VALUES(capacity),
  booked_count = VALUES(booked_count);

INSERT INTO announcement (id, title, category, content, status, publish_at, created_at) VALUES
(1, '五一节后门诊时间安排', '节假日安排', '节后门诊恢复正常，上午 09:00-12:00，下午 14:00-18:00。', 'PUBLISHED', NOW(), NOW()),
(2, '儿童涂氟优惠活动', '优惠活动', '本月儿童涂氟项目享受体验价，建议提前在线预约。', 'PUBLISHED', NOW(), NOW()),
(3, '牙龈出血的常见原因', '健康科普', '牙龈出血可能与牙结石、牙龈炎、刷牙方式不当有关，建议定期洁牙并由医生评估。', 'PUBLISHED', NOW(), NOW()),
(4, '本周三下午设备维护', '停诊信息', '因影像设备维护，本周三下午暂停拍片服务，普通门诊照常开放。', 'PUBLISHED', NOW(), NOW()),
(5, '暑期正畸咨询预约开放', '优惠活动', '暑期正畸咨询号源已开放，学生患者可提前预约初筛。', 'SCHEDULED', DATE_ADD(NOW(), INTERVAL 2 DAY), NOW()) ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  category = VALUES(category),
  content = VALUES(content),
  status = VALUES(status),
  publish_at = VALUES(publish_at),
  created_at = VALUES(created_at);

INSERT INTO medicine (id, name, effect, usage_instruction, price, stock, on_shelf) VALUES
(1, '复方氯己定含漱液', '辅助缓解牙龈炎、口腔异味', '每日 2 次，每次含漱 30 秒，遵医嘱使用。', 28.00, 120, 1),
(2, '布洛芬缓释胶囊', '缓解轻中度疼痛', '饭后服用，孕妇、胃溃疡患者需先咨询医生。', 18.50, 80, 1),
(3, '脱敏牙膏', '改善牙本质敏感', '每日刷牙使用，坚持 2-4 周观察效果。', 39.90, 60, 1),
(4, '医用牙线棒', '辅助清洁牙缝', '饭后使用，避免用力损伤牙龈。', 16.80, 200, 1),
(5, '儿童含氟牙膏', '儿童龋齿预防辅助', '黄豆大小用量，儿童需在家长指导下使用。', 32.00, 90, 1),
(6, '口腔溃疡凝胶', '缓解口腔黏膜不适', '局部薄涂，每日 2-3 次，连续不适应就诊。', 24.50, 75, 1),
(7, '正畸保护蜡', '缓解托槽或弓丝摩擦造成的不适', '取少量覆盖摩擦位置，进食前取下。', 19.90, 100, 1),
(8, '儿童软毛牙刷', '儿童日常口腔清洁', '建议 3 个月更换一次，家长协助刷牙。', 22.00, 110, 1),
(9, '含氟漱口水', '辅助预防龋齿和维护口腔清洁', '每日 1 次，儿童需在家长指导下使用。', 35.00, 70, 1),
(10, '种植牙护理刷', '种植修复后辅助清洁', '按医生指导清洁种植体周围区域。', 48.00, 45, 1) ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  effect = VALUES(effect),
  usage_instruction = VALUES(usage_instruction),
  price = VALUES(price),
  stock = VALUES(stock),
  on_shelf = VALUES(on_shelf);

INSERT INTO symptom_rule (id, keywords, possible_cause, recommended_department, recommended_medicine_ids, advice) VALUES
(1, '牙痛,疼,痛,冷热刺激', '龋齿、牙髓炎或牙本质敏感', '口腔综合科', '2,3', '避免冷热刺激，保持口腔清洁，疼痛持续或夜间痛明显时尽快预约医生。'),
(2, '牙龈出血,出血,牙龈肿', '牙龈炎、牙结石或刷牙方式不当', '牙周科', '1', '使用软毛牙刷，避免用力横刷，建议进行牙周检查和洁治评估。'),
(3, '口臭,异味', '口腔卫生不佳、牙周炎或龋齿', '口腔综合科', '1', '加强舌苔和牙缝清洁，如长期口臭建议就诊排查。'),
(4, '儿童,蛀牙,黑点,乳牙', '儿童龋齿或窝沟清洁不足', '儿童口腔科', '5', '建议家长帮助儿童刷牙，减少高频甜食摄入，并预约儿童口腔检查。'),
(5, '溃疡,口腔破,黏膜疼', '复发性口腔溃疡、局部刺激或咬伤', '口腔综合科', '6', '避免辛辣刺激，观察 7-10 天；若反复发作或面积较大请就医。'),
(6, '牙齿不齐,龅牙,地包天,矫正,正畸', '牙列不齐、咬合异常或青少年早期错颌问题', '正畸科', '7,9', '建议预约正畸初筛，携带既往影像资料；托槽摩擦不适可临时使用正畸保护蜡。'),
(7, '缺牙,种植,牙冠,嵌体,修复', '牙体缺损、缺牙修复需求或种植修复维护问题', '修复种植科', '10,9', '建议先进行口腔检查和影像评估，修复后应按医生指导清洁种植体和牙冠周围。') ON DUPLICATE KEY UPDATE
  keywords = VALUES(keywords),
  possible_cause = VALUES(possible_cause),
  recommended_department = VALUES(recommended_department),
  recommended_medicine_ids = VALUES(recommended_medicine_ids),
  advice = VALUES(advice);

INSERT INTO ai_config (id, provider_name, base_url, api_key, model, temperature, max_tokens, enabled, remark, updated_at) VALUES
(1, 'OpenAI兼容接口', 'https://api.openai.com/v1', '', 'gpt-4o-mini', 0.2, 900, 1, '请只在本地数据库填写真实api_key，不要提交到远程仓库。', NOW()) ON DUPLICATE KEY UPDATE
  id = id;

INSERT INTO doctor_review (id, patient_id, doctor_id, appointment_id, rating, comment, created_at) VALUES
(1, 1, 1, 3, 5, '医生解释很耐心，预约流程也方便。', NOW()),
(2, 2, 2, 2, 5, '洁牙前后都讲得很清楚，体验不错。', NOW()),
(3, 3, 1, 3, 4, '线上预约很顺畅，候诊时间短。', NOW()) ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
  appointment_id = VALUES(appointment_id),
  rating = VALUES(rating),
  comment = VALUES(comment),
  created_at = VALUES(created_at);

INSERT INTO medication_reminder (id, patient_id, medicine_id, medicine_name, start_date, total_quantity, daily_dose, expected_run_out_date, warned) VALUES
(1, 1, 1, '复方氯己定含漱液', CURDATE(), 6, 2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 0),
(2, 2, 3, '脱敏牙膏', DATE_SUB(CURDATE(), INTERVAL 10 DAY), 28, 2, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 0),
(3, 3, 6, '口腔溃疡凝胶', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 9, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 0) ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  medicine_id = VALUES(medicine_id),
  medicine_name = VALUES(medicine_name),
  start_date = VALUES(start_date),
  total_quantity = VALUES(total_quantity),
  daily_dose = VALUES(daily_dose),
  expected_run_out_date = VALUES(expected_run_out_date),
  warned = VALUES(warned);

INSERT INTO appointment (id, patient_id, doctor_id, schedule_id, visit_date, time_slot, symptoms, demand, fee_amount, fee_refunded, status, status_reason, created_at) VALUES
(1, 1, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '右下后牙冷热刺激痛', '希望检查是否龋齿', 28.00, 0, 'SUBMITTED', NULL, NOW()),
(2, 2, 2, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:30:00', '刷牙时牙龈出血', '想做牙周检查', 35.00, 0, 'CONFIRMED', '已确认，请准时到诊。', NOW()),
(3, 3, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:00:00', '牙齿敏感', '检查并咨询用药', 28.00, 0, 'COMPLETED', '已完成接诊。', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 1, 2, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '15:00:00', '口臭和牙龈肿胀', '希望洁牙评估', 35.00, 0, 'NO_SHOW', '患者未到诊。', DATE_SUB(NOW(), INTERVAL 3 DAY)) ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
  schedule_id = VALUES(schedule_id),
  visit_date = VALUES(visit_date),
  time_slot = VALUES(time_slot),
  symptoms = VALUES(symptoms),
  demand = VALUES(demand),
  fee_amount = VALUES(fee_amount),
  fee_refunded = VALUES(fee_refunded),
  status = VALUES(status),
  status_reason = VALUES(status_reason),
  created_at = VALUES(created_at);

INSERT INTO medical_record (id, patient_id, doctor_id, appointment_id, chief_complaint, present_illness, examination, diagnosis, treatment_plan, report_image_path, created_at) VALUES
(1, 3, 1, 3, '牙齿敏感 2 周', '进食冷饮时短暂酸痛，无夜间痛。', '牙颈部轻度楔状缺损，叩诊阴性。', '牙本质敏感', '建议使用脱敏牙膏，避免横向用力刷牙，2 周后复查。', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 2, 2, 2, '刷牙出血 1 月', '近期牙龈红肿，刷牙偶有出血。', '龈缘红肿，可见软垢和牙石。', '慢性牙龈炎', '建议洁治，复方氯己定含漱液短期辅助，改善口腔清洁习惯。', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)) ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
  appointment_id = VALUES(appointment_id),
  chief_complaint = VALUES(chief_complaint),
  present_illness = VALUES(present_illness),
  examination = VALUES(examination),
  diagnosis = VALUES(diagnosis),
  treatment_plan = VALUES(treatment_plan),
  report_image_path = VALUES(report_image_path),
  created_at = VALUES(created_at);

INSERT INTO prescription (id, record_id, patient_id, doctor_id, note, created_at) VALUES
(1, 1, 3, 1, '脱敏护理，观察症状变化。', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 2, 2, 2, '牙周基础治疗后短期辅助含漱。', DATE_SUB(NOW(), INTERVAL 1 DAY)) ON DUPLICATE KEY UPDATE
  record_id = VALUES(record_id),
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
  note = VALUES(note),
  created_at = VALUES(created_at);

INSERT INTO prescription_item (id, prescription_id, medicine_id, medicine_name, frequency, dosage, days) VALUES
(1, 1, 3, '脱敏牙膏', '每日2次', '常规刷牙用量', 14),
(2, 2, 1, '复方氯己定含漱液', '每日2次', '每次含漱30秒', 3) ON DUPLICATE KEY UPDATE
  prescription_id = VALUES(prescription_id),
  medicine_id = VALUES(medicine_id),
  medicine_name = VALUES(medicine_name),
  frequency = VALUES(frequency),
  dosage = VALUES(dosage),
  days = VALUES(days);

INSERT INTO medicine_order (id, order_no, patient_id, total_amount, delivery_method, status, created_at) VALUES
(1, 'DCO202605030001', 1, 46.50, '到店自取', 'PAID', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'DCO202605030002', 2, 67.90, '同城配送', 'SHIPPED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'DCO202605030003', 3, 24.50, '到店自取', 'REFUND_REQUESTED', NOW()) ON DUPLICATE KEY UPDATE
  order_no = VALUES(order_no),
  patient_id = VALUES(patient_id),
  total_amount = VALUES(total_amount),
  delivery_method = VALUES(delivery_method),
  status = VALUES(status),
  created_at = VALUES(created_at);

INSERT INTO order_item (id, order_id, medicine_id, medicine_name, unit_price, quantity) VALUES
(1, 1, 1, '复方氯己定含漱液', 28.00, 1),
(2, 1, 2, '布洛芬缓释胶囊', 18.50, 1),
(3, 2, 3, '脱敏牙膏', 39.90, 1),
(4, 2, 1, '复方氯己定含漱液', 28.00, 1),
(5, 3, 6, '口腔溃疡凝胶', 24.50, 1) ON DUPLICATE KEY UPDATE
  order_id = VALUES(order_id),
  medicine_id = VALUES(medicine_id),
  medicine_name = VALUES(medicine_name),
  unit_price = VALUES(unit_price),
  quantity = VALUES(quantity);

INSERT INTO message (id, patient_id, doctor_id, question, reply, created_at, replied_at) VALUES
(1, 1, 1, '补牙后多久可以正常吃东西？', '一般树脂补牙后即可进食，建议当天避免咬过硬食物。', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 2, 2, '洁牙后牙缝变大正常吗？', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL) ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
  question = VALUES(question),
  reply = VALUES(reply),
  created_at = VALUES(created_at),
  replied_at = VALUES(replied_at);

INSERT INTO operation_log (id, operator_id, operator_role, action, detail, created_at) VALUES
(1, 1, 'ADMIN', 'SEED_DATA', '初始化演示数据', NOW()),
(2, 3, 'PATIENT', 'CREATE_APPOINTMENT', '患者李明提交预约', NOW()),
(3, 2, 'DOCTOR', 'SAVE_RECORD', '医生陈洁保存病例', NOW()) ON DUPLICATE KEY UPDATE
  operator_id = VALUES(operator_id),
  operator_role = VALUES(operator_role),
  action = VALUES(action),
  detail = VALUES(detail),
  created_at = VALUES(created_at);
