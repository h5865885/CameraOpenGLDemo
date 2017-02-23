package activitytest.example.xxoo.cameraopengldemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

import activitytest.example.xxoo.cameraopengldemo.ConUtil;
import activitytest.example.xxoo.cameraopengldemo.R;
import activitytest.example.xxoo.cameraopengldemo.Util;

public class LoadingActivity extends Activity {
    private TextView _WarrantyText;
//    private Button _againWarrantyBtn;
//    private ProgressBar WarrantyBar;

    private static final String TAG = "LoadingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        init();
        initData();
        netWork();
    }

    private void init() {
        _WarrantyText = (TextView) findViewById(R.id.textView);
    }

    private void initData() {
        if (Util.API_KEY == null || Util.API_SECRET == null) {
//            if (!ConUt)
        }
    }

    private void netWork() {
        if (Facepp.getSDKAuthType(ConUtil.getFileContent(this, R.raw.megviifacepp_0_4_1_model))
                == 2) {
            //非联网授权
        }
        _WarrantyText.setText("正在联网授权中...");
        final LicenseManager licenseManager = new LicenseManager(this);
        licenseManager.setExpirationMillis(Facepp.getApiExpirationMillis(this, ConUtil
                .getFileContent(this, R.raw.megviifacepp_0_4_1_model)));
        String uuid = ConUtil.getUUIDString(LoadingActivity.this);
        long[] apiName = {
                Facepp.getApiName()
        };
        Log.d(TAG, "netWork: 授权");
        licenseManager.takeLicenseFromNetwork(uuid, Util.API_KEY, Util.API_SECRET, apiName,
                LicenseManager.DURATION_30DAYS, new LicenseManager.TakeLicenseCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "netWork: 授权sucees");
                        authState(true);
                    }

                    @Override
                    public void onFailed(int i, byte[] bytes) {
                        Log.d(TAG, "netWork: 授权failed");
                        authState(false);
                    }
                });
    }

    private void authState(boolean isSuccess) {
        if (isSuccess) {
            startActivity(new Intent(this, CameraOpenGLDemo.class));
            finish();
        } else {
//            Toast.makeText(getApplicationContext(), "默认Toast样式",
//                    Toast.LENGTH_SHORT).show();

            Toast.makeText(this,"授权 failed",Toast.LENGTH_SHORT).show();

//            WarrantyBar.setVisibility(View.GONE);
//            againWarrantyBtn.setVisibility(View.VISIBLE);
//            WarrantyText.setText("联网授权失败！请检查网络或找服务商");
        }
    }
}

