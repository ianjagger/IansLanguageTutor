package com.psychastria.ianslanguagetutor;

import android.app.Activity;
import android.app.AlertDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utilities {
    public static void processException(Activity a, Exception ex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(a);

        builder.setMessage(ex.getMessage() + ex.getStackTrace())
                .setTitle("Error");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static boolean containsElement(String[] inputArray, String value)
    {
        boolean found = false;

        int count = inputArray.length;

        for (String anInputArray : inputArray) {
            if (anInputArray.equals(value)) {
                found = true;
            }
        }

        return found;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
