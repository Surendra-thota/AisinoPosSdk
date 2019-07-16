package com.aisino.printer.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vanstone.trans.api.SystemApi;
import com.vanstone.trans.api.PrinterApi;
import android.content.Context;
import android.util.Log;
import com.vanstone.utils.CommonConvert;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.ByteArrayOutputStream;

import java.io.UnsupportedEncodingException;
import android.util.Base64;
import java.math.BigDecimal;

/**
 * This class echoes a string called from JavaScript.
 */
public class AisinoPrinterPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d("aabb", "Enter to plugin exe");
        if (action.equals("printReceipt")) {
            this.printReceipt(args.getJSONObject(0), callbackContext);
            return true;
        }
        if (action.equals("initializeSystem")) {
            this.initializeSystem(callbackContext);
            return true;
        }
        if (action.equals("printSummary")) {
            this.printSummary(args, callbackContext);
            return true;
        }
        return false;
    }

    private void initializeSystem(final CallbackContext callbackContext) {
        Context context = cordova.getActivity().getApplicationContext();
        String CurAppDir = context.getFilesDir().getAbsolutePath();
        Log.d("aabb", CurAppDir);

        try {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("main", "enter to run");
                    SystemApi.SystemInit_Api(0, CommonConvert.StringToBytes(CurAppDir + "/" + "\0"), context);
                    Log.d("main", "complete init");
                    callbackContext.success("System Initialize Success");
                }
            });
        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("SystemAPi Initialize " + e.getMessage());
            callbackContext.error("Printer not supported");

        }
    }

    private void printReceipt(final JSONObject printData, final CallbackContext callbackContext) {
        if (printData != null) {
            String title = "Receipt";
            String copyType = "Customer Copy";
            String paymentDate = "";
            String receiptNumber = "";
            String invoiceNumber = "";
            String paymentMode = "Cash";
            String amount = "";
            Boolean isThanksNote = false;

            try {
                title = printData.has("title") ? printData.getString("title") : "Receipt";
                copyType = printData.has("copyType") ? printData.getString("copyType") : "Customer Copy";
                paymentDate = printData.has("paymentDate") ? printData.getString("paymentDate") : null;
                receiptNumber = printData.has("receiptNumber") ? printData.getString("receiptNumber") : null;
                invoiceNumber = printData.has("invoiceNo") ? printData.getString("invoiceNo") : null;
                paymentMode = printData.has("paymentMode") ? printData.getString("paymentMode") : "Cash";
                amount = printData.has("amountPaid") ? printData.getString("amountPaid") : "0";
                isThanksNote = printData.has("isThanksNote") ? printData.getBoolean("isThanksNote") : false;
            } catch (Exception e) {
                System.out.println("Fetch Data " + e.getMessage());
                callbackContext.error("Data Conversion Failed");
            }

            PrinterApi.PrnClrBuff_Api();

            try {
                Resources activityRes = cordova.getActivity().getResources();
                int logoId2 = activityRes.getIdentifier("baharanlogo", "drawable",
                        cordova.getActivity().getPackageName());
                Drawable logoDrawable2 = activityRes.getDrawable(logoId2);
                Bitmap bitmap2 = ((BitmapDrawable) logoDrawable2).getBitmap();
                PrinterApi.PrnLogo_Api(bitmap2);
            } catch (Exception e) {
                // TODO: handle exception
            }
            PrinterApi.printSetAlign_Api(1);
            PrinterApi.PrnLineSpaceSet_Api((short) 5, 0);
            PrinterApi.PrnFontSet_Api(42, 42, 0);
            PrinterApi.PrnSetGray_Api(15);
            PrinterApi.PrnLineSpaceSet_Api((short) 5, 0);
            PrinterApi.printSetBlodText_Api(true);
            PrinterApi.PrnStr_Api(title);
            PrinterApi.printSetBlodText_Api(false);
            PrinterApi.PrnFontSet_Api(24, 24, 0);
            PrinterApi.PrnStr_Api(copyType);
            PrinterApi.printSetAlign_Api(0);
            PrinterApi.PrnStr_Api("________________________________________________");
            PrinterApi.PrnFontSet_Api(32, 32, 0);
            PrinterApi.printSetBlodText_Api(true);
            if (paymentDate != null && !paymentDate.isEmpty()) {
                PrinterApi.PrnStr_Api("Date: " + paymentDate);
            }
            if (invoiceNumber != null && !invoiceNumber.isEmpty() && invoiceNumber != "null") {
                PrinterApi.PrnStr_Api("Invoice No: " + invoiceNumber);
            }
            if (receiptNumber != null && !receiptNumber.isEmpty()) {
                PrinterApi.PrnStr_Api("Receipt No: " + receiptNumber);
            }
            PrinterApi.PrnStr_Api("Payment Mode: " + paymentMode);
            PrinterApi.PrnStr_Api("Amount Paid: " + amount);
            PrinterApi.printSetBlodText_Api(false);

            if (isThanksNote == true) {

                PrinterApi.PrnStr_Api("________________________________________________");
                PrinterApi.printSetAlign_Api(1);
                PrinterApi.PrnFontSet_Api(32, 32, 0);
                PrinterApi.PrnSetGray_Api(15);
                PrinterApi.PrnLineSpaceSet_Api((short) 5, 0);
                PrinterApi.PrnStr_Api("Thank You");
                PrinterApi.printSetAlign_Api(0);
                PrinterApi.PrnFontSet_Api(20, 20, 0);
                PrinterApi.PrnStr_Api("For any queries contact 07702399999 or");
                PrinterApi.PrnStr_Api("visit Malik Mahmood 60th Street");
                PrinterApi.PrnStr_Api("Baharan City - Block 13 - Ground Floor");
                PrinterApi.PrnFontSet_Api(28, 28, 0);
            }
            PrinterApi.PrnStr_Api("________________________________________________");
            PrinterApi.printSetAlign_Api(1);
            PrinterApi.PrnStr_Api("-- Powered by EcoPay --");
            PrinterApi.PrnStr_Api("\n");
            PrinterApi.PrnStr_Api("\n");

            int printResponse = PrintData();

            if (printResponse == 0) {
                callbackContext.success("Print Success");
            } else {
                callbackContext.error(Integer.toString(printResponse));
            }
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void printSummary(final JSONArray printData, final CallbackContext callbackContext) {
        if (printData != null) {
            PrinterApi.PrnClrBuff_Api();
            PrinterApi.PrnFontSet_Api(36, 36, 0);
            PrinterApi.PrnSetGray_Api(15);
            PrinterApi.PrnLineSpaceSet_Api((short) 3, 0);
            PrinterApi.PrnStr_Api("    Summary Report");
            PrinterApi.PrnLineSpaceSet_Api((short) 2, 0);
            PrinterApi.PrnFontSet_Api(32, 32, 0);
            PrinterApi.PrnSetGray_Api(12);
            PrinterApi.PrnStr_Api("_______________________________________________________");
            PrinterApi.PrnStr_Api("Unit Number\t\t\t Amount");
            PrinterApi.PrnStr_Api("_______________________________________________________");
            PrinterApi.PrnFontSet_Api(28, 28, 0);
            Float totalAmount = 0f;
            try {
                if (printData != null && printData.length() > 0) {
                    for (int i = 0; i < printData.length(); i++) {
                        JSONObject data = printData.getJSONObject(i);
                        String accountNumber = data.has("AccountNumber") ? data.getString("AccountNumber") : null;
                        String paidAmount = data.has("PaidAmount") ? data.getString("PaidAmount") : null;
                        totalAmount += data.has("PaidAmount")
                                ? BigDecimal.valueOf(data.getDouble("PaidAmount")).floatValue()
                                : 0f;
                        PrinterApi.PrnStr_Api(accountNumber + "\t\t\t\t\t\t\t\t\t\t" + paidAmount);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            PrinterApi.PrnStr_Api("________________________________________________");
            PrinterApi.PrnFontSet_Api(32, 32, 0);
            PrinterApi.PrnStr_Api("Total \t\t\t\t\t\t\t\t\t\t" + Float.toString(totalAmount));
            PrinterApi.PrnStr_Api("________________________________________________");

            int printResponse = PrintData();

            if (printResponse == 0) {
                callbackContext.success("Print Success");
            } else {
                callbackContext.error(Integer.toString(printResponse));
            }
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    public static int PrintData() {
        int ret;
        String Buf = null;

        while (true) {
            ret = PrinterApi.PrnStart_Api();
            Log.d("aabb", "PrnStart_Api:" + ret);
            // if (ret == 2) {
            // Buf = "Return:" + ret + " paper is not enough";
            // } else if (ret == 3) {
            // Buf = "Return:" + ret + " too hot";
            // } else if (ret == 4) {
            // Buf = "Return:" + ret + " PLS put it back\nPress any key to reprint";
            // } else if (ret == 0) {
            // return 0;
            // }
            return ret;

        }
    }
}
