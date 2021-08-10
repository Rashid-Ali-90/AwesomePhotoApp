package com.awesome.photo;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

public class CustProgressBar {

    ProgressDialog progressDialog;

    public void createProgressBar(Context context)
    {
        try {
            if (progressDialog == null)
            {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Loading...");
                progressDialog.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeProgressBar()
    {
        if (progressDialog != null && progressDialog.isShowing())
        {
            try {
                progressDialog.cancel();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}