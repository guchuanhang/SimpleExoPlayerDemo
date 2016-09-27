package com.hang.exoplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.hang.exoplayer.bean.Util;

import java.util.Arrays;
import java.util.List;

public class NetInfoFragment extends DialogFragment {
    public NetInfoFragment() {
        // Required empty public constructor
    }

    public static NetInfoFragment newInstance(Bundle bundle) {
        NetInfoFragment fragment = new NetInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("温馨提示");
        builder.setMessage("亲，当前为非WIFI网络，继续播放会消耗手机流量哦！");
        builder.setPositiveButton("继续播放", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Util.allowThis4GPlay();
                Intent intent = new Intent(getActivity(), PlayService.class);
                intent.putExtras(bundle);
                getActivity().startService(intent);
//                List<String> playingAddressList = Arrays.asList(new String[]{WelcomeActivity.muzhiLive});
//                PlayService.loadMedia(getActivity(), playingAddressList, 0);
            }
        });
        builder.setNegativeButton("取消", null);

        return builder.create();
    }
}
