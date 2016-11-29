package com.example.brerlappin.sefmultitouch;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by BRERLAPPIN on 28/10/2016.
 */

public class ListFiles extends ListActivity {
    private List<String> dirEntries = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Intent intt = getIntent();
        File directory = new File(intt.getStringExtra("directory"));

        System.out.println("Checking if directory exists: \n"+directory);
        if(directory.isDirectory()){
            File[] files = directory.listFiles();

            //Sort in descending date order
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return -Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });

            //Fill list with files
            this.dirEntries.clear();
            for(File file : files){
                this.dirEntries.add(file.getPath());
            }

            ArrayAdapter<String> dirList = new ArrayAdapter<String>(this, R.layout.file_row, this.dirEntries);

            //Alphabetize entries
            //dirList.sort(null);

            this.setListAdapter(dirList);
        }else {
            System.out.println("ERROR, DIRECTORY NOT RECOGNIZED");
            System.out.println("Possible options:");
            System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
            System.out.println(Environment.getDataDirectory().getAbsolutePath());
            System.out.println(Environment.getDownloadCacheDirectory().getAbsolutePath());
            System.out.println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        }
    }

    @Override
    protected void onListItemClick(ListView lv, View v, int pos, long id){
        File clickedFile = new File(this.dirEntries.get(pos));
        Intent intt = getIntent();
        intt.putExtra("clickedFile", clickedFile.toString());
        setResult(RESULT_OK, intt);
        finish();
    }
}
