package se.umu.cs.khalil.picchat;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ShowMessageActivity extends AppCompatActivity {

    private ImageView mShowImage;
    private byte[] ImageBytes;

    //När användaren öppnar ett meddelande kommer denna aktivitet att startas där bilden visas i fullskärm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);

        mShowImage = (ImageView) findViewById(R.id.show_imageView);

        //hämtar bilden och sparar den i type byte[]
        ImageBytes = getIntent().getByteArrayExtra("data");
        //decode:ar byte[] till en Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(ImageBytes,0, ImageBytes.length);

        //lägger till bitmap i imageview som i sin tur visas för användaren
        mShowImage.setImageBitmap(bitmap);

        //Om användaren klickar på bilden kommer enheten att lämna denna aktivitet och gå tillbaka till föregående
        //aktivitet, som är loggedIn-activity där FriendListFragment visas
        mShowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}