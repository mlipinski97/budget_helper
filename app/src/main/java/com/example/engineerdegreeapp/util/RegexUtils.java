package com.example.engineerdegreeapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    /*
    Password requires at least
    1 lower case character, 1 upper case character, 1 number, 1 special character and must be at least 6 characters and at most 20
     */
    static Pattern passwordPattern = Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{6,20})");
    static Pattern mailUsernamePattern = Pattern
            .compile("[a-z0-9]+([-+._][a-z0-9]+){0,2}@.*?(\\.(a(?:[cdefgilmnoqrstuwxz]|ero|(?:rp|si)a)|b(?:[abdefghijmnorstvwyz]iz)|c(?:[acdfghiklmnoruvxyz]|at|o(?:m|op))|d[ejkmoz]|e(?:[ceghrstu]|du)|f[ijkmor]|g(?:[abdefghilmnpqrstuwy]|ov)|h[kmnrtu]|i(?:[delmnoqrst]|n(?:fo|t))|j(?:[emop]|obs)|k[eghimnprwyz]|l[abcikrstuvy]|m(?:[acdeghklmnopqrstuvwxyz]|il|obi|useum)|n(?:[acefgilopruz]|ame|et)|o(?:m|rg)|p(?:[aefghklmnrstwy]|ro)|qa|r[eosuw]|s[abcdeghijklmnortuvyz]|t(?:[cdfghjklmnoprtvwz]|(?:rav)?el)|u[agkmsyz]|v[aceginu]|w[fs]|y[etu]|z[amw])\\b){1,2}");

    public static boolean isPasswordRegexSafe(String password){
        Matcher matcher = passwordPattern.matcher(password);
        return matcher.matches();
    }
    public static boolean isMailUsernameRegexSafe(String mailUsername){
        Matcher matcher = mailUsernamePattern.matcher(mailUsername);
        return matcher.matches();
    }

    static Pattern moneyAmountPattern = Pattern.compile("^[\\d]+[\\.\\,][\\d]{2}$");
    public static boolean isMoneyAmountRegexSafe(String moneyAmount){
        if(!moneyAmount.contains(",") && !moneyAmount.contains(".")){
            moneyAmount += ".00";
        }
        Matcher matcher = moneyAmountPattern.matcher(moneyAmount);
        return matcher.matches();
    }

}
