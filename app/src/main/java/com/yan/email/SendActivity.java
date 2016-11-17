package com.yan.email;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SendActivity extends AppCompatActivity {

    private EditText editText_to, editText_subject, editText_content;
    private Button button_send, button_lianxi;
    private SocketProcess socketProcess;
    private SQLiteDatabase sqLiteDatabase;
    private DBHelper dbHelper;
    private ArrayList<String> zhanghaos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        String user = getIntent().getStringExtra("user");
        String pass = getIntent().getStringExtra("pass");
        socketProcess = new SocketProcess(user, pass);
        editText_to = (EditText) findViewById(R.id.toedit);
        editText_subject = (EditText) findViewById(R.id.subjectedit);
        editText_content = (EditText) findViewById(R.id.contentedit);
        editText_to.setText("");
        editText_subject.setText("");
        editText_content.setText("");
        String to = getIntent().getStringExtra("to");
        if (to != null) {
            editText_to.setText(to);
        }
        button_send = (Button) findViewById(R.id.sendbutton);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断邮箱格式
                final String to = editText_to.getText().toString();
                if (!to.contains("@") || !to.contains(".com")) {
                    Toast.makeText(SendActivity.this, "邮箱格式错误!", Toast.LENGTH_SHORT).show();
                } else {
                    //发送邮件
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean isok;
                            try {
                                isok = socketProcess.sendMessage(to, editText_subject.getText().toString(), editText_content.getText().toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isok) {
                                            insert(to);
                                            Toast.makeText(SendActivity.this, "发送成功！", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else if (!isok) {
                                            Toast.makeText(SendActivity.this, "发送失败！", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                //e.printStackTrace();
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(SendActivity.this,"发生不可预知的致命错误！",Toast.LENGTH_SHORT).show();
//                                    }
//                                });
                            }
                        }
                    }).start();
                }
            }
        });
        dateinit();
        init();
    }

    private void insert(String user){
        ContentValues contentValues=new ContentValues();
        contentValues.put("lianxi",user);
        sqLiteDatabase.insert("lianxitb",null, contentValues);
    }
    private void init() {
        button_lianxi = (Button) findViewById(R.id.lianxibutton);
        button_lianxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(SendActivity.this);
                View view=LayoutInflater.from(SendActivity.this).inflate(R.layout.lianxilayout,null);
                ListView listView= (ListView) view.findViewById(R.id.lianxilistview);
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(SendActivity.this, R.layout.textlayout,zhanghaos);
                listView.setAdapter(arrayAdapter);
                builder.setView(view);
                final AlertDialog alertDialog=builder.create();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String zh=zhanghaos.get(position);
                        editText_to.setText(zh);
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
    }

    private void dateinit() {
        dbHelper = new DBHelper(this);
        sqLiteDatabase = dbHelper.getWritableDatabase();
        String sql = "select *from lianxitb";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        //添加常用联系人信息
        zhanghaos = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String zhanghao = cursor.getString(cursor.getColumnIndex("lianxi"));
                zhanghaos.add(zhanghao);
            }
        }
    }
}
