package com.firegroup.lanya;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.features2d.BOWTrainer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Implements OpenCV method
 */

public class Test_for_OpenCV extends AppCompatActivity {
    MyApplication MyApp;
    WiFiConnectThread MyAcceptThread;
    SurfaceView img_src, img_res;
    boolean start = false;
    boolean commits = false;
    boolean control_commits = false;
    SurfaceHolder src_Holder, res_Holder;
    RelativeLayout main_layout;
    LinearLayout control_layout;
    LinearLayout filter_layout;
    File DataPath;
    int[] HSVArr = {0,0,0,0,0,0};
    int[] IDList = {R.id.cv_H_Low,R.id.cv_S_Low,R.id.cv_V_Low,R.id.cv_H_High,R.id.cv_S_High,R.id.cv_V_High};
    int[] ParaList = {R.id.cv_back,R.id.cv_up,R.id.cv_mid,R.id.cv_speed};
    int[] Parameters = {0,0,0,0};

    public String[] process_coder(String format){
        File[] myFile = DataPath.listFiles();
        int index = 0;
        for(File file : myFile){
            if(file.getName().contains("."+format))
                index++;
        }
        if(index == 0)
            return null;
        String[] result = new String[index];
        index = 0;
        for(File file : myFile){
            if(file.getName().contains("."+format)) {
                result[index] = file.getName();
                index++;
            }
        }
        return result;
    }

    public void Save(String name, String format){
        String tmp;
        try{
            tmp = Environment.getExternalStorageDirectory().toString()+"/OpenCV";
            File thisData = new File(tmp+"/"+name+"." + format);
            if(thisData.exists())
            {
                Toast.makeText(this,"Wrong name!",Toast.LENGTH_SHORT).show();
                return;
            }
            if(thisData.createNewFile()){
                RandomAccessFile input = new RandomAccessFile(thisData,"rw");
                String mes = "";
                if (format.equals("data")){
                    for(int i = 0; i < 6; i++){
                        mes = mes.concat(" ");
                        mes = mes + String.valueOf(HSVArr[i]);
                    }
                }
                else if (format.equals("para")){
                    for(int i = 0; i < 4; i++){
                        mes = mes.concat(" ");
                        mes = mes + String.valueOf(Parameters[i]);
                    }
                }
                Log.e("OpenCV",mes);
                input.writeUTF(mes);
                input.close();
            }else{
                Toast.makeText(this,"Can't create file! Check out commit!",Toast.LENGTH_SHORT).show();
            }
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"Can't write the data!",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean Load(final String format){
        final String[] naming = process_coder(format);
        if(naming == null) return false;
        AlertDialog.Builder Data = new AlertDialog.Builder(this);
        Data.setTitle("Loading...");
        Data.setItems(naming, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    RandomAccessFile FetchData = new RandomAccessFile(DataPath.getCanonicalFile() + "/"+naming[i],"r");
                    String[] FetchedData = FetchData.readUTF().split(" ");
                    int index = 0;
                    if(format.equals("data")) {
                        for (String data : FetchedData) {
                            if (!data.equals("")) {
                                SeekBar tmp = (SeekBar) findViewById(IDList[index]);
                                tmp.setProgress(Integer.valueOf(data));
                                if (index < 3) {
                                    MyApp.setLow(Integer.valueOf(data), index);
                                } else {
                                    MyApp.setHigh(Integer.valueOf(data), index - 3);
                                }
                                index++;
                                if (index == 6) break;
                            }
                        }
                    }else if (format.equals("para")){
                        for (String data : FetchedData) {
                            if (!data.equals("")) {
                                if(index < 3){
                                    SeekBar tmp = (SeekBar) findViewById(IDList[index]);
                                    tmp.setProgress(Integer.valueOf(data));
                                    MyApp.setParameter(index,Integer.valueOf(data));
                                }
                                if (index == 3){
                                    int value = Integer.valueOf(data);
                                    Parameters[3] = value;
                                    MyApp.setParameter(index,value);
                                    Button tmp = (Button)findViewById(R.id.cv_speed);
                                    if (value == 1){
                                        tmp.setText("Slow down");
                                    }else{
                                        tmp.setText("Speed up");
                                    }
                                    break;
                                }
                                index++;
                            }
                        }
                    }
                    FetchData.close();
                }catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(Test_for_OpenCV.this,"Can't load file",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Data.show();
        return true;
    }

    public boolean Delete(final String format){
        final String[] naming = process_coder(format);
        if(naming == null) return false;
        AlertDialog.Builder Data = new AlertDialog.Builder(this);
        Data.setTitle("Delete...");
        Data.setItems(naming, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    File FetchingData = new File(DataPath.getCanonicalFile() + "/"+naming[i]);
                    if(FetchingData.exists())
                        if(!FetchingData.delete()){
                            Toast.makeText(Test_for_OpenCV.this,"Delete file failed!",Toast.LENGTH_SHORT).show();
                        }
                    String tmp="";
                    for(int index = 0; index < naming.length; index++){
                        if(index == i) continue;
                        tmp = tmp.concat(" "+naming[i]);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(Test_for_OpenCV.this,"Can't delete file",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Data.show();
        return true;
    }


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opencv_test);
        MyApp = (MyApplication)getApplication();
        MyApp.setActivity(this);
        MyAcceptThread = MyApp.getMyAcceptThread();
        main_layout = (RelativeLayout)findViewById(R.id.cv_main_xml);
        control_layout = (LinearLayout)findViewById(R.id.cv_control_xml);
        filter_layout = (LinearLayout) findViewById(R.id.cv_filter_xml);
        String currentPath = Environment.getExternalStorageDirectory().toString();
        Log.e("OpenCV","Get path successful!");
        if(!currentPath.equals("")) {
            DataPath = new File(currentPath + "/OpenCV");
            if(!DataPath.exists()){
                if(!DataPath.mkdirs()){
                    Toast.makeText(this,"Create parent file failed",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        Button filter_commit = (Button) findViewById(R.id.cv_filter_commit);
        filter_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commits = !commits;
                MyApp.setDeal(commits);
            }
        });
        Button filter_load = (Button)findViewById(R.id.cv_filter_load);
        filter_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Load("data"))
                    Toast.makeText(Test_for_OpenCV.this,"No file have been found!",Toast.LENGTH_SHORT).show();
            }
        });
        Button filter_del = (Button)findViewById(R.id.cv_filter_Delete);
        filter_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Delete("data"))
                    Toast.makeText(Test_for_OpenCV.this,"No file have been found!",Toast.LENGTH_SHORT).show();
            }
        });
        Button filter_save = (Button)findViewById(R.id.cv_filter_save);
        filter_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText name = new EditText(Test_for_OpenCV.this);
                new AlertDialog.Builder(Test_for_OpenCV.this).setTitle("Save as ..")
                        .setView(name)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String FileName = name.getText().toString();
                                if(!FileName.equals("")){
                                    Save(FileName,"data");
                                }
                            }
                        }).setNegativeButton("Cancel",null).create().show();
            }
        });

        Button control_commit = (Button) findViewById(R.id.cv_control_begin);
        control_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control_commits = !control_commits;
                MyApp.setControl(control_commits);
            }
        });
        Button control_load = (Button)findViewById(R.id.cv_control_load);
        control_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Load("para"))
                    Toast.makeText(Test_for_OpenCV.this,"No file have been found!",Toast.LENGTH_SHORT).show();
            }
        });
        Button control_del = (Button)findViewById(R.id.cv_control_delete);
        control_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Delete("para"))
                    Toast.makeText(Test_for_OpenCV.this,"No file have been found!",Toast.LENGTH_SHORT).show();
            }
        });
        Button control_save = (Button)findViewById(R.id.cv_control_save);
        control_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText name = new EditText(Test_for_OpenCV.this);
                new AlertDialog.Builder(Test_for_OpenCV.this).setTitle("Save as ..")
                        .setView(name)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String FileName = name.getText().toString();
                                if(!FileName.equals("")){
                                    Save(FileName,"para");
                                }
                            }
                        }).setNegativeButton("Cancel",null).create().show();
            }
        });

        Button connect = (Button) findViewById(R.id.cv_connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = !start;
                if(start){
                    MyAcceptThread.begin();
                }
                else{
                    MyAcceptThread.pause();
                }
            }
        });

        Button filter_return = (Button)findViewById(R.id.cv_filter_return);
        filter_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            filter_layout.setVisibility(View.GONE);
            main_layout.setVisibility(View.VISIBLE);
            }
        });

        Button control_return = (Button)findViewById(R.id.cv_control_return);
        control_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control_layout.setVisibility(View.GONE);
                main_layout.setVisibility(View.VISIBLE);
            }
        });

        Button control = (Button)findViewById(R.id.cv_control);
        control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main_layout.setVisibility(View.GONE);
                control_layout.setVisibility(View.VISIBLE);
            }
        });

        Button filter = (Button)findViewById(R.id.cv_filter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main_layout.setVisibility(View.GONE);
                filter_layout.setVisibility(View.VISIBLE);
            }
        });

        img_src = (SurfaceView)findViewById(R.id.cv_src);
        src_Holder = img_src.getHolder();
        img_res = (SurfaceView)findViewById(R.id.cv_res);
        res_Holder = img_res.getHolder();
        MyApp.setMyholder(src_Holder);
        MyApp.setDeal_holder(res_Holder);
        SeekBar[] myList = new SeekBar[6];
        for(int Iteration = 0; Iteration < 6 ; Iteration++){
            final int index = Iteration;
            myList[Iteration] = (SeekBar)findViewById(IDList[Iteration]);
            myList[Iteration].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    HSVArr[index] = i;
                    if(index < 3){
                        MyApp.setLow(i,index);
                    }
                    else{
                        MyApp.setHigh(i,index-3);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        SeekBar[] Para = new SeekBar[3];
        for(int Iter = 0; Iter < 3; Iter++){
            final int index = Iter;
            Para[Iter] = (SeekBar)findViewById(ParaList[Iter]);
            Para[Iter].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    Parameters[index] = i;
                    MyApp.setParameter(index,i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        final Button Speed = (Button)findViewById(R.id.cv_speed);
        Speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Speed.getText().toString().contains("up")){
                    Parameters[3] = 1;
                    Speed.setText("Slow down");
                    MyApp.setParameter(3,1);
                }
                else{
                    Parameters[3] = 0;
                    Speed.setText("Speed up");
                    MyApp.setParameter(3,0);
                }
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        MyApp.setDeal(false);
        if(start){
            start = false;
            MyAcceptThread.pause();
        }
    }
}
