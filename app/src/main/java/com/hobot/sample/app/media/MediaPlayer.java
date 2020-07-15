package com.hobot.sample.app.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Observable;
import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;

import java.lang.ref.WeakReference;

/**
 * 语音播报
 *
 * @author Hobot
 */
public class MediaPlayer {

    private android.media.MediaPlayer mMediaPlayer;
    private WeakReference<Context> mContext;
    private int mCurrentId;

    private CompletionPlayObservable mObservable;
    private CompletionPlayWithIdObservable mWithIdObservable;
    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

        @Override
        public void onPrepared(android.media.MediaPlayer mp) {
            // 装载完毕 开始播放流媒体
            mMediaPlayer.start();
        }
    };
    private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(android.media.MediaPlayer mp) {
            if (mObservable != null) {
                mObservable.onCompletionPlayListener();
            }

            if (mWithIdObservable != null) {
                mWithIdObservable.onCompletionPlayWithId(mCurrentId);
            }
        }
    };

    public MediaPlayer(Context context) {
        mObservable = new CompletionPlayObservable();
        mWithIdObservable = new CompletionPlayWithIdObservable();
        mContext = new WeakReference<>(context);
        mMediaPlayer = new android.media.MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
    }

    /**
     * 播放语音
     *
     * @param mediaId 媒体ID
     */
    public void play(int mediaId) {
        if (isPlaying()) {
            if (mediaId == mCurrentId) {
                return;
            }
            mWithIdObservable.onCompletionPlayWithId(mCurrentId);
        }
        try {
            // 设置指定的流媒体地址
            mCurrentId = mediaId;
            mMediaPlayer.reset();
            AssetFileDescriptor fileDescriptor = mContext.get().getResources().openRawResourceFd(mediaId);
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());

            // 设置音频流的类型
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            // 通过异步的方式装载媒体资源
            mMediaPlayer.prepareAsync();
            fileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCurrentId() {
        return mCurrentId;
    }

    /**
     * 当前是否正在播放。
     *
     * @return 当前播放状态
     */
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    /**
     * 暂停播放。
     */
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    /**
     * 停止播放
     */
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mOnCompletionListener = null;
        mOnPreparedListener = null;
        if (mContext != null) {
            mContext.clear();
            mContext = null;
        }
        if (mObservable != null) {
            mObservable.unregisterAll();
            mObservable = null;
        }
        if (mWithIdObservable != null) {
            mWithIdObservable.unregisterAll();
            mWithIdObservable = null;
        }
    }

    /**
     * 注册播放结束接听
     *
     * @param listener 监听
     */
    public void registerCompletionPlayListener(OnCompletionPlayListener listener) {
        if (mObservable != null) {
            mObservable.registerObserver(listener);
        }
    }

    /**
     * 解注册播放结束接听
     *
     * @param listener 监听
     */
    public void unregisterCompletionPlayListener(OnCompletionPlayListener listener) {
        if (mObservable != null) {
            mObservable.unregisterObserver(listener);
        }
    }

    /**
     * 注册播放结束接听
     *
     * @param listener 监听
     */
    public void registerCompletionPlayWithIdListener(OnCompletionPlayWithIdListener listener) {
        if (mWithIdObservable != null) {
            mWithIdObservable.registerObserver(listener);
        }
    }

    /**
     * 解注册播放结束接听
     *
     * @param listener 监听
     */
    public void unregisterCompletionPlayWithIdListener(OnCompletionPlayWithIdListener listener) {
        if (mWithIdObservable != null) {
            mWithIdObservable.unregisterObserver(listener);
        }
    }

    /**
     * 播放结束监听
     */
    public interface OnCompletionPlayListener {
        void onCompletionPlayListener();
    }

    /**
     * 带播放id的播放结束监听
     */
    public interface OnCompletionPlayWithIdListener {
        void onCompletionPlayWithId(int id);
    }

    private class CompletionPlayObservable extends Observable<OnCompletionPlayListener> implements OnCompletionPlayListener {

        @Override
        public void registerObserver(OnCompletionPlayListener observer) {
            if (!mObservers.contains(observer)) {
                super.registerObserver(observer);
            }
        }

        @Override
        public void unregisterObserver(OnCompletionPlayListener observer) {
            synchronized (mObservers) {
                if (mObservers.contains(observer)) {
                    super.unregisterObserver(observer);
                }
            }
        }

        @Override
        public void onCompletionPlayListener() {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); i++) {
                    mObservers.get(i).onCompletionPlayListener();
                }
            }
        }
    }

    private class CompletionPlayWithIdObservable extends Observable<OnCompletionPlayWithIdListener> implements OnCompletionPlayWithIdListener {
        @Override
        public void onCompletionPlayWithId(int id) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); i++) {
                    mObservers.get(i).onCompletionPlayWithId(id);
                }
            }
        }
    }
}
