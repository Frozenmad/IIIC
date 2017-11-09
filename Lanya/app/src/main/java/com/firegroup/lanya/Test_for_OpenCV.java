package com.firegroup.lanya;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Froze on 2017/11/4.
 */

public class Test_for_OpenCV extends AppCompatActivity {
    MyApplication MyApp;
    WiFiConnectThread MyAcceptThread;
    SurfaceView img_src, img_res;
    boolean start = false;
    boolean commits = false;
    SurfaceHolder src_Holder, res_Holder;
    File DataPath,DataRecoder;
    int[] HSVarr = {0,0,0,0,0,0};
    int[] idlist = {R.id.cv_H_Low,R.id.cv_S_Low,R.id.cv_V_Low,R.id.cv_H_High,R.id.cv_S_High,R.id.cv_V_High};

    public String[] process_coder(){
        File[] myfile = DataPath.listFiles();
        int index = 0;
        for(File file : myfile){
            if(file.getName().contains(".data"))
                index++;
        }
        if(index == 0)
            return null;
        String[] result = new String[index];
        index = 0;
        for(File file : myfile){
            if(file.getName().contains(".data")) {
                result[index] = file.getName();
                index++;
            }
        }
        return result;
    }

    public void Save(String name){
        String tmp = new String();
        try{
            tmp = Environment.getExternalStorageDirectory().toString()+"/OpenCV";
            File thisData = new File(tmp+"/"+name+".data");
            if(thisData.exists())
            {
                Toast.makeText(this,"Wrong name!",Toast.LENGTH_SHORT).show();
                return;
            }
            if(thisData.createNewFile()){
                RandomAccessFile input = new RandomAccessFile(thisData,"rw");
                String mes = "";
                for(int i = 0; i < 6; i++){
                    mes = mes + " ";
                    mes = mes + String.valueOf(HSVarr[i]);
                }
                Log.e("OpenCV",mes);
                input.writeUTF(mes);
                if(DataRecoder.exists()) {
                    RandomAccessFile recod = new RandomAccessFile(DataRecoder, "rw");
                    String contents = recod.readUTF();
                    recod.writeUTF(contents+" "+name);
                    recod.close();
                }else{
                    Toast.makeText(this,"Can't find the DataRecoder file! Restart may help",Toast.LENGTH_SHORT).show();
                }
                input.close();
            }else{
                Toast.makeText(this,"Can't create file! Check out commit!",Toast.LENGTH_SHORT).show();
            }
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"Can't write the data!",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean Load(){
        try {
            if(DataRecoder.exists()) {
                RandomAccessFile file = new RandomAccessFile(DataRecoder, "r");
                String Content = file.readUTF();
                final String[] naming = process_coder();
                if(naming == null) return false;
                AlertDialog.Builder Data = new AlertDialog.Builder(this);
                Data.setTitle("Loading...");
                Data.setItems(naming, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            RandomAccessFile Fetchedata = new RandomAccessFile(DataPath.getCanonicalFile() + "/"+naming[i],"r");
                            String[] fetcheddata = Fetchedata.readUTF().split(" ");
                            int index = 0;
                            for(String data : fetcheddata){
                                if(!data.equals("")){
                                    SeekBar tmp = (SeekBar)findViewById(idlist[index]);
                                    tmp.setProgress(Integer.valueOf(data));
                                    if(index < 3){
                                        MyApp.setLow(Integer.valueOf(data),index);
                                    }else{
                                        MyApp.setHigh(Integer.valueOf(data),index-3);
                                    }
                                    index ++ ;
                                    if(index == 6) break;
                                }
                            }
                            Fetchedata.close();
                        }catch (IOException e){
                            e.printStackTrace();
                            Toast.makeText(Test_for_OpenCV.this,"Can't load file",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Data.show();
                file.close();
            }else{
                Toast.makeText(this,"Can't find the DataRecoder file! Restart may help",Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"Can't load file",Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public boolean Delete(){
        try {
            if(DataRecoder.exists()) {
                RandomAccessFile file = new RandomAccessFile(DataRecoder, "r");
                String Content = file.readUTF();
                final String[] naming = process_coder();
                if(naming == null) return false;
                AlertDialog.Builder Data = new AlertDialog.Builder(this);
                Data.setTitle("Delete...");
                Data.setItems(naming, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            File Fetchedata = new File(DataPath.getCanonicalFile() + "/"+naming[i]);
                            if(Fetchedata.exists())
                                Fetchedata.delete();
                            String tmp = "";
                            for(int index = 0; index < naming.length; index++){
                                if(index == i) continue;
                                tmp+=" "+naming[i];
                            }
                            if(DataRecoder.exists())
                                DataRecoder.delete();
                            DataRecoder.createNewFile();
                            RandomAccessFile Recod = new RandomAccessFile(DataRecoder,"rw");
                            Recod.writeUTF(tmp);
                            Recod.close();
                        }catch (IOException e){
                            e.printStackTrace();
                            Toast.makeText(Test_for_OpenCV.this,"Can't delete file",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Data.show();
                file.close();
            }else{
                Toast.makeText(this,"Can't find the DataRecoder file! Restart may help",Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"Can't delete file",Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opencv_test);
        MyApp = (MyApplication)getApplication();
        MyApp.setActivity(this);
        MyAcceptThread = MyApp.getMyAcceptThread();
        String currentPath = Environment.getExternalStorageDirectory().toString();
        String tmppath = "";
        try{
            tmppath = currentPath;
            Log.e("OPENCV","Get path successful!");
            if(!tmppath.equals("")) {
                DataPath = new File(tmppath + "/OpenCV");
                if(!DataPath.exists()){
                    if(!DataPath.mkdirs()){
                        Toast.makeText(this,"Create parent file failed",Toast.LENGTH_SHORT).show();
                    }
                }
                DataRecoder = new File(tmppath+"/OpenCV/Recode.data");
                if(!DataRecoder.exists()){
                    if(!DataRecoder.createNewFile()){
                        Toast.makeText(this,"Create recoder file failed",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"Get path failed",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        Button commit = (Button) findViewById(R.id.cv_commit);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commits = !commits;
                MyApp.setDeal(commits);
            }
        });
        Button load = (Button)findViewById(R.id.cv_load);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Load())
                    Toast.makeText(Test_for_OpenCV.this,"No file have been found!",Toast.LENGTH_SHORT).show();
            }
        });
        Button del = (Button)findViewById(R.id.cv_Delete);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Delete())
                    Toast.makeText(Test_for_OpenCV.this,"No file have been found!",Toast.LENGTH_SHORT).show();
            }
        });
        Button save = (Button)findViewById(R.id.cv_save);
        save.setOnClickListener(new View.OnClickListener() {
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
                                    Save(FileName);
                                }
                            }
                        }).setNegativeButton("Cancel",null).create().show();
            }
        });

        Button begin = (Button)findViewById(R.id.cv_begin);
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = !start;
                if(start) {
                    MyAcceptThread.begin();
                }
                else{
                    MyAcceptThread.pause();
                }
            }
        });
        img_src = (SurfaceView)findViewById(R.id.cv_src);
        src_Holder = img_src.getHolder();
        img_res = (SurfaceView)findViewById(R.id.cv_res);
        res_Holder = img_res.getHolder();
        MyApp.setMyholder(src_Holder);
        MyApp.setDeal_holder(res_Holder);
        SeekBar[] mylist = new SeekBar[6];
        for(int iter = 0; iter < 6 ; iter++){
            final int index = iter;
            mylist[iter] = (SeekBar)findViewById(idlist[iter]);
            mylist[iter].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    HSVarr[index] = i;
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
