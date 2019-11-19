package me.phil.madcampus.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import me.phil.madcampus.MainActivity;
import me.phil.madcampus.R;
import me.phil.madcampus.model.User;
import me.phil.madcampus.shared.DummyApi;

import static android.app.Activity.RESULT_OK;

/** Account settings for password,auto-login,avatar **/
public class SecurityFragment extends Fragment {
    public static final int SELECT_PHOTO=12345;
    private EditText oldPwd,newPwd,newConfirm;
    private TextView txtUser;
    private ImageView imgAvatar;
    private User user;
    public boolean avatarChanged=false;
    private boolean confirmed=false;
    private DummyApi api;
    Context context;

    public SecurityFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public SecurityFragment(Context context,DummyApi api) {
        this.context=context;
        this.api=api;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_security, container, false);
        user=getArguments().getParcelable("user");
        /**load current avatar**/
        if(user.avatar!=null)
        {
            Bitmap avatar=MainActivity.getScaledBitmap(user.avatar);
            ImageView myImage =view.findViewById(R.id.img_avatar);
            myImage.setImageBitmap(avatar);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        oldPwd=view.findViewById(R.id.edit_oldpwd);
        newPwd=view.findViewById(R.id.edit_newpwd2);
        newConfirm=view.findViewById(R.id.edit_newpwdconfirm);
        txtUser=view.findViewById(R.id.txt_title);
        imgAvatar=view.findViewById(R.id.img_avatar);

       // user=getArguments().getParcelable("user");
        txtUser.setText(user.name+"\n"+user.studies);

        Button btnConfirm=view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(btn->confirmChanges());
        /**Image chooser**/
        imgAvatar.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        });
        Button btnDeleteAvatar= view.findViewById(R.id.btn_delete_avatar);

        btnDeleteAvatar.setOnClickListener(v->
                MainActivity.showConfirmDialog(context, R.string.clearavatar, R.string.txtclearavatar,
                        (dialog, which) -> changeAvatar(null)));

        getActivity().setTitle(getString(R.string.titlesecurity));
    }
    /**Image chooser result**/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
            /**Select chosen image**/
            Uri pickedImage = data.getData();
            changeAvatar(pickedImage);
        }
    }
    private void changeAvatar(Uri path){
        CircleImageView avatarImg = imgAvatar.findViewById(R.id.img_avatar);
        avatarChanged=true;
        if(path==null){
            avatarImg.setImageDrawable(getResources().getDrawable(R.drawable.round_image_view));
            user.avatar=null;
            return;
        }
        String[] filePath = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(path, filePath, null, null, null);
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
        cursor.close();
        Bitmap newAvatar= MainActivity.getScaledBitmap(imagePath);
        avatarImg.setImageBitmap(newAvatar);
        avatarImg.setVisibility(View.VISIBLE);
        user.avatar=imagePath;

    }
    private void confirmChanges() {
        /**Save changes only if old password was entered**/
        // api=new DummyApi(getContext());
        if(api.validateLogin(user.name,oldPwd.getText().toString())!=null){
            /**Change password**/
            if(newPwd.getText().length()!=0)
                /** Check password confirmation, correct pattern, length **/
                if(newPwd.getText().toString().equals(newConfirm.getText().toString()))
                {
                    if(newPwd.getText().length()>5 && newPwd.getText().toString().matches("([a-zA-Z]+[0-9]+.*)"))
                        if(api.changePassword(user.name,newPwd.getText().toString())) {
                            Toast.makeText(getActivity(), R.string.updatepwd, Toast.LENGTH_SHORT).show();
                            user.passwd=newPwd.getText().toString();
                        }
                        else
                            Toast.makeText(getActivity(), R.string.notupdatepwd, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), R.string.passwd_length, Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getActivity(), R.string.passwd_confirm, Toast.LENGTH_SHORT).show();
            /**Change avatar **/
            if(avatarChanged){
                if(api.changeAvatar(user.name,user.avatar)) {
                    Toast.makeText(getActivity(), R.string.updateav, Toast.LENGTH_SHORT).show();

                }
                else
                    Toast.makeText(getActivity(), R.string.notupdateav, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getActivity(), R.string.savechg, Toast.LENGTH_SHORT).show();
            confirmed=true;
        }
        else{
            Toast.makeText(getActivity(), R.string.confirmpwd, Toast.LENGTH_SHORT).show();
            confirmed=false;
        }
        /**Update user object for current session**/
        Bundle bundle = new Bundle();
        bundle.putBoolean("confirmed", confirmed);
        bundle.putParcelable("user", user);
        setArguments(bundle);
    }

}
