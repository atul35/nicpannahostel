<?php
require_once 'db.php';
require_once 'functions.php';

$pdo = Database::getConnection();

$action = $_GET['action'] ?? '';

try {
    switch ($action) {

        // 🧍 Add user
        case 'add_user':
            $data = json_decode(file_get_contents("php://input"), true);
            if (empty($data['full_name']) || empty($data['phone_number']) || empty($data['role'])) {
                respond('error', 'Missing required fields');
            }

            $stmt = $pdo->prepare("INSERT INTO users (full_name, email, phone_number, role) VALUES (?, ?, ?, ?)");
            $stmt->execute([$data['full_name'], $data['email'] ?? null, $data['phone_number'], $data['role']]);
            logMessage("User added: {$data['full_name']}");
            respond('success', 'User added successfully');

        // 👤 Get users
        case 'get_users':
            $stmt = $pdo->query("SELECT * FROM users ORDER BY created_on DESC");
            $users = $stmt->fetchAll(PDO::FETCH_ASSOC);
            respond('success', 'Users fetched successfully', $users);

            // 👤 Get user by phone number
        case 'get_user_by_phone':
            $phone = $_GET['phone_number'] ?? '';

            if (empty($phone)) {
                respond('error', 'phone_number is required');
            }

            $stmt = $pdo->prepare("SELECT u.user_id, u.full_name, u.email, u.phone_number, u.role, u.hostel_id, u.created_on, u.created_by, u.updated_on, u.updated_by, h.hostel_name, h.hostel_location AS hostel_location FROM users u LEFT JOIN hostel h ON u.hostel_id = h.hostel_id WHERE u.phone_number = ?");
            $stmt->execute([$phone]);
            $user = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($user) {
                respond('success', 'User fetched successfully', $user);
            } else {
                respond('error', 'User not found');
            }

        // 📝 Add complaint
        case 'add_complaint':
            $data = json_decode(file_get_contents("php://input"), true);
            if (empty($data['complaint_text']) || empty($data['user_id'])) {
                respond('error', 'Missing required fields');
            }

            $stmt = $pdo->prepare("INSERT INTO complaints (complaint_text, user_id) VALUES (?, ?)");
            $stmt->execute([$data['complaint_text'], $data['user_id']]);
            logMessage("Complaint added by user {$data['user_id']}");
            respond('success', 'Complaint added successfully');

        // 📋 Get complaints
        case 'get_complaints':
            $stmt = $pdo->query("SELECT c.complaint_id, c.complaint_text, u.full_name, u.phone_number, h.hostel_name, h.hostel_location , c.status, c.created_on FROM complaints c
                                 JOIN users u ON c.user_id = u.user_id
                                 JOIN hostel h on h.hostel_id = u.hostel_id
                                 ORDER BY c.created_on DESC");
            $complaints = $stmt->fetchAll(PDO::FETCH_ASSOC);
            respond('success', 'Complaints fetched successfully', $complaints);

            // 📋 Get complaints by user (no joins)
        case 'get_complaints_by_user':
        $userId = $_GET['user_id'] ?? '';
        if (empty($userId)) {
            respond('error', 'user_id is required');
        }
        $stmt = $pdo->prepare("SELECT complaint_id, complaint_text, user_id, created_on, status FROM complaints WHERE user_id = ? ORDER BY created_on DESC");
        $stmt->execute([$userId]);
        $complaints = $stmt->fetchAll(PDO::FETCH_ASSOC);
        respond('success', 'User complaints fetched successfully', $complaints);

        // 🕓 Add complaint log
        case 'add_complaint_log':
            $data = json_decode(file_get_contents("php://input"), true);
            if (empty($data['complaint_id']) || empty($data['action_taken_by']) || empty($data['status'])) {
                respond('error', 'Missing required fields');
            }

            $stmt = $pdo->prepare("INSERT INTO complaint_logs (complaint_id, action_by, status, remarks) VALUES (?, ?, ?, ?); 
            UPDATE complaints SET status = ? WHERE complaint_id = ?;");
            $stmt->execute([
                $data['complaint_id'],
                $data['action_taken_by'],
                $data['status'],
                $data['remarks'] ?? null,
                $data['status'],
                $data['complaint_id']
            ]);
            logMessage("Complaint log added for complaint ID {$data['complaint_id']}");
            respond('success', 'Complaint log added successfully');

        // 🧾 Get complaint logs by complain id
       case 'get_complaint_logs_by_complain':
            $complaintId = $_GET['complaint_id'] ?? '';
            if (empty($complaintId)) {
                respond('error', 'complaint_id is required');
            }
            $stmt = $pdo->prepare("
                SELECT l.status, l.remarks, l.action_on, u.full_name AS action_by
                FROM complaint_logs l
                JOIN users u ON l.action_by = u.user_id
                WHERE l.complaint_id = ?
                ORDER BY l.action_on DESC
            ");
            $stmt->execute([$complaintId]);
            $logs = $stmt->fetchAll(PDO::FETCH_ASSOC);
            respond('success', 'Complaint logs fetched successfully', $logs);

        // ❌ Invalid action
        default:
            logMessage("Error: Invalid API action {$action}");
            respond('error', 'Invalid API action');
    }

} catch (Exception $e) {
    logMessage("Exception: " . $e->getMessage());
    respond('error', 'Server error occurred');
}
?>
