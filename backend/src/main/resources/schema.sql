CREATE TABLE IF NOT EXISTS user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  last_login_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS patient_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  gender VARCHAR(20) NULL,
  phone VARCHAR(32) NULL,
  address VARCHAR(255) NULL,
  allergy_history VARCHAR(500) NULL
);

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
);

CREATE TABLE IF NOT EXISTS doctor_qualification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  certificate_type VARCHAR(64) NOT NULL,
  file_path VARCHAR(255) NULL,
  status VARCHAR(20) NOT NULL,
  review_comment VARCHAR(255) NULL,
  submitted_at DATETIME NOT NULL,
  reviewed_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS doctor_schedule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  work_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  capacity INT NOT NULL DEFAULT 8,
  booked_count INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(120) NOT NULL,
  category VARCHAR(64) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  publish_at DATETIME NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS medicine (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  effect VARCHAR(255) NULL,
  usage_instruction TEXT NULL,
  price DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL,
  on_shelf TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  medicine_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  UNIQUE KEY uk_cart_patient_medicine (patient_id, medicine_id)
);

CREATE TABLE IF NOT EXISTS medicine_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  patient_id BIGINT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  delivery_method VARCHAR(64) NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  medicine_id BIGINT NOT NULL,
  medicine_name VARCHAR(100) NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL
);

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
);

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
);

CREATE TABLE IF NOT EXISTS prescription (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_id BIGINT NULL,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  note VARCHAR(500) NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS prescription_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  prescription_id BIGINT NOT NULL,
  medicine_id BIGINT NOT NULL,
  medicine_name VARCHAR(100) NOT NULL,
  frequency VARCHAR(64) NULL,
  dosage VARCHAR(64) NULL,
  days INT NULL
);

CREATE TABLE IF NOT EXISTS message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  question TEXT NOT NULL,
  reply TEXT NULL,
  created_at DATETIME NOT NULL,
  replied_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS doctor_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comment VARCHAR(500) NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS symptom_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  keywords VARCHAR(255) NOT NULL,
  possible_cause VARCHAR(255) NOT NULL,
  recommended_department VARCHAR(64) NOT NULL,
  recommended_medicine_ids VARCHAR(255) NULL,
  advice TEXT NOT NULL
);

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
);

CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT NULL,
  operator_role VARCHAR(20) NULL,
  action VARCHAR(100) NOT NULL,
  detail VARCHAR(500) NULL,
  created_at DATETIME NOT NULL
);
