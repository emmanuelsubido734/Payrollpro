<?php
require_once 'nusoap/lib/nusoap.php';
require_once 'payroll_functions.php';

$server = new nusoap_server();
$server->configureWSDL('PayrollService', 'urn:PayrollService');
$server->wsdl->schemaTargetNamespace = 'urn:PayrollService';

// Complex type must be defined before it's referenced by register()
$server->wsdl->addComplexType(
    'GrossPayResult', 'complexType', 'struct', 'all', '',
    array(
        'regularPay'  => array('name' => 'regularPay', 'type' => 'xsd:float'),
        'overtimePay' => array('name' => 'overtimePay', 'type' => 'xsd:float'),
        'grossPay'    => array('name' => 'grossPay', 'type' => 'xsd:float')
    )
);

// ---- 1. ComputeGrossPay ----
$server->register('ComputeGrossPay',
    array('hourlyRate' => 'xsd:float', 'hoursWorked' => 'xsd:float',
          'overtimeHours' => 'xsd:float', 'overtimeMultiplier' => 'xsd:float'),
    array('return' => 'tns:GrossPayResult'),
    'urn:PayrollService',
    'urn:PayrollService#ComputeGrossPay',
    'rpc', 'encoded',
    'Computes regular pay, overtime pay, and gross pay.'
);

function ComputeGrossPay($hourlyRate, $hoursWorked, $overtimeHours, $overtimeMultiplier) {
    if ($hourlyRate < 0 || $hoursWorked < 0 || $overtimeHours < 0) {
        return new soap_fault('Client', '', 'Negative values are not allowed.');
    }
    return calc_gross_pay($hourlyRate, $hoursWorked, $overtimeHours, $overtimeMultiplier);
}

// ---- 2. ComputeTax ----
$server->register('ComputeTax',
    array('grossPay' => 'xsd:float', 'civilStatus' => 'xsd:string'),
    array('return' => 'xsd:float'),
    'urn:PayrollService',
    'urn:PayrollService#ComputeTax',
    'rpc', 'encoded',
    'Computes withholding tax using a progressive bracket schedule.'
);

function ComputeTax($grossPay, $civilStatus) {
    if ($grossPay < 0) {
        return new soap_fault('Client', '', 'Gross pay cannot be negative.');
    }
    return calc_tax($grossPay, $civilStatus);
}

// ---- 3. ComputeDeductions ----
$server->register('ComputeDeductions',
    array('sss' => 'xsd:float', 'philHealth' => 'xsd:float',
          'pagIbig' => 'xsd:float', 'otherDeductions' => 'xsd:float'),
    array('return' => 'xsd:float'),
    'urn:PayrollService',
    'urn:PayrollService#ComputeDeductions',
    'rpc', 'encoded',
    'Sums all mandatory and other deductions.'
);

function ComputeDeductions($sss, $philHealth, $pagIbig, $otherDeductions) {
    return calc_deductions($sss, $philHealth, $pagIbig, $otherDeductions);
}

// ---- 4. ComputeNetSalary ----
$server->register('ComputeNetSalary',
    array('grossPay' => 'xsd:float', 'tax' => 'xsd:float', 'deductions' => 'xsd:float'),
    array('return' => 'xsd:float'),
    'urn:PayrollService',
    'urn:PayrollService#ComputeNetSalary',
    'rpc', 'encoded',
    'Computes final net salary after tax and deductions.'
);

function ComputeNetSalary($grossPay, $tax, $deductions) {
    return calc_net_salary($grossPay, $tax, $deductions);
}

// Dispatch the incoming request
$requestData = file_get_contents('php://input');
$server->service($requestData);