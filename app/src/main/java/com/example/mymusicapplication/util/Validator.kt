package com.example.mymusicapplication.util

class Validator {
    fun isValidPhoneNumber(phoneNumber: String): Boolean{
        return phoneNumber.matches(Regex("^[6-9][0-9]{9}$"))
    }

    fun isValidEmail(email: String): Boolean{
        return email.matches(Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+[.com]{1,30}\$"))
    }

    fun isStrongPwd(password: String): Boolean{
        return password.matches(Regex("^(?=.*[A-Z])(?=.*[!@#$%&*])(?=.*[0-9])(?=.*[a-z)]).{8,16}$"))
    }

    fun isValidName(name: String): Boolean{
        return name.matches(Regex("^[a-zA-Z]{1,30}\$"))
    }
}