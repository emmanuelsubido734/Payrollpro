<?php
/**
 * Core payroll computation logic — kept separate from the SOAP wiring
 * so it can be tested independently and reused later by the PHP
 * SOAP client web form.
 */

function calc_gross_pay($hourlyRate, $hoursWorked, $overtimeHours, $overtimeMultiplier = 1.25) {
    $regularPay = $hourlyRate * $hoursWorked;
    $overtimePay = $hourlyRate * $overtimeMultiplier * $overtimeHours;
    $grossPay = $regularPay + $overtimePay;

    return array(
        "regularPay"  => round($regularPay, 2),
        "overtimePay" => round($overtimePay, 2),
        "grossPay"    => round($grossPay, 2)
    );
}

/**
 * Progressive (bracket-based) monthly tax computation.
 * NOTE: these brackets are simplified sample figures for this class
 * project, not the current official BIR withholding table.
 */
function calc_tax($grossPay, $civilStatus = "single") {
    // Company-defined non-taxable allowance by civil status (project policy, not tax law)
    $exemption = ($civilStatus === "married") ? 5000 : 3000;
    $taxableIncome = max(0, $grossPay - $exemption);

    $brackets = array(
        array("upTo" => 20833,       "rate" => 0.00, "base" => 0,         "over" => 0),
        array("upTo" => 33333,       "rate" => 0.15, "base" => 0,         "over" => 20833),
        array("upTo" => 66667,       "rate" => 0.20, "base" => 1875,      "over" => 33333),
        array("upTo" => 166667,      "rate" => 0.25, "base" => 8541.80,   "over" => 66667),
        array("upTo" => 666667,      "rate" => 0.30, "base" => 33541.80,  "over" => 166667),
        array("upTo" => PHP_INT_MAX, "rate" => 0.35, "base" => 183541.80, "over" => 666667)
    );

    $tax = 0;
    foreach ($brackets as $bracket) {
        if ($taxableIncome <= $bracket["upTo"]) {
            $tax = $bracket["base"] + ($taxableIncome - $bracket["over"]) * $bracket["rate"];
            break;
        }
    }

    return round(max(0, $tax), 2);
}

function calc_deductions($sss, $philHealth, $pagIbig, $otherDeductions = 0) {
    return round($sss + $philHealth + $pagIbig + $otherDeductions, 2);
}

function calc_net_salary($grossPay, $tax, $deductions) {
    return round($grossPay - $tax - $deductions, 2);
}