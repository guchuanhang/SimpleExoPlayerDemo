/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.demo.player;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.demo.EventLogger;
import com.google.android.exoplayer.demo.R;
import com.google.android.exoplayer.demo.Samples;
import com.google.android.exoplayer.demo.SmoothStreamingTestMediaDrmCallback;
import com.google.android.exoplayer.demo.WidevineTestMediaDrmCallback;
import com.google.android.exoplayer.demo.player.DemoPlayer.RendererBuilder;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.ApicFrame;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.util.Util;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

public class SimplePlayer implements
        DemoPlayer.Listener, DemoPlayer.Id3MetadataListener {
    public static final String TAG = SimplePlayer.class.getSimpleName();
    private static Context appContext;
    private static SimplePlayer servicePlayer;
    public static final String ACTION_PLAY_STUTUS = "play status";
    public static final String KEY_STATUS = "play status";
    public static final String KEY_SWITCH = "audio switch";
    public static final String KEY_IS_NEXT = "to next";
    public static final String KEY_START_PLAY = "start play";

    private SimplePlayer() {
        appContext = ExoApplication.getApplication();
    }

    public static SimplePlayer getInstance() {
        if (servicePlayer == null) {
            servicePlayer = new SimplePlayer();
        }
        return servicePlayer;
    }

    //        Samples.Sample sample = new Samples.Sample("", playAddress, Util.TYPE_OTHER);
    public void play(Samples.Sample sample) {
        if (sample != null) {
            if (this.contentUri != null) {
                //play same as before
                if (contentUri.toString().equals(sample.uri)) {
                    if (player == null) {
                        preparePlayer(true);
                    } else if (player.getPlaybackState() == ExoPlayer.STATE_IDLE) {
                        releasePlayer();
                        preparePlayer(true);
                    }
                    return;
                }
            }
            releasePlayer();
            if (this.contentUri != null && !contentUri.toString().equals(sample.uri)) {
                playerPosition = 0;
            }
            this.contentUri = Uri.parse(sample.uri);
            this.contentType = sample.type;
            contentId = sample.name.toLowerCase().replace("\\s", "");
            if (player == null) {
                preparePlayer(true);
            }
            sendPlayStatusBroadcast(true, false, false, true);
        }
    }


    //这个用于resume/pause切换
    public static final int TYPE_RESUME = 0;
    public static final int TYPE_LOAD = 1;
    public static final int TYPE_RELEASE = 2;
    public static final String ACTION = "action";

    // For use within demo app code.
    public static final String CONTENT_ID_EXTRA = "content_id";
    public static final String CONTENT_TYPE_EXTRA = "content_type";
    public static final String PROVIDER_EXTRA = "provider";

    // For use when launching the demo app using adb.
    private static final String CONTENT_EXT_EXTRA = "type";

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private EventLogger eventLogger;
    private DemoPlayer player;
    private boolean playerNeedsPrepare;

    private long playerPosition;
    private Uri contentUri;
    private int contentType;
    private String contentId;
    private String provider = "";

    public boolean isPlaying() {
        return player != null && (player.getPlaybackState() == ExoPlayer.STATE_BUFFERING ||
                player.getPlaybackState() == ExoPlayer.STATE_READY
                || player.getPlaybackState() == ExoPlayer.STATE_PREPARING);
    }

    public boolean isPlaying(String targetPlayingUrl) {
        if (isPlaying()) {
            if (contentUri != null && contentUri.toString().equals(targetPlayingUrl)) {
                return true;
            }
        }
        return false;
    }

    public double getProgress() {
        if (player != null) {
            return (player.getCurrentPosition() * 1.0 / player.getDuration());
        }
        return 0;
    }

    public void setProgress(double progress) {
        if (player == null) return;
        playerPosition = (long) (progress * player.getDuration());
        player.seekTo(playerPosition);
    }

    private RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(appContext, "ExoPlayerDemo");
        switch (contentType) {
            case Util.TYPE_SS:
                return new SmoothStreamingRendererBuilder(appContext, userAgent, contentUri.toString(),
                        new SmoothStreamingTestMediaDrmCallback());
            case Util.TYPE_DASH:
                return new DashRendererBuilder(appContext, userAgent, contentUri.toString(),
                        new WidevineTestMediaDrmCallback(contentId, provider));
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(appContext, userAgent, contentUri.toString());
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(appContext, userAgent, contentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);
            player.setMetadataListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setPlayWhenReady(playWhenReady);
    }

    public void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
        sendPlayStatusBroadcast(false, false, false, false);
    }

    public void sendPlayStatusBroadcast(boolean isPlaying, boolean toSwitch, boolean isNext, boolean startPlay) {
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAY_STUTUS);
        intent.putExtra(KEY_STATUS, isPlaying);
        intent.putExtra(KEY_SWITCH, toSwitch);
        intent.putExtra(KEY_IS_NEXT, isNext);
        intent.putExtra(KEY_START_PLAY, startPlay);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                sendPlayStatusBroadcast(true, false, false, false);
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                sendPlayStatusBroadcast(false, true, true, false);
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        Log.d(TAG, text);
    }

    @Override
    public void onError(Exception e) {
        String errorString = null;
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            errorString = appContext.getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
        } else if (e instanceof ExoPlaybackException
                && e.getCause() instanceof DecoderInitializationException) {
            // Special case for decoder initialization failures.
            DecoderInitializationException decoderInitializationException =
                    (DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
                    errorString = appContext.getString(R.string.error_querying_decoders);
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = appContext.getString(R.string.error_no_secure_decoder,
                            decoderInitializationException.mimeType);
                } else {
                    errorString = appContext.getString(R.string.error_no_decoder,
                            decoderInitializationException.mimeType);
                }
            } else {
                errorString = appContext.getString(R.string.error_instantiating_decoder,
                        decoderInitializationException.decoderName);
            }
        }
        if (errorString != null) {
            Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
    }

    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
        for (Id3Frame id3Frame : id3Frames) {
            if (id3Frame instanceof TxxxFrame) {
                TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id,
                        txxxFrame.description, txxxFrame.value));
            } else if (id3Frame instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
            } else if (id3Frame instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else if (id3Frame instanceof ApicFrame) {
                ApicFrame apicFrame = (ApicFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, description=%s",
                        apicFrame.id, apicFrame.mimeType, apicFrame.description));
            } else if (id3Frame instanceof TextInformationFrame) {
                TextInformationFrame textInformationFrame = (TextInformationFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s", textInformationFrame.id,
                        textInformationFrame.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
            }
        }
    }

    /**
     * Makes a best guess to infer the type from a media {@link Uri} and an optional overriding file
     * extension.
     *
     * @param uri           The {@link Uri} of the media.
     * @param fileExtension An overriding file extension.
     * @return The inferred type.
     */
    private static int inferContentType(Uri uri, String fileExtension) {
        String lastPathSegment = !TextUtils.isEmpty(fileExtension) ? "." + fileExtension
                : uri.getLastPathSegment();
        return Util.inferContentType(lastPathSegment);
    }


}
