package com.snailstudio.xsdk.downloadmanager.demo;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.snailstudio.xsdk.downloadmanager.core.DownloadConfig;
import com.snailstudio.xsdk.downloadmanager.core.OnDownloadListener;
import com.snailstudio.xsdk.downloadmanager.utils.FileUtils;

import java.io.File;

import static com.snailstudio.xsdk.downloadmanager.core.DownloadConfig.newDownloadConfig;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String[] urls = {
            "http://p.gdown.baidu.com/6d123c729501d527ac0a68a80375b2a0bdc4be2c5414054952f5e436c2787fc26377a80a758eb848b926e7458ab4b16761d9cf4ebe0af177720134ac3e2cead44f3c6988e3f9d25774c85ca1a5e101a34df61c94cc7e1fe2399756ec9a4b2880fa0ff0cd174d672a122a87edc86347751538926fee90cd00b7d25c5f3c42cad1aa5a2dfd9705d4a5676095c0f215ee5e5d7d3eb6a53d045556d9ca5551c30b22cd7316f6100fc2568163b521decce55f77b104f8269062e7",
            "http://p.gdown.baidu.com/c9bf16e103cac73b7e291eb9c01af3ef2658ffb815945076975f5e547336bc22028626e9fc5af207b672f2323bbb79869380b07b2429ea37dcef1552b4bac44e6d8165cc1e108d76379efa1c05c3e89a01bdea7286f6204363a5246dda681878e009abb134b2abae46116df6a77e80112e361ef9833172a32ae3ebaec7cd7890c0ac0921791bf726a72650974a824bd77b2b51d1f46a557da36991520025faf991b4db0409694ed66c73ad0853061ebb2365f2f3be959d50b9a369921ad15104415f851aa88efc6a97677851ef6ff01627bd877f19625f1d9b59df43f2a2b006b08ceff50cb5e58ec515e6d29787dcaafdb156e92971cd89c1250aa4f4faf9719bf343e9499ed7dfa16bc469ba77d0ed1fd9f7956b159680610ba63e18e2f65347ca367be7a005944cb6f136110519e816bfef7d656452a175b5f81862f2f070553606fcfe4fdce1",
            "http://p.gdown.baidu.com/40413ff113cfc9d2c1b379a625ad93ec51348269a35a472faf3c05086477bc1649501f3333a187b3871dfa560123f733e39e6bc7d9fab9a52093102d7e6c0d3e501e428e0f0afb6ac2e97b13cdde7b258ec319aaea443526e36ba358d80a01c191662b8e2de0c84a2640ce8e0bea38edf13cb4885a69a662b4ed112c10b1c5a2033b888ddd04fe49bbcb6a65e857b5a45b38ddf2d1e9818d",
            "http://p.gdown.baidu.com/0d8f963583070e645a2862a9f8a13d8852bc524e12d7fd6555b1dd92e64ea97f23b28ec43c9740d25f76228d8c93ccd6b2276b8d76c4307274cf3cf64a92a4e4bc7f3f28556d0b8a10d582823a14aece3345cd58f071b78d5c93f0349428e6e9ca7b0f8ca07b1940be55a401dafa7c5984fe97a03f344031d9cf0b6361e600fb02fcc12bdfdccbde970dd9d3bbbd20e2b77ffc9daea3abb1c9039d0c55b378309958199c3a798dd812cdc2209f1aaad9c1250aa4f4faf971e4ea485cd3b708c55e3de67ecd68cdd7374231bc9b5a5351d7e3dbc0d92cd62ab875e273dec2f41d52d37f4892946a4eb06555e857ae012a4ad36022d1c7245fd65cead919dae032",
            "http://p.gdown.baidu.com/62843f116b38e505e6eb715ef434167e25ac6c024751e3e1a4924b8c3829b778ba8a459e3c1606e2742bc2ec18700fb52becbd4b5c7e0e9d9330d0ae20c07250fa880a5fce05102ebd1c6ed423812e93e0e0a4b716d69a8a8f01b5e0c0d3d4fd36f0fef20d4ecb9f003eb8a8ab5a55e89f4e501b0f5a6093a28679d15f6326bb051a9636ba771e251538926fee90cd00b49304da1821bbdb8f5af067b096530d09395ce0f81e0272bea4ae947f4e6001d7c7c6d5cb911d482739a1670662e5be5563a8fac28e6ee1304ccc57ce341d76712af8ecde75eec5a4d30e89bdc43d8a",
            "http://p.gdown.baidu.com/4ce40b0168c8638f9b0343d13d4f86ac5e4ade86dff3de0fc1250aa4f4faf9712567f652f4334a3e3f72bebfb8dd5d3aeade61a92ad308d2329745d277aa52a260d4f44f27564aa48932676c20f08d1c93821d2a89f65ff69622f5e771170f83bcc1365b6767ccff13c650e843d54771",
            "http://imtt.dd.qq.com/16891/6352ED6982A630FF770972495624240A.apk?fsname=cn.andouya_3.2.0406_233.apk&csr=1bbd",
            "http://w73.xitongxz.net:808/201709/02/LB_GHOST_WIN7_SP1_X86_V2017_09.iso",
            "magnet:?xt=urn:btih:03621694F0E8B2CE87216C99CB5CA3AF23029E37"
    };

    private String[] names = {
            "腾讯QQ.apk",
            "微信.apk",
            "天天飞车.apk",
            "聚爆.apk",
            "小文件1.apk",
            "小文件2.apk",
            "小文件3.apk",
            "超大文件.iso",
            "磁力链接.torrent"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        initView();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initView() {
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        for (int i = 0; i < urls.length; i++) {
            Button bt = new Button(this);
            bt.setText(names[i]);
            bt.setId(i);
            bt.setOnClickListener(this);
            container.addView(bt,
                    new LinearLayout.LayoutParams(
                            200,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private void download(final int i) {

        File file = new File(FileUtils.getRealFilePath(DownloadConfig.DOWNLOAD_FILE_PATH
                + File.separator + names[i]));
        if (file.exists())
            file.delete();

        newDownloadConfig()
                .context(this)
                .url(urls[i])
                .path(file.getPath(), false)
                .name(names[i])
                .create()
                .start(new OnDownloadListener() {
                    @Override
                    public void onStart(long fileSize) {

                    }

                    @Override
                    public void onError(String message) {
                        Log.d(TAG, "onError:" + message);
                        Toast.makeText(MainActivity.this, "onError:" + message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete(String downloadPath, long time, long downloadedSize) {

                        Log.d(TAG, "OnDownloadComplete time:" + time + ", downloadedSize:" + downloadedSize);
                        Toast.makeText(MainActivity.this, "Download " + names[i] + " complete!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPaused(long fileSize, long downloadedSize) {

                    }

                    @Override
                    public void onProcess(long fileSize, long downloadedSize, double speed) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    break;
                }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        download(v.getId());
    }
}
