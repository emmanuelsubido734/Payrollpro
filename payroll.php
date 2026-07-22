<?php
header("Content-Type: application/json");
require_once 'db_connect.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $required = ['employeeId','hoursWorked','overtimeHours','grossPay','tax','deductions','netPay','date'];
    foreach ($required as $field) {
        if (!isset($data[$field])) {
            http_response_code(400);
            echo json_encode(["error" => "Missing field: $field"]);
            exit;
        }
    }

    $stmt = $pdo->prepare(
        "INSERT INTO Payroll (employee_id, hours_worked, overtime_hours, gross_pay, tax, deductions, net_pay, payroll_date)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    );
    try {
        $stmt->execute([
            $data['employeeId'], $data['hoursWorked'], $data['overtimeHours'],
            $data['grossPay'], $data['tax'], $data['deductions'],
            $data['netPay'], $data['date']
        ]);
        http_response_code(201);
        echo json_encode(["message" => "Payroll record saved", "payrollId" => $pdo->lastInsertId()]);
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(["error" => "Could not save payroll: " . $e->getMessage()]);
    }
} elseif ($method === 'GET') {
    if (isset($_GET['employee_id'])) {
        $stmt = $pdo->prepare("SELECT * FROM Payroll WHERE employee_id = ? ORDER BY payroll_date DESC");
        $stmt->execute([$_GET['employee_id']]);
    } else {
        $stmt = $pdo->query("SELECT * FROM Payroll ORDER BY payroll_date DESC");
    }
    echo json_encode(array_map('formatPayroll', $stmt->fetchAll(PDO::FETCH_ASSOC)));
} else {
    http_response_code(405);
    echo json_encode(["error" => "Method not allowed"]);
}

function formatPayroll($row) {
    return [
        "employeeId"    => $row['employee_id'],
        "hoursWorked"   => (float) $row['hours_worked'],
        "overtimeHours" => (float) $row['overtime_hours'],
        "grossPay"      => (float) $row['gross_pay'],
        "tax"           => (float) $row['tax'],
        "deductions"    => (float) $row['deductions'],
        "netPay"        => (float) $row['net_pay'],
        "date"          => $row['payroll_date']
    ];
}