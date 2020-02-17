package com.imsajib02.passwordmanager;

public class Items {

    int ImageView, index;
    String title, email, password, username, phone, recoverymail;

    public Items(int ImageView, int index, String title, String email, String password, String username,
                    String phone, String recoverymail)
    {
        this.ImageView = ImageView;
        this.index = index;
        this.title = title;
        this.email = email;
        this.password = password;
        this.username = username;
        this.phone = phone;
        this.recoverymail = recoverymail;
    }

    public int getImageView()
    {
        return ImageView;
    }

    public int getIndex()
    {
        return index;
    }

    public String getTitle()
    {
        return title;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPhone()
    {
        return phone;
    }

    public String getRecoverymail()
    {
        return recoverymail;
    }
}
