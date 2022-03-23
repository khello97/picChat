package se.umu.cs.khalil.picchat;

public class User {

    private String mUserName, mFullName;

    public User(){

    }

    public User( String UserName, String FullName){

        this.mUserName = UserName;
        this.mFullName = FullName;


    }

    public String getFullName() {
        return mFullName;
    }


    public void setFullName(String fullName) {
        mFullName = fullName;
    }


    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }
}
