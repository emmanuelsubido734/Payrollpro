<?php
header("Content-Type: application/json");
require_once 'db_connect.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    if (isset($_GET['id'])) {
        $stmt = $pdo->prepare("SELECT * FROM Employees WHERE employee_id = ?");
        $stmt->execute([$_GET['id']]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        if (!$row) {
            http_response_code(404);
            echo json_encode(["error" => "Employee not found"]);
            exit;
        }
        echo json_encode(formatEmployee($row));
    } else {
        $stmt = $pdo->query("SELECT * FROM Employees WHERE is_active = 1 ORDER BY employee_id");
        echo json_encode(array_map('formatEmployee', $stmt->fetchAll(PDO::FETCH_ASSOC)));
    }
} elseif ($method === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    if (!isset($data['employeeId'], $data['firstName'], $data['lastName'], $data['hourlyRate'])) {
        http_response_code(400);
        echo json_encode(["error" => "Missing required fields"]);
        exit;
    }

    $stmt = $pdo->prepare(
        "INSERT INTO Employees (employee_id, first_name, last_name, position, hourly_rate, civil_status)
         VALUES (?, ?, ?, ?, ?, ?)"
    );
    try {
        $stmt->execute([
            $data['employeeId'],
            $data['firstName'],
            $data['lastName'],
            $data['position'] ?? null,
            $data['hourlyRate'],
            $data['civilStatus'] ?? 'single'
        ]);
        http_response_code(201);
        echo json_encode(["message" => "Employee added"]);
    } catch (PDOException $e) {
        http_response_code(409);
        echo json_encode(["error" => "Could not add employee: " . $e->getMessage()]);
    }
} elseif ($method === 'DELETE') {
    parse_str(file_get_contents('php://input'), $deleteData);
    $employeeId = $_GET['id'] ?? $deleteData['id'] ?? null;

    if (!$employeeId) {
        http_response_code(400);
        echo json_encode(["error" => "Missing employee id"]);
        exit;
    }

    $stmt = $pdo->prepare("UPDATE Employees SET is_active = 0 WHERE employee_id = ?");
    $stmt->execute([$employeeId]);

    if ($stmt->rowCount() === 0) {
        http_response_code(404);
        echo json_encode(["error" => "Employee not found"]);
    } else {
        echo json_encode(["message" => "Employee deactivated"]);
    }
} else {
    http_response_code(405);
    echo json_encode(["error" => "Method not allowed"]);
}

function formatEmployee($row) {
    return [
        "employeeId"  => $row['employee_id'],
        "firstName"   => $row['first_name'],
        "lastName"    => $row['last_name'],
        "position"    => $row['position'],
        "hourlyRate"  => (float) $row['hourly_rate'],
        "civilStatus" => $row['civil_status']
    ];
}