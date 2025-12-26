INSERT INTO users (id, name, email, phone, role, status, password_hash, is_deleted, created_at, updated_at) VALUES
  (1, 'Admin A', 'admin1@example.com', '13000000001', 'ADMIN', 'ACTIVE', '{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG', 0, NOW(), NOW()),
  (2, 'Teacher Li', 'teacher1@example.com', '13000000002', 'TEACHER', 'ACTIVE', '{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG', 0, NOW(), NOW()),
  (3, 'Teacher Wang', 'teacher2@example.com', '13000000003', 'TEACHER', 'ACTIVE', '{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG', 0, NOW(), NOW()),
  (4, 'Student Chen', 'student1@example.com', '13000000004', 'STUDENT', 'ACTIVE', '{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG', 0, NOW(), NOW()),
  (5, 'Student Zhao', 'student2@example.com', '13000000005', 'STUDENT', 'ACTIVE', '{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG', 0, NOW(), NOW()),
  (6, 'Student Liu', 'student3@example.com', '13000000006', 'STUDENT', 'ACTIVE', '{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG', 0, NOW(), NOW()),
  (7, 'Teacher Sun', 'teacher3@example.com', '13000000007', 'TEACHER', 'ACTIVE', '{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG', 0, NOW(), NOW()),
  (8, 'Admin B', 'admin2@example.com', '13000000008', 'ADMIN', 'ACTIVE', '{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG', 0, NOW(), NOW());

INSERT INTO labs (id, name, location, capacity, open_time_start, open_time_end, status, is_deleted, created_at, updated_at) VALUES
  (1, 'Chem Lab A', 'Building 1-203', 40, '08:00:00', '22:00:00', 'IDLE', 0, NOW(), NOW()),
  (2, 'Physics Lab B', 'Building 2-105', 35, '08:00:00', '21:00:00', 'IDLE', 0, NOW(), NOW()),
  (3, 'Bio Lab C', 'Building 3-307', 30, '09:00:00', '20:00:00', 'MAINTENANCE', 0, NOW(), NOW()),
  (4, 'Comp Lab D', 'Building 4-101', 50, '08:30:00', '22:30:00', 'IDLE', 0, NOW(), NOW());

INSERT INTO devices (id, lab_id, name, model, status, is_deleted, created_at, updated_at) VALUES
  (1, 1, 'Spectrometer', 'SP-2025', 'IDLE', 0, NOW(), NOW()),
  (2, 1, 'Centrifuge', 'CF-900', 'IDLE', 0, NOW(), NOW()),
  (3, 2, 'Oscilloscope', 'OS-500', 'IDLE', 0, NOW(), NOW()),
  (4, 2, 'Signal Generator', 'SG-120', 'IDLE', 0, NOW(), NOW()),
  (5, 3, 'Microscope', 'MC-80', 'MAINTENANCE', 0, NOW(), NOW()),
  (6, 4, '3D Printer', 'PR-3D', 'IDLE', 0, NOW(), NOW()),
  (7, 4, 'GPU Workstation', 'WS-RTX', 'IDLE', 0, NOW(), NOW()),
  (8, 1, 'pH Meter', 'PH-10', 'IDLE', 0, NOW(), NOW());

INSERT INTO courses (id, name, class_name, student_count, term, created_by, is_deleted, created_at, updated_at) VALUES
  (1, 'Analytical Chemistry', 'Chem 2023-1', 45, '2024-2025-2', 2, 0, NOW(), NOW()),
  (2, 'Modern Physics', 'Phys 2023-2', 38, '2024-2025-2', 3, 0, NOW(), NOW()),
  (3, 'Molecular Biology', 'Bio 2023-1', 32, '2024-2025-2', 7, 0, NOW(), NOW()),
  (4, 'Data Structures', 'CS 2023-1', 48, '2024-2025-2', 7, 0, NOW(), NOW());

INSERT INTO reservations (id, requester_id, lab_id, device_id, course_id, start_time, end_time, status, type, priority,
                          approved_by, approved_at, cancel_reason, is_deleted, created_at, updated_at) VALUES
  (1, 2, 1, NULL, 1, '2025-01-15 08:30:00', '2025-01-15 10:30:00', 'APPROVED', 'COURSE', 'COURSE', 1, NOW(), NULL, 0, NOW(), NOW()),
  (2, 4, 1, 1, NULL, '2025-01-16 09:00:00', '2025-01-16 11:00:00', 'PENDING', 'SINGLE', 'NORMAL', NULL, NULL, NULL, 0, NOW(), NOW()),
  (3, 5, 2, 3, NULL, '2025-01-16 13:00:00', '2025-01-16 15:00:00', 'APPROVED', 'SINGLE', 'NORMAL', 1, NOW(), NULL, 0, NOW(), NOW()),
  (4, 6, 2, NULL, NULL, '2025-01-17 10:00:00', '2025-01-17 12:00:00', 'APPROVED', 'SINGLE', 'NORMAL', 8, NOW(), NULL, 0, NOW(), NOW()),
  (5, 3, 4, 6, NULL, '2025-01-18 14:00:00', '2025-01-18 16:00:00', 'PENDING', 'SINGLE', 'NORMAL', NULL, NULL, NULL, 0, NOW(), NOW()),
  (6, 7, 4, NULL, 4, '2025-01-19 09:30:00', '2025-01-19 11:30:00', 'PENDING', 'COURSE', 'COURSE', NULL, NULL, NULL, 0, NOW(), NOW()),
  (7, 4, 1, 2, NULL, '2025-01-20 08:30:00', '2025-01-20 09:30:00', 'REJECTED', 'SINGLE', 'NORMAL', 1, NOW(), NULL, 0, NOW(), NOW()),
  (8, 5, 1, NULL, NULL, '2025-01-20 10:00:00', '2025-01-20 12:00:00', 'CANCELLED', 'SINGLE', 'NORMAL', NULL, NULL, 'User cancel', 0, NOW(), NOW()),
  (9, 2, 2, 4, NULL, '2025-01-21 14:00:00', '2025-01-21 16:00:00', 'APPROVED', 'SINGLE', 'NORMAL', 8, NOW(), NULL, 0, NOW(), NOW()),
  (10, 3, 1, 8, NULL, '2025-01-22 08:00:00', '2025-01-22 09:00:00', 'APPROVED', 'SINGLE', 'NORMAL', 1, NOW(), NULL, 0, NOW(), NOW()),
  (11, 4, 4, 7, NULL, '2025-01-22 10:00:00', '2025-01-22 12:00:00', 'APPROVED', 'SINGLE', 'NORMAL', 8, NOW(), NULL, 0, NOW(), NOW()),
  (12, 5, 2, NULL, NULL, '2025-01-23 13:00:00', '2025-01-23 15:00:00', 'APPROVED', 'SINGLE', 'NORMAL', 1, NOW(), NULL, 0, NOW(), NOW());

