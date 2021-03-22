package com.extrastudios.docscanner.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import com.extrastudios.docscanner.interfaces.OnPdfReorderedInterface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class ReorderPdfPagesAsync extends AsyncTask<String, String, ArrayList<Bitmap>> {

    private final Uri mUri;
    private final String mPath;
    private final OnPdfReorderedInterface mOnPdfReorderedInterface;
    private final Activity mActivity;

    /**
     * @param uri                     Uri of the pdf
     * @param path                    Absolute path of the pdf
     * @param onPdfReorderedInterface interface to update  pdf reorder progress
     * @param activity                Its needed to get the current context
     */

    public ReorderPdfPagesAsync(Uri uri,
                                String path,
                                Activity activity,
                                OnPdfReorderedInterface onPdfReorderedInterface) {
        this.mUri = uri;
        this.mPath = path;
        this.mOnPdfReorderedInterface = onPdfReorderedInterface;
        this.mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mOnPdfReorderedInterface.onPdfReorderStarted();
    }

    @Override
    protected ArrayList<Bitmap> doInBackground(String... strings) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        ParcelFileDescriptor fileDescriptor = null;
        try {
            if (mUri != null)
                fileDescriptor = mActivity.getContentResolver().openFileDescriptor(mUri, "r");
            else if (mPath != null)
                fileDescriptor = ParcelFileDescriptor.open(new File(mPath), MODE_READ_ONLY);
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                bitmaps = getBitmaps(renderer);
                // close the renderer
                renderer.close();
            }
        } catch (IOException | SecurityException | IllegalArgumentException | OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmaps;
    }

    /**
     * Get list of Bitmaps from PdfRenderer
     *
     * @param renderer
     * @return
     */
    private ArrayList<Bitmap> getBitmaps(PdfRenderer renderer) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        final int pageCount = renderer.getPageCount();
        for (int i = 0; i < pageCount; i++) {
            PdfRenderer.Page page = renderer.openPage(i);
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                    Bitmap.Config.ARGB_8888);
            // say we render for showing on the screen
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            // do stuff with the bitmap
            bitmaps.add(bitmap);
            // close the page
            page.close();
        }
        return bitmaps;
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
        super.onPostExecute(bitmaps);
        if (bitmaps != null && !bitmaps.isEmpty()) {
            mOnPdfReorderedInterface.onPdfReorderCompleted(bitmaps);
        } else {
            mOnPdfReorderedInterface.onPdfReorderFailed();
        }
    }
}