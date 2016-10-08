package manager.permission.com.permission;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.medzone.permission.PermissionDenied;
import com.medzone.permission.PermissionGant;
import com.permission.lib.PermissionManager;

public class MainActivity extends AppCompatActivity {


    private TextView tvPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvPanel = (TextView) findViewById(R.id.tv_panel);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void requestPermission1(View view) {
        PermissionManager.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION,12);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    public void requestPermission2(View view) {
        PermissionManager.requestPermissions(this, 10, Manifest.permission.READ_CONTACTS);

    }

    public void requestPermission3(View view) {
        tvPanel.setText("");
    }

    @PermissionGant(10)
    public void contactSuccess() {
        Snackbar.make(getWindow().getDecorView(), "联系人权限获取成功", Snackbar.LENGTH_SHORT).show();
    }

    @PermissionDenied(10)
    public void contactFail() {
        Snackbar.make(getWindow().getDecorView(), "联系人权限获取失败，请尝试在设置中开启", Snackbar.LENGTH_SHORT).show();
    }

    @PermissionGant(12)
    public void locationSuccess() {
        Snackbar.make(getWindow().getDecorView(), "位置权限获取成功", Snackbar.LENGTH_SHORT).show();
    }

    @PermissionDenied(12)
    public void locationFail() {
        Snackbar.make(getWindow().getDecorView(), "位置权限获取失败，请尝试在设置中开启", Snackbar.LENGTH_SHORT).show();
    }

}
