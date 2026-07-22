package com.payrollpro.app.network

import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import org.ksoap2.serialization.MarshalFloat

data class GrossPayResult(
    val regularPay: Double,
    val overtimePay: Double,
    val grossPay: Double
)

/**
 * Thin wrapper around ksoap2-android calling the four PHP SOAP transactions
 * on soap_server.php. One instance per call is fine — it's stateless.
 */
class PayrollSoapClient(private val endpointUrl: String) {

    private val namespace = "urn:PayrollService"

    private fun call(methodName: String, envelope: SoapSerializationEnvelope) {
        val soapAction = "$namespace#$methodName"
        val transport = HttpTransportSE(endpointUrl)
        transport.call(soapAction, envelope)
    }

    private fun buildEnvelope(request: SoapObject): SoapSerializationEnvelope {
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)
        MarshalFloat().register(envelope)
        return envelope
    }

    fun computeGrossPay(
        hourlyRate: Double,
        hoursWorked: Double,
        overtimeHours: Double,
        overtimeMultiplier: Double
    ): GrossPayResult {
        val request = SoapObject(namespace, "ComputeGrossPay")
        request.addProperty("hourlyRate", hourlyRate)
        request.addProperty("hoursWorked", hoursWorked)
        request.addProperty("overtimeHours", overtimeHours)
        request.addProperty("overtimeMultiplier", overtimeMultiplier)

        val envelope = buildEnvelope(request)
        call("ComputeGrossPay", envelope)

        val result = envelope.response as SoapObject
        return GrossPayResult(
            regularPay = result.getProperty("regularPay").toString().toDouble(),
            overtimePay = result.getProperty("overtimePay").toString().toDouble(),
            grossPay = result.getProperty("grossPay").toString().toDouble()
        )
    }

    fun computeTax(grossPay: Double, civilStatus: String): Double {
        val request = SoapObject(namespace, "ComputeTax")
        request.addProperty("grossPay", grossPay)
        request.addProperty("civilStatus", civilStatus)

        val envelope = buildEnvelope(request)
        call("ComputeTax", envelope)

        return envelope.response.toString().toDouble()
    }

    fun computeDeductions(
        sss: Double,
        philHealth: Double,
        pagIbig: Double,
        otherDeductions: Double
    ): Double {
        val request = SoapObject(namespace, "ComputeDeductions")
        request.addProperty("sss", sss)
        request.addProperty("philHealth", philHealth)
        request.addProperty("pagIbig", pagIbig)
        request.addProperty("otherDeductions", otherDeductions)

        val envelope = buildEnvelope(request)
        call("ComputeDeductions", envelope)

        return envelope.response.toString().toDouble()
    }

    fun computeNetSalary(grossPay: Double, tax: Double, deductions: Double): Double {
        val request = SoapObject(namespace, "ComputeNetSalary")
        request.addProperty("grossPay", grossPay)
        request.addProperty("tax", tax)
        request.addProperty("deductions", deductions)

        val envelope = buildEnvelope(request)
        call("ComputeNetSalary", envelope)

        return envelope.response.toString().toDouble()
    }
}