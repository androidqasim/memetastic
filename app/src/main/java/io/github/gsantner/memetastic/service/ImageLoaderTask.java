/*
 * MemeTastic by Gregor Santner (http://gsantner.net)
 * Copyright (C) 2016-2018
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.gsantner.memetastic.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import java.io.File;

import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.data.MemeLibConfig;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.ContextUtils;

public class ImageLoaderTask<T> extends AsyncTask<File, Void, Bitmap> {
    private final static int MAX_DIMENSION = 5000;

    public interface OnImageLoadedListener<T> {
        void onImageLoaded(Bitmap bitmap, T callbackParam);
    }

    private final Context _context;
    private final int _maxSize;
    private final OnImageLoadedListener _listener;
    private final T _callbackParam;
    private final boolean _loadThumbnail;

    public ImageLoaderTask(OnImageLoadedListener listener, Context context, boolean loadThumbnail, T callbackParam) {
        _listener = listener;
        _context = context;
        _callbackParam = callbackParam;
        _loadThumbnail = loadThumbnail;
        _maxSize = loadThumbnail ? AppSettings.get().getThumbnailQualityReal()
                : MemeLibConfig.MEME_FULLSCREEN_MAX_IMAGESIZE;
    }

    private Bitmap loadStorageImage(File pathToImage) {
        File cacheFile = new File(_context.getCacheDir(), pathToImage.getAbsolutePath().substring(1));
        ContextUtils cu = ContextUtils.get();
        Bitmap bitmap;
        try {
            if (_loadThumbnail) {
                if (cacheFile.exists()) {
                    bitmap = cu.loadImageFromFilesystem(cacheFile, _maxSize);
                } else {
                    bitmap = cu.loadImageFromFilesystem(pathToImage, _maxSize);
                    cu.writeImageToFileDetectFormat(cacheFile, bitmap, 65);
                }
            } else {
                bitmap = cu.loadImageFromFilesystem(pathToImage, _maxSize);
            }
        } catch (NullPointerException nul) {
            bitmap = cu.drawableToBitmap(ContextCompat.getDrawable(_context, R.drawable.ic_mood_bad_black_256dp));
        }

        return bitmap;
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        return loadStorageImage(params[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (_listener != null)
            _listener.onImageLoaded(bitmap, _callbackParam);
    }
}
