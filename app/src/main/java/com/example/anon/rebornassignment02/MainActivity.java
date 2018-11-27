package com.example.anon.rebornassignment02;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    MyRecyclerViewAdapter myRecyclerViewAdapter;
    ArrayList<MyData> myDatas = new ArrayList<>();
    ImageView mainImageView;
    Uri uriMainImage = null;
    Button btnChooseMain;
    Button btnRemoveMain;
    Button btnAdd;
    Button btnShowClosestPic;
    ImageView closestImageView;
    int SELECT_IMAGE_FOR_MAIN_PIC = 6996;
    int SELECT_IMAGE_FOR_RECYCLER_VIEW = 9969;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up id
        recyclerView = findViewById(R.id.recyclerView);
        mainImageView = findViewById(R.id.mainImageView);
        btnChooseMain = findViewById(R.id.btnChooseMain);
        btnRemoveMain = findViewById(R.id.btnRemoveMain);
        btnAdd = findViewById(R.id.btnAdd);
        btnShowClosestPic = findViewById(R.id.btnShowClosestPic);
        closestImageView = findViewById(R.id.closestImageView);

        // set up for main picture
        setUpButtonMainPic();

        // use recycler view
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 4);
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(myDatas, R.layout.item_layout);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(myRecyclerViewAdapter);

        // button below recycler view
        setUpButtonAdd();
        setUpButtonShowClosestPic();
    }

    private void setUpButtonMainPic() {
        btnChooseMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(
                            intent,
                            SELECT_IMAGE_FOR_MAIN_PIC);
                }

            }
        });

        btnRemoveMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // https://stackoverflow.com/questions/2859212/how-to-clear-an-imageview-in-android
                mainImageView.setImageResource(android.R.color.transparent);
                uriMainImage = null;
            }
        });
    }

    private void setUpButtonShowClosestPic() {
        btnShowClosestPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uriClosestPic = getUriClosestPic();

                if (uriClosestPic != null) {
                    closestImageView.setImageURI(uriClosestPic);
                } else {
                    closestImageView.setImageResource(android.R.color.transparent);
                }
            }
        });
    }


    // https://en.wikipedia.org/wiki/Color_difference
    private float distanceBetween(byte[] bytes_1, byte[] bytes_2) {
        float distance = 0;

        // bytes_1 and bytes_2 must be equal length
        // cause we scale bitmap
        for (int i = 0; i < bytes_1.length; i += 3) {
            distance += (float) Math.sqrt(Math.pow(bytes_1[i] - bytes_2[i], 2)
                    + Math.pow(bytes_1[i + 1] - bytes_2[i + 1], 2)
                    + Math.pow(bytes_1[i + 2] - bytes_2[i + 2], 2));
        }
        return distance;

    }

    private byte[] getRGBFromBitmap(Bitmap bitmap) {
        if (bitmap == null)
            return null;

        bitmap = Bitmap.createScaledBitmap(bitmap,
                100,
                100,
                false
        );

        int totalPixels = bitmap.getWidth() * bitmap.getHeight();
        int totalColors = 3; // RGB

        byte[] RGB = new byte[totalPixels * totalColors];
        int[] pixels = new int[totalPixels];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(),
                0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < totalPixels; ++i) {
            RGB[i * totalColors] = (byte) Color.red(pixels[i]);
            RGB[i * totalColors + 1] = (byte) Color.green(pixels[i]);
            RGB[i * totalColors + 2] = (byte) Color.blue(pixels[i]);
        }

        return RGB;
    }

    private Uri getUriClosestPic() {
        // chua chon main image, hoac chua co gallery de compare
        if (uriMainImage == null || myDatas.size() == 0) {
            return null;
        }

        Bitmap mainBitmap = null;
        try {
            mainBitmap = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(),
                    uriMainImage);
        } catch (Exception e) {
            Log.e("Bitmap", e.toString());
        }

        // make sure main bitmap never null
        if (mainBitmap == null) {
            return null;
        }

        // luu khoang cach RGB cua anh trong gallery den main pic
        float[] distances = new float[myDatas.size()];
        int index = 0;

        for (MyData myData : myDatas) {
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        myData.getUri()
                );
                distances[index++] =
                        distanceBetween(
                                getRGBFromBitmap(mainBitmap),
                                getRGBFromBitmap(bitmap));
            } catch (Exception e) {
                Log.e("Bitmap", e.toString());
                distances[index++] = -1;
            }
        }

        // lay ra vi tri anh co distance voi main pic nho nhat
        int smallestIndex = 0;
        for (int i = 1; i < index; ++i) {
            if (distances[i] != -1 && distances[i] < distances[smallestIndex]) {
                smallestIndex = i;
            }
        }

        // de phong truong hop -1
        if (distances[smallestIndex] == -1) {
            return null;
        }

        return myDatas.get(smallestIndex).getUri();

    }

    private void setUpButtonAdd() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // https://developer.android.com/guide/components/intents-common#Storage
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(
                            intent,
                            SELECT_IMAGE_FOR_RECYCLER_VIEW);

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_FOR_MAIN_PIC) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        mainImageView.setImageURI(data.getData());
                        uriMainImage = data.getData();
                    } catch (Exception e) {
                        Log.e("Intent", e.toString());
                    }
                }
            }
        } else if (requestCode == SELECT_IMAGE_FOR_RECYCLER_VIEW) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        // them Uri cua anh duoc chon trong gallery
                        myDatas.add(new MyData(data.getData()));
                        myRecyclerViewAdapter.notifyItemInserted(myDatas.size() - 1);
                    } catch (Exception e) {
                        Log.e("Intent", e.toString());
                    }
                }
            }
        }
    }
}
