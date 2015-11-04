package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.rael.daniel.drc.R;


public class ImageFragment extends Fragment {
    ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate basic layout
        View v = inflater.inflate(R.layout.imgur_image_layout
                , container
                , false);
        this.imageView = (ImageView)v.findViewById(R.id.imgur_image);
        return v;
    }

    public ImageFragment() {
        super();
    }

    public static Fragment newInstance(){
        ImageFragment cf=new ImageFragment();
        return cf;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
