package ru.skillbranch.kotlinexample

import kotlin.random.Random

object UserHolder {

    private val map = mutableMapOf<String,User>()

    fun registerUser( fullName : String, email:String,password:String) : User {
        return User.makeUser(fullName,email = email,password = password)
                .also {
                    user -> map[user.login] = user
                }
    }

    fun loginUser (login: String , password: String) : String?{
        return map[login.trim()]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun clearHolder(){
        map.clear()
    }

    fun registerUserByPhone (fullName: String,phone:String) : User{
        return User.makeUser(fullName,phone = phone)
                .also {
                    user -> map[phone] = user
                }
    }

    fun requestAccessCode(phone: String):String{
        val rnd = Random(1)
        val password = rnd.nextInt().toString() + rnd.nextInt().toString() + rnd.nextInt().toString() + rnd.nextInt().toString()
        map[phone]?.sendAccessCode(phone,password)
        return password
    }
}