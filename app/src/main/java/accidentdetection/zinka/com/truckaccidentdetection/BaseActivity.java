package accidentdetection.zinka.com.truckaccidentdetection;

import android.support.v7.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {

    private CustomProgressDialog mProgressDialog;

    public void showProgressDialog(String message) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            dismissProgressDialog();
        }
        if (this == null) {
            return;
        }
        mProgressDialog = new CustomProgressDialog(this,message);
        mProgressDialog.setCancelable(false);
        try {
            mProgressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
