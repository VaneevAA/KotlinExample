package ru.skillbranch.kotlinexample


import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor (
        private val firstName: String,
        private val lastName: String?,
        email: String? = null,
        rawPhone: String? = null,
        meta: Map<String, Any>? = null)
       {

           var userInfo: String = ""
           private val fullName: String
            get() = listOfNotNull(firstName,lastName).joinToString (" ") .capitalize()
           val initials: String
           get() = listOfNotNull(firstName,lastName)
                   .map { it.first().toUpperCase() }
                   .joinToString (" ")
           internal var phone: String? = null
           set(value) {
               field = value?.replace("[^+\\d]".toRegex(),"")
           }
           private var _login :String? = null
           internal var login: String
           set(value)  {
               _login = value?.toLowerCase()
           }
           get() = _login!!

            init {
                println("First init block, primary constuctor was called $email")

                check(!firstName.isBlank()) {"FirstName must be not blank"}
                check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) {"Email or phone must be not blank"}

                phone = rawPhone
                login = email ?: phone!!

                userInfo = """
                    firstName: $firstName
                    lastName: $lastName
                    login: $login
                    initials : $initials
                    email: $email
                    phone: $phone
                    meta: $meta""".trimIndent()

                
            }

           fun checkPaasword(pass:String) = encrypt(pass) == passwordHash

           fun changePassword(oldPass:String, newPass : String){
               if(checkPaasword(oldPass)) passwordHash = encrypt(newPass)
               else throw IllegalArgumentException("Wrong pass")
           }

           private val salt: String by lazy {
               ByteArray(16).also {
                   SecureRandom().nextBytes(it)
               }.toString()
           }
           private lateinit var passwordHash: String
           @VisibleForTesting(otherwise = VisibleForTesting.NONE)
           var accessCode: String? = null

           constructor(
                   firstName: String,
                   lastName: String?,
                   email: String?,
                   password:String
           ): this(firstName,lastName,email = email,meta = mapOf("auth" to "password")){
               println("Secondary mail constructor")
               passwordHash = encrypt(password)

           }

           constructor(
                   firstName: String,
                   lastName: String?,
                   rawPhone: String?
           ):this(firstName,lastName,rawPhone = rawPhone,meta = mapOf("auth" to "sms")){
               println("Secondary phone constructor")
               val code = generateAccessCode()
               accessCode = code

           }

           private fun String.md5 () : String{
               val md:MessageDigest = MessageDigest.getInstance("MD5")
               val digest:ByteArray = md.digest(toByteArray())
               val hexString: String = BigInteger(1, digest).toString(16)
               return hexString.padStart(32,'0')
           }

           private fun encrypt(password: String) : String = salt.plus(password).md5()

           private fun generateAccessCode():String{
               val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghigklmnopqrstuvwxyz0123456789"

               return StringBuilder().apply {
                   repeat(6){
                       (possible.indices).random().also {
                           index -> append(possible[index])
                       }
                   }
               }.toString()
           }

           fun sendAccessCode(phone:String?,code:String){
                println("..... sending access code: $code on $phone")
           }

           companion object Factory {
               fun makeUser(fullName: String,
                            email:String? = null,
                            password: String? = null,
                            phone: String? = null) : User{
                   val (firstName, lastName) = fullName.fullNameToPair()
                   return when {
                       !phone.isNullOrBlank() -> User(firstName,lastName,rawPhone = phone)
                       !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName,lastName,email = email,password = password)
                       else -> throw IllegalArgumentException("Email or phone must be")
                   }
               }

               private fun String.fullNameToPair(): Pair<String,String?>{
                   return this.split(" ")
                           .filter { it.isNotBlank() }
                           .run {
                               when (size){
                                   1 -> this.first() to null
                                   2 -> first() to last()
                                   else -> throw IllegalArgumentException("No first and last name")
                               }
                           }
               }
           }


}