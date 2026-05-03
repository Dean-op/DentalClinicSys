CREATE TABLE IF NOT EXISTS user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  last_login_at DATETIME NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统账号表，存储患者、医生、管理员的登录账号、角色和状态';

CREATE TABLE IF NOT EXISTS patient_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  gender VARCHAR(20) NULL,
  phone VARCHAR(32) NULL,
  address VARCHAR(255) NULL,
  allergy_history VARCHAR(500) NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者基本信息表，存储姓名、性别、联系方式、地址和过敏史';

CREATE TABLE IF NOT EXISTS doctor_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  title VARCHAR(64) NULL,
  department VARCHAR(64) NULL,
  specialty VARCHAR(255) NULL,
  introduction TEXT NULL,
  review_status VARCHAR(20) NOT NULL,
  rating DOUBLE DEFAULT 5
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生个人资料表，存储职称、科室、擅长方向、简介、审核状态和评分';

CREATE TABLE IF NOT EXISTS doctor_qualification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  certificate_type VARCHAR(64) NOT NULL,
  file_path VARCHAR(255) NULL,
  status VARCHAR(20) NOT NULL,
  review_comment VARCHAR(255) NULL,
  submitted_at DATETIME NOT NULL,
  reviewed_at DATETIME NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生资质审核表，存储执业资格证、职称证明等材料及审核结果';

CREATE TABLE IF NOT EXISTS doctor_schedule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  work_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  capacity INT NOT NULL DEFAULT 8,
  booked_count INT NOT NULL DEFAULT 0
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生出诊排班表，存储医生可预约日期、时间段、容量和已预约数量';

CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(120) NOT NULL,
  category VARCHAR(64) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  publish_at DATETIME NULL,
  created_at DATETIME NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='诊所公告表，存储停诊信息、优惠活动、健康科普和节假日安排';

CREATE TABLE IF NOT EXISTS medicine (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  effect VARCHAR(255) NULL,
  usage_instruction TEXT NULL,
  price DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL,
  on_shelf TINYINT(1) NOT NULL DEFAULT 1
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品信息表，存储药品名称、功效、用法说明、价格、库存和上下架状态';

CREATE TABLE IF NOT EXISTS cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  medicine_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  UNIQUE KEY uk_cart_patient_medicine (patient_id, medicine_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车明细表，存储患者临时加入购物车的药品和数量';

CREATE TABLE IF NOT EXISTS medicine_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  patient_id BIGINT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  delivery_method VARCHAR(64) NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at DATETIME NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品订单主表，存储订单编号、患者、总金额、配送方式和订单状态';

CREATE TABLE IF NOT EXISTS order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  medicine_id BIGINT NOT NULL,
  medicine_name VARCHAR(100) NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品订单明细表，存储订单中的药品、单价和购买数量';

CREATE TABLE IF NOT EXISTS appointment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  visit_date DATE NOT NULL,
  time_slot TIME NOT NULL,
  symptoms VARCHAR(500) NULL,
  demand VARCHAR(500) NULL,
  status VARCHAR(30) NOT NULL,
  status_reason VARCHAR(255) NULL,
  created_at DATETIME NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表，存储患者预约医生的日期、时间段、症状需求和处理状态';

CREATE TABLE IF NOT EXISTS medical_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  appointment_id BIGINT NULL,
  chief_complaint VARCHAR(500) NULL,
  present_illness TEXT NULL,
  examination TEXT NULL,
  diagnosis TEXT NULL,
  treatment_plan TEXT NULL,
  report_image_path VARCHAR(255) NULL,
  created_at DATETIME NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病例记录表，存储医生为患者记录的主诉、病史、检查、诊断和治疗方案';

CREATE TABLE IF NOT EXISTS prescription (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_id BIGINT NULL,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  note VARCHAR(500) NULL,
  created_at DATETIME NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子处方主表，存储医生为患者开具的处方及医嘱';

CREATE TABLE IF NOT EXISTS prescription_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  prescription_id BIGINT NOT NULL,
  medicine_id BIGINT NOT NULL,
  medicine_name VARCHAR(100) NOT NULL,
  frequency VARCHAR(64) NULL,
  dosage VARCHAR(64) NULL,
  days INT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子处方明细表，存储处方中的药品、用药频次、剂量和天数';

CREATE TABLE IF NOT EXISTS message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  question TEXT NOT NULL,
  reply TEXT NULL,
  created_at DATETIME NOT NULL,
  replied_at DATETIME NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医患留言表，存储患者留言、医生回复和回复时间';

CREATE TABLE IF NOT EXISTS doctor_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comment VARCHAR(500) NULL,
  created_at DATETIME NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生评价表，存储患者对医生的评分和评价内容';

CREATE TABLE IF NOT EXISTS symptom_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  keywords VARCHAR(255) NOT NULL,
  possible_cause VARCHAR(255) NOT NULL,
  recommended_department VARCHAR(64) NOT NULL,
  recommended_medicine_ids VARCHAR(255) NULL,
  advice TEXT NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='症状匹配规则表，存储AI牙医和症状推荐使用的关键词、原因、科室和建议';

CREATE TABLE IF NOT EXISTS ai_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  provider_name VARCHAR(64) NOT NULL,
  base_url VARCHAR(255) NOT NULL,
  api_key VARCHAR(500) NOT NULL,
  model VARCHAR(120) NOT NULL,
  temperature DOUBLE NOT NULL DEFAULT 0.2,
  max_tokens INT NOT NULL DEFAULT 900,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  updated_at DATETIME NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI接口配置表，存储OpenAI兼容接口地址、密钥、模型和启用状态';

CREATE TABLE IF NOT EXISTS medication_reminder (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  medicine_id BIGINT NOT NULL,
  medicine_name VARCHAR(100) NOT NULL,
  start_date DATE NOT NULL,
  total_quantity INT NOT NULL,
  daily_dose INT NOT NULL,
  expected_run_out_date DATE NOT NULL,
  warned TINYINT(1) NOT NULL DEFAULT 0
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用药提醒表，存储患者药品用量、预计用完日期和预警状态';

CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT NULL,
  operator_role VARCHAR(20) NULL,
  action VARCHAR(100) NOT NULL,
  detail VARCHAR(500) NULL,
  created_at DATETIME NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表，记录登录、审核、下单、预约、病例等关键操作';

ALTER TABLE user_account CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE patient_profile CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE doctor_profile CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE doctor_qualification CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE doctor_schedule CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE announcement CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE medicine CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE cart_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE medicine_order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE order_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE appointment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE medical_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE prescription CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE prescription_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE message CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE doctor_review CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE symptom_rule CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE ai_config CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE medication_reminder CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE operation_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
