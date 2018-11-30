package com.example.helloworld;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class CameraAlbumTest extends AppCompatActivity {
    public static final int TAKE_PHOTO=1;
    public static final int CHOOSE_PHOTO =2;

    private ImageView picture;
    private Uri imageUri;
    private ImageView picture2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_album_test);

        //测试
        Bmob.initialize(this,"5397552e87a1b470c8d5cec858c08d99");


        Button takePhoto=(Button) findViewById(R.id.take_photo);
        Button chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);
        Button loadingpicture=(Button) findViewById(R.id.loading);


        picture=(ImageView)findViewById(R.id.picture);
        picture2=(ImageView)findViewById(R.id.picture2);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建File,用于存储拍照后的图片
                File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT >= 24){
                    imageUri = FileProvider.getUriForFile(CameraAlbumTest.this,
                            "com.example.helloworld.fileprovider",outputImage);
                }else {
                    imageUri = Uri.fromFile(outputImage);
                }

                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });

        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(CameraAlbumTest.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(CameraAlbumTest.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    Log.d("ssSS","skdfjksdfjk");
                }else{
                    openAlbum();
                }
            }
        });

        loadingpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            BmobQuery<Person> query=new BmobQuery<>();
            query.findObjects(new FindListener<Person>() {
                @Override
                public void done(List<Person> list, BmobException e) {
                    if(e==null){
                        show_ad(list);
                    }else{
                        Toast.makeText(CameraAlbumTest.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });
            }
        });
    }
    public void show_ad(List<Person> list){
        final Person person=list.get(0);
        final BmobFile icon=person.getAddimage();
        icon.download(new DownloadFileListener() {
            @Override
            public void done(String s, BmobException e) {
                if(e==null){
                    picture2.setImageBitmap(BitmapFactory.decodeFile(s));
                }
            }

            @Override
            public void onProgress(Integer integer, long l) {

            }
        });
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
            switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch(requestCode){
            case TAKE_PHOTO:
                if(resultCode==RESULT_OK){
                    try{
                        //将拍摄的照片显示出来

                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Log.d("TAG",imageUri.toString());
                        picture.setImageBitmap(bitmap);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    if(Build.VERSION.SDK_INT >= 19){
                        handleImageOnKitKat(data);
                        Log.d("TAG","XXXXXXXxxx");
                    }
                    else{
                        handleImageBeforeKitKat(data);
                        Log.d("TAG","YYYYYYYYYYY");
                    }
                }
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath=null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId  = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];
                String selection=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }

        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath=getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath=uri.getPath();
        }
        displayImage(imagePath);
    }


    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        displayImage(imagePath);
    }
    private String getImagePath(Uri uri,String selection){
        String path=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){
        if(imagePath != null){
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
            AddImage(imagePath);

        }else{
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

    private void EnsureImage(String name){
        final String username=name;

        BmobQuery<Person> personBmobQuery = new BmobQuery<>();
        personBmobQuery.order("-createdAt");//按时间排序
        personBmobQuery.findObjects(new FindListener<Person>() {
            @Override
            public void done(List<Person> lists, BmobException e) {
                for (Person list : lists) {
                    if (username.equals(list.getName())) {
                        list.getObjectId();
                        list.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e==null){
                                    showToast("成功删除上一条数据");
                                }else{
                                    showToast("删除失败");
                                }
                            }
                        });
                        return;
                    } else {
                        showToast("新的人，新的图片");
                    }
                }
            }
        });
    }

    private void AddImage(String imagePath){
        //添加到后台
        final Person person=new Person();
        final String icon;
        icon=imagePath;
        final BmobFile bmobFile=new BmobFile(new File(icon));
        person.setName("地球");
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    person.setAddimage(bmobFile);
                    EnsureImage(person.getName());
                    person.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if(e==null){
                                showToast("添加数据成功");
                            }else{
                                showToast("添加数据失败");
                            }
                        }
                    });

                }
            }
        });
    }

}
