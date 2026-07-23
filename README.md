# PayrollPro

PayrollPro is an Android payroll management system built with Kotlin and Jetpack Compose. It manages employee records and computes payroll (gross pay, tax, deductions, and net pay) against a MySQL database running on XAMPP, using a mixed REST + SOAP backend.

Built as an IT130 Machine Problem.

## Features

- **Employee management** — add, view, and remove employees, with full details (ID, name, position, hourly rate, civil status)
- **Payroll computation** — enter hours worked and overtime for an employee, get gross pay, tax, deductions, and net pay computed by the server
- **Payroll history** — browse past payroll runs, with per-record detail view
- **Configurable settings** — SOAP endpoint, REST API base URL, overtime multiplier, and deduction defaults (SSS, PhilHealth, Pag-IBIG, other), all persisted on-device
- **Dark mode**, persisted across app restarts
- **Local login screen** (see [Login](#login) below for what this is and isn't)

## Tech stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose (Material 3) |
| State / persistence | ViewModel + Jetpack DataStore |
| REST networking | Retrofit + Gson |
| SOAP networking | ksoap2-android |
| Backend (REST + SOAP dispatch) | PHP |
| SOAP server | NuSOAP |
| Database | MySQL (via XAMPP) |

## Architecture

The app talks to **two separate backend channels** against the same MySQL database:

1. **REST (Retrofit)** — `employees.php` and `payroll.php` handle employee CRUD and payroll history (GET/POST), returning/accepting JSON.
2. **SOAP (ksoap2 ↔ NuSOAP)** — `soap_server.php` exposes the actual payroll math as four SOAP operations, called in sequence whenever payroll is computed.

```
Android App (Kotlin / Compose)
   ├── Retrofit ──► employees.php / payroll.php ──► MySQL (Employees, Payroll tables)
   └── ksoap2   ──► soap_server.php (NuSOAP)     ──► payroll_functions.php (pure calculation logic)
```

Keeping the calculation logic (`payroll_functions.php`) separate from the SOAP wiring (`soap_server.php`) means the math can be tested independently of the SOAP transport.

### Why two protocols?

This was a deliberate design choice for the class project: REST handles simple data CRUD (employees, history), while SOAP demonstrates a structured, contract-based web service (WSDL, typed operations) for the actual payroll computation — the part of the system with real business logic worth exposing as a formal service.

## Payroll calculations

All calculations happen server-side, in `payroll_functions.php`, and are called through `soap_server.php`'s four operations in this order:

### 1. Gross pay — `ComputeGrossPay`

```
regularPay  = hourlyRate × hoursWorked
overtimePay = hourlyRate × overtimeMultiplier × overtimeHours
grossPay    = regularPay + overtimePay
```

`overtimeMultiplier` defaults to 1.25 and is configurable in Settings.

### 2. Tax — `ComputeTax`

A progressive, bracket-based withholding calculation:

```
exemption      = 5000 if civilStatus == "married" else 3000
taxableIncome  = max(0, grossPay - exemption)
tax            = bracket base + (taxableIncome - bracket floor) × bracket rate
```

Brackets (floor → rate):

| Taxable income up to | Rate | Base |
|---|---|---|
| 20,833 | 0% | 0 |
| 33,333 | 15% | 0 |
| 66,667 | 20% | 1,875 |
| 166,667 | 25% | 8,541.80 |
| 666,667 | 30% | 33,541.80 |
| above | 35% | 183,541.80 |

> **Note:** these brackets and exemption amounts are simplified sample figures for this class project — **not** the current official BIR withholding table. Don't use this for real payroll without replacing them with accurate, up-to-date figures.

### 3. Deductions — `ComputeDeductions`

```
deductions = sss + philHealth + pagIbig + otherDeductions
```

All four values are configurable in Settings (defaults: SSS ₱500, PhilHealth ₱250, Pag-IBIG ₱100, other ₱0).

### 4. Net salary — `ComputeNetSalary`

```
netPay = grossPay - tax - deductions
```

The final `PayrollResult` (hours worked, overtime, gross pay, tax, deductions, net pay, date) is shown on the payslip screen, then optionally confirmed and synced to the `Payroll` table via REST.

## Database schema

```sql
CREATE TABLE Employees (
    employee_id  VARCHAR(10) PRIMARY KEY,
    first_name   VARCHAR(50) NOT NULL,
    last_name    VARCHAR(50) NOT NULL,
    position     VARCHAR(50),
    hourly_rate  DECIMAL(10,2) NOT NULL,
    civil_status VARCHAR(10) NOT NULL DEFAULT 'single'
        CHECK (civil_status IN ('single', 'married'))
);

CREATE TABLE Payroll (
    payroll_id     INT AUTO_INCREMENT PRIMARY KEY,
    employee_id    VARCHAR(10) NOT NULL,
    hours_worked   DECIMAL(6,2) NOT NULL,
    overtime_hours DECIMAL(6,2) NOT NULL DEFAULT 0,
    gross_pay      DECIMAL(10,2) NOT NULL,
    tax            DECIMAL(10,2) NOT NULL,
    deductions     DECIMAL(10,2) NOT NULL,
    net_pay        DECIMAL(10,2) NOT NULL,
    payroll_date   DATE NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);
```

Employee deletion is a **soft delete** (an `is_active` flag), so payroll history for a removed employee remains intact and viewable.

## Screens

| Screen | Purpose |
|---|---|
| Login | Local on-device gate (see below) |
| Dashboard | Entry point to Employees, Calculator, History, Settings |
| Employee List | Browse employees, add/delete, tap a row for details |
| Employee Detail | View an employee's full info, jump to payroll calculation |
| Add Employee | Form to register a new employee |
| Payroll Calculator | Enter hours worked/overtime, compute payroll |
| Payslip | Review a computed result before confirming/saving it |
| Payroll History | Browse past payroll records, tap for detail |
| Settings | SOAP endpoint, REST base URL, deduction defaults, dark mode, logout |

## Login

The login screen checks credentials **on-device only**, against a hardcoded username/password. It is **not** real authentication — it doesn't protect the server or the database, and it is hardcoded so the app doesn't lock out the user on a misconfigured URL. Anyone with the APK can read the credential check in source. Do not rely on this for protecting real data.

## Setup

1. Install XAMPP, start Apache + MySQL.
2. Import the database schema (above) via phpMyAdmin, or run the provided `.sql` file.
3. Place the PHP files (`employees.php`, `payroll.php`, `soap_server.php`, `payroll_functions.php`, `db_connect.php`) in your `htdocs` folder (e.g. `htdocs/payroll/`).
4. Update `db_connect.php` with your MySQL credentials if they differ from the XAMPP defaults (`root` / blank password).
5. In the app's **Settings** screen, set:
   - **REST API Base URL** to your PHP folder, e.g. `http://10.0.2.2/payroll/` (Android emulator alias for host machine's `localhost`)
   - **SOAP Endpoint** to `http://10.0.2.2/payroll/soap_server.php`
6. Run the app. On first launch it fetches employees and payroll history from the server automatically.

If testing on a physical device instead of an emulator, replace `10.0.2.2` with your computer's actual LAN IP address, and make sure the device is on the same network.

## Known limitations

- Tax brackets are simplified sample figures, not accurate BIR tables (see [Tax](#2-tax--computetax) above).
- Login is a local UI gate only, not real authentication.
- No offline mode — employee/payroll data requires a live connection to the PHP backend.
