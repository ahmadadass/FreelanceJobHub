package com.ucas.freelancejobhub.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.ucas.freelancejobhub.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageUtils {
    private ImageUtils() {
    }

    public static Uri createCameraImageUri(Context context, String prefix) throws IOException {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory == null) {
            throw new IOException("External pictures directory is unavailable");
        }
        File image = File.createTempFile(prefix + "_", ".jpg", directory);
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                image
        );
    }

    public static String copyProfileImage(Context context, Uri source, long userId) throws IOException {
        File directory = new File(context.getFilesDir(), "profile_images");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create profile image directory");
        }

        File destination = new File(directory,
                "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg");

        try (InputStream input = context.getContentResolver().openInputStream(source);
             FileOutputStream output = new FileOutputStream(destination)) {
            if (input == null) {
                throw new IOException("Selected image could not be opened");
            }
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        }

        return Uri.fromFile(destination).toString();
    }

    public static void loadProfileImage(ImageView imageView, String uriValue) {
        if (uriValue == null || uriValue.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.ic_person);
            return;
        }
        try {
            imageView.setPadding(0, 0, 0, 0);
            imageView.setImageURI(null);
            imageView.setImageURI(Uri.parse(uriValue));
        } catch (RuntimeException exception) {
            imageView.setImageResource(R.drawable.ic_person);
        }
    }
}
