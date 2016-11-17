package com.yan.email;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayList<Itemmail> itemmails;
    private SocketProcess socketProcess;
    private MyAdapter myAdapter;
    private ListView listView;
    private int sum = 0;
    private boolean flag = true, timeflag = true;
    private NotificationManager manager;
    private TextView textView;
    private int updatetime;
    private SQLiteDatabase sqLiteDatabase;
    private DBHelper dbHelper;
    private ArrayList<Zhanghao> zhanghaos;
    private ArrayList<String> zhanghaonomima;
    private String user;
    private String pass;
    private String[] times;
    private Thread thread;
    private TextView name, zhanghaotextview;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private int nowcount = 0;

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
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this, SendActivity.class);
                intent.putExtra("user", socketProcess.getUser());
                intent.putExtra("pass", socketProcess.getPass());
                startActivity(intent);
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View nav = navigationView.getHeaderView(0);
        name = (TextView) nav.findViewById(R.id.name);
        zhanghaotextview = (TextView) nav.findViewById(R.id.zhanghaotextView);
        navigationView.setNavigationItemSelectedListener(this);

        user = getIntent().getStringExtra("user");
        pass = getIntent().getStringExtra("pass");
        updatetime = 1000 * 60 * 5;
        times = "5 s,1 min,5 min,10 min,15 min,30 min,60 min".split(",");
        init();
    }

    private void dateinit() {
        dbHelper = new DBHelper(this);
        sqLiteDatabase = dbHelper.getWritableDatabase();
        String sql = "select *from zhanghaotb";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        //添加账号信息
        zhanghaonomima = new ArrayList<>();
        zhanghaos = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String zhanghao = cursor.getString(cursor.getColumnIndex("zhanghao"));
                String mima = cursor.getString(cursor.getColumnIndex("mima"));
                Zhanghao zhanghao1 = new Zhanghao();
                zhanghao1.mima = mima;
                zhanghao1.zhanghao = zhanghao;
                zhanghaos.add(zhanghao1);
                zhanghaonomima.add(zhanghao);
            }
        }
        cursor.close();
        sql = "select *from settingtb where kind='updatetime'";
        cursor = sqLiteDatabase.rawQuery(sql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String timestring = cursor.getString(cursor.getColumnIndex("valuestring"));
                updatetime = Integer.parseInt(timestring);
            }
        }
        sharedPreferences = getSharedPreferences("mailuser", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    private ProgressDialog mDialog;

    private void init() {
        dateinit();
        mDialog = new ProgressDialog(MainActivity.this);
        flag = true;
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        textView = (TextView) findViewById(R.id.count);
        itemmails = new ArrayList<>();
        socketProcess = new SocketProcess(user, pass);
        name.setText(user.substring(0, user.indexOf("@")));
        zhanghaotextview.setText(user);
        listView = (ListView) findViewById(R.id.maillist);
        listView.setVisibility(View.GONE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int set = sum - position;
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("position", set);
                intent.putExtra("user", user);
                intent.putExtra("pass", pass);
                startActivity(intent);
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 当不滚动时
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
//                    Log.i("viewlist", view.getLastVisiblePosition() + "  " + intflag);
                    // 判断是否滚动到底部
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        if (nowcount == sum) {
                            Toast.makeText(MainActivity.this, "已加载全部数据！", Toast.LENGTH_SHORT).show();
                        } else {
                            mDialog.setMessage("正在加载更多.....");
                            mDialog.setIndeterminate(false);
                            mDialog.setCancelable(false);
                            mDialog.show();
                            //加载更多功能的代码
                            startinsert();
                        }
                    } /*else if (view.getFirstVisiblePosition() == 0 && view.getLastVisiblePosition() == 6) {
                        mDialog.setMessage("正在刷新服务信息.....");
                        mDialog.setIndeterminate(false);
                        mDialog.setCancelable(false);
                        mDialog.show();
                        //刷新服务器代码
                        updatemail();
                    }*/
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        myAdapter = new MyAdapter(MainActivity.this, itemmails);
        listView.setAdapter(myAdapter);
        initinsert();
    }

    private ArrayList<Itemmail> miditemmails;

    //手动插入
    public void startinsert() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    nowcount = itemmails.size();
                    miditemmails = socketProcess.getlist(nowcount);
                } catch (Exception e) {
                    //e.printStackTrace();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this,"发生不可预知的致命错误！",Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }
                Log.i("mymail", itemmails.size() + " " + socketProcess.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itemmails.addAll(miditemmails);
                        myAdapter.notifyDataSetChanged();
                        mDialog.dismiss();
                    }
                });
            }
        }).start();
    }

//    private int intflag = -1;

    //初始化插入数据
    public void initinsert() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    nowcount = itemmails.size();
                    sum = socketProcess.getsumnoc();
                    miditemmails = socketProcess.getlist(nowcount);
                } catch (Exception e) {
                    //e.printStackTrace();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this,"发生不可预知的致命错误！",Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }
                Log.i("mymail", itemmails.size() + " " + socketProcess.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setVisibility(View.VISIBLE);
                        itemmails.addAll(miditemmails);
                        myAdapter.notifyDataSetChanged();
                        textView.setText("共" + sum + "条记录");
                        startupdate();
                    }
                });
            }
        }).start();
    }

    //手动更新
    public void updatemail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    nowcount = itemmails.size();
                    sum = socketProcess.getsum();
                    miditemmails = socketProcess.getlist(nowcount);
                } catch (Exception e) {
                    //e.printStackTrace();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this,"发生不可预知的致命错误！",Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }
                Log.i("mymail", itemmails.size() + " " + socketProcess.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        flagdialog=false;
                        listView.setVisibility(View.VISIBLE);
                        itemmails.addAll(miditemmails);
                        myAdapter.notifyDataSetChanged();
                        textView.setText("共" + sum + "条记录");
                        mDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    //加载后台更新程序
    public void startupdate() {
        thread = new Thread(new Runnable() {
            @Override
            //后台刷新
            public void run() {
                SocketProcess socketProcess1 = new SocketProcess(socketProcess.getUser(), socketProcess.getPass());
                while (flag) {
                    if (!socketProcess1.getUser().equals(socketProcess.getUser())) {
                        break;
                    }
                    try {
                        //获取邮件总数
                        final int count = socketProcess1.getsum();
                        if (count > sum) {
                            //刷新邮件列表,不考虑删除邮件的情况
                            sum = count;
                            final Itemmail itemmail = socketProcess1.getitemmail(count);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //插在队首
                                    itemmails.add(0, itemmail);
                                    myAdapter.notifyDataSetChanged();
                                    textView.setText("共" + sum + "条记录");
                                    //提示消息
                                    PendingIntent pendingIntent3 = PendingIntent.getActivity(MainActivity.this, 0,
                                            new Intent(MainActivity.this, DetailActivity.class)
                                                    .putExtra("position", sum)
                                                    .putExtra("user", user)
                                                    .putExtra("pass", pass), 0);
                                    //初始化通知栏
                                    Notification notify = new Notification.Builder(MainActivity.this)
                                            .setSmallIcon(android.R.drawable.ic_dialog_email)
                                            .setTicker("TickerText:" + "您有新的邮件，请注意查收！")
                                            .setContentTitle("您有新的邮件！")
                                            .setContentText("点击查看详情")
                                            .setContentIntent(pendingIntent3)
                                            .setDefaults(Notification.DEFAULT_ALL)
                                            .build();
                                    manager.notify(Notification.DEFAULT_ALL, notify);
                                }
                            });
                        }
                        timeflag = true;
                        int divid = updatetime / 5;
                        for (int i = 0; i < divid; i++) {
                            Thread.sleep(5);
                            if (!timeflag) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this,"发生不可预知的致命错误！",Toast.LENGTH_SHORT).show();
//                            }
//                        });
                        startupdate();
                        break;
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(false);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_update) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.lianxilayout, null);
            ListView listView = (ListView) view.findViewById(R.id.lianxilistview);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.textlayout, times);
            listView.setAdapter(arrayAdapter);
            builder.setView(view);
            final AlertDialog alertDialog = builder.create();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String time = times[position];
                    String kind = time.split(" ")[1];
                    time = time.split(" ")[0];
                    int n;
                    if (kind.equals("min")) {
                        n = 1000 * 60;
                    } else {
                        n = 1000;
                        if (time.equals("5")) {
                            Toast.makeText(MainActivity.this, "更新过于频繁可能会被“封号”，第二天才能重新使用哦！", Toast.LENGTH_SHORT).show();
                        }
                    }
                    updatetime = Integer.parseInt(time) * n;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("valuestring", updatetime + "");
                    sqLiteDatabase.update("settingtb", contentValues, "kind='updatetime'", null);
                    timeflag = false;
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        } else if (id == R.id.nav_change) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.lianxilayout, null);
            ListView listView = (ListView) view.findViewById(R.id.lianxilistview);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.textlayout, zhanghaonomima);
            listView.setAdapter(arrayAdapter);
            builder.setView(view);
            final AlertDialog alertDialog = builder.create();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Zhanghao zhanghao = zhanghaos.get(position);
                    flag = false;
                    user = zhanghao.zhanghao;
                    pass = zhanghao.mima;
                    editor.putString("user", user);
                    editor.putString("pass", pass);
                    editor.commit();
                    //更新信息
                    init();
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        } else if (id == R.id.nav_insert) {
            editor.remove("user");
            editor.remove("pass");
            editor.commit();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        } else if (id == R.id.nav_runaway) {
            editor.remove("user");
            editor.remove("pass");
            editor.commit();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        } else if (id == R.id.nav_shuaxing) {
            mDialog.setMessage("正在刷新服务信息.....");
            mDialog.setIndeterminate(false);
            mDialog.setCancelable(false);
            mDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mDialog.dismiss();
                    if(flagdialog){
                        mDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"连接服务器失败！",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
            //刷新服务器代码
            itemmails.clear();
            updatemail();
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private boolean flagdialog=true;
}
