package com.yan.email;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private SocketProcess socketProcess;
    private Itemmail itemmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DetailActivity.this,SendActivity.class);
                intent.putExtra("user",socketProcess.getUser());
                intent.putExtra("pass",socketProcess.getPass());
                intent.putExtra("to",itemmail.from);
                startActivity(intent);
            }
        });
        init();
    }


    private void init() {
        final int position=getIntent().getIntExtra("position",1);
        String user=getIntent().getStringExtra("user");
        String pass=getIntent().getStringExtra("pass");
        socketProcess=new SocketProcess(user,pass);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    itemmail=socketProcess.getitemmail(position);
                } catch (Exception e) {
                    //e.printStackTrace();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(DetailActivity.this,"发生不可预知的致命错误！",Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }
                if(itemmail!=null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DetailActivity.this.setTitle("邮件详情");
                            TextView content= (TextView) findViewById(R.id.mailcontent);
                            String contentstring="from: \n\t\t\t"+itemmail.from+"\n";
                            contentstring+="date: \n\t\t\t"+itemmail.time+"\n\n";
                            contentstring+="subject: \n" +
                                    "\t\t\t"+itemmail.subject+"\n\n";
                            contentstring+="content: \n"+itemmail.content+"\n";
                            content.setText(contentstring);
                        }
                    });
                }
            }
        }).start();
    }


}