INSERT INTO reservation_series (id, requester_id, rule_type, rule_value, mode, is_deleted, created_at, updated_at) VALUES
  (1, 2, 'WEEKLY', '{"daysOfWeek":[1,3,5],"count":6}', 'PARTIAL', 0, NOW(), NOW()),
  (2, 7, 'DAILY', '{"interval":2,"count":4}', 'STRICT', 0, NOW(), NOW());

INSERT INTO reservation_series_items (id, series_id, reservation_id, status, is_deleted, created_at, updated_at) VALUES
  (1, 1, 1, 'APPROVED', 0, NOW(), NOW()),
  (2, 1, 9, 'APPROVED', 0, NOW(), NOW()),
  (3, 1, 10, 'APPROVED', 0, NOW(), NOW()),
  (4, 2, 6, 'PENDING', 0, NOW(), NOW());

INSERT INTO approvals (id, reservation_id, action, reason, operator_id, is_deleted, created_at, updated_at) VALUES
  (1, 1, 'APPROVE', 'Course approved', 1, 0, NOW(), NOW()),
  (2, 3, 'APPROVE', 'Device available', 1, 0, NOW(), NOW()),
  (3, 7, 'REJECT', 'Schedule conflict', 1, 0, NOW(), NOW()),
  (4, 4, 'APPROVE', 'Lab available', 8, 0, NOW(), NOW()),
  (5, 9, 'APPROVE', 'Device available', 8, 0, NOW(), NOW());

INSERT INTO audit_logs (id, actor_id, action, target_type, target_id, detail, is_deleted, created_at, updated_at) VALUES
  (1, 2, 'RESERVATION_CREATE', 'RESERVATION', 1, '{"note":"course reservation"}', 0, NOW(), NOW()),
  (2, 1, 'RESERVATION_APPROVE', 'RESERVATION', 1, '{"reason":"course approved"}', 0, NOW(), NOW()),
  (3, 4, 'RESERVATION_CREATE', 'RESERVATION', 2, '{"note":"student device"}', 0, NOW(), NOW()),
  (4, 1, 'RESERVATION_REJECT', 'RESERVATION', 7, '{"reason":"conflict"}', 0, NOW(), NOW()),
  (5, 8, 'RESERVATION_APPROVE', 'RESERVATION', 4, '{"reason":"lab available"}', 0, NOW(), NOW()),
  (6, 7, 'RESERVATION_COURSE_CREATE', 'RESERVATION', 6, '{"note":"cs course"}', 0, NOW(), NOW()),
  (7, 3, 'RESERVATION_CREATE', 'RESERVATION', 5, '{"note":"teacher device"}', 0, NOW(), NOW());

INSERT INTO notifications (id, user_id, type, content, status, is_deleted, created_at, updated_at) VALUES
  (1, 2, 'RESERVATION', 'Reservation approved', 'READ', 0, NOW(), NOW()),
  (2, 4, 'RESERVATION', 'Reservation submitted', 'UNREAD', 0, NOW(), NOW()),
  (3, 5, 'RESERVATION', 'Reservation cancelled', 'READ', 0, NOW(), NOW()),
  (4, 6, 'RESERVATION', 'Reservation approved', 'UNREAD', 0, NOW(), NOW()),
  (5, 7, 'RESERVATION', 'Course reservation pending', 'UNREAD', 0, NOW(), NOW()),
  (6, 3, 'RESERVATION', 'Reservation approved', 'READ', 0, NOW(), NOW()),
  (7, 4, 'RESERVATION', 'Reservation rejected', 'READ', 0, NOW(), NOW());

INSERT INTO rule_configs (id, key_name, value, description, is_deleted, created_at, updated_at) VALUES
  (1, 'reservation_rules', '{"teacherCancelHours":12,"studentCancelHours":24,"maxDailyReservations":2}', 'Reservation rules', 0, NOW(), NOW()),
  (2, 'priority_rules', '{"coursePriority":true,"overridePolicy":"CANCEL_CONFLICTS"}', 'Priority and override policy', 0, NOW(), NOW());
