package com.lihan.automaticsendmail

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.activation.DataHandler
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class GMailSender private constructor(
    private val senderMail : String,
    private  val senderPassword : String,
    private val receiverMail : String,
    private val subject : String,
    private val body : String ,
    private val onSuccessCallBack : (String) -> Unit ,
    private val onFailCallBack : (String) -> Unit ,
){
    private lateinit var session: Session
    private val _multipart: Multipart = MimeMultipart()
    init {
        initSession()
    }
    class Builder(
        private var senderMail : String ,
        private var senderPassword : String ,
        private var receiverMail : String ,
        private var subject : String ,
        private var body : String ,
        private var onSuccessCallBack : (String) -> Unit ,
        private var onFailCallBack : (String) -> Unit ,
    ){
        fun setSenderMail(senderMail: String): Builder {
            this.senderMail = senderMail
            return this
        }

        fun setSenderPassword(senderPassword: String): Builder {
            this.senderPassword = senderPassword
            return this
        }

        fun setReceiverMail(receiverMail: String): Builder {
            this.receiverMail = receiverMail
            return this
        }

        fun setSubject(subject: String): Builder {
            this.subject = subject
            return this
        }

        fun setBody(body: String): Builder {
            this.body = body
            return this
        }

        fun setOnSuccessCallBack(onSuccessCallBack: (String) -> Unit) : Builder{
            this.onSuccessCallBack = onSuccessCallBack
            return this
        }

        fun setOnFailCallBack(onFailCallBack: (String) -> Unit) : Builder{
            this.onFailCallBack = onFailCallBack
            return this
        }

        fun build() = GMailSender(
            senderMail,
            senderPassword,
            receiverMail,
            subject,
            body,
            onSuccessCallBack,
            onFailCallBack
        )



    }
    private fun initSession(){
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.host", "smtp.gmail.com")
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = "false"
        props.setProperty("mail.smtp.quitwait", "false")
        session = Session.getDefaultInstance(props, object : Authenticator(){
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderMail, senderPassword)
            }
        })
    }

    fun send(){
        CoroutineScope(Dispatchers.IO).launch {
        try {
            val message = MimeMessage(session)
            val handler = DataHandler(ByteArrayDataSource(body.toByteArray(), "text/plain"))
            message.sender = InternetAddress(senderMail)
            message.subject = subject
            message.dataHandler = handler
            val messageBodyPart: BodyPart = MimeBodyPart()
            messageBodyPart.setText(body)
            _multipart.addBodyPart(messageBodyPart)
            message.setContent(_multipart)
            if (receiverMail.indexOf(',') > 0) message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(receiverMail)
            ) else message.setRecipient(Message.RecipientType.TO, InternetAddress(receiverMail))
            Transport.send(message)
            onSuccessCallBack("$subject send Succeed")
        } catch (e: Exception) {
            e.printStackTrace()
            onFailCallBack("Error : ${e.message}")
        }
        }
    }

}