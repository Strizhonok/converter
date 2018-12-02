package com.strizhonok.sportdiary;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;


public class TrainingFragment extends Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        TabHost tabHost = getView().findViewById(R.id.training_tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.training_tab1);
        tabSpec.setIndicator(R.string.nav_menu_training_tab1);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.training_tab2);
        tabSpec.setIndicator("Кошка");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag3");
        tabSpec.setContent(R.id.training_tab3);
        tabSpec.setIndicator("Котёнок");
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);

        return inflater.inflate(R.layout.fragment_training, container, false);
    }
}
