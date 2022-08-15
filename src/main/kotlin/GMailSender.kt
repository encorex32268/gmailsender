import java.util.*
import javax.activation.DataHandler
import javax.mail.*
import javax.mail.event.TransportEvent
import javax.mail.event.TransportListener
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

class GMailSender private constructor(
    private var senderMail : String,
    private var senderPassword : String,
    private val receiverMail : String,
    private val subject : String,
    private val body : String ,
    private val onSuccessCallBack : (String) -> Unit ,
    private val onFailCallBack : (String) -> Unit
){
    private var session: Session? =null
    private lateinit var transport: Transport
    private val _multipart: Multipart = MimeMultipart()

    class Builder(
        private var senderMail : String = "",
        private var senderPassword : String = "" ,
        private var receiverMail : String = "",
        private var subject : String  = "",
        private var body : String  = "",
        private var onSuccessCallBack : (String) -> Unit ={},
        private var onFailCallBack : (String) -> Unit ={}
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

        if (session!=null){
            session = null
        }
        session = Session.getDefaultInstance(props, object : Authenticator(){
            override fun getPasswordAuthentication(): PasswordAuthentication {
                val account = senderMail
                val password = senderPassword
                return PasswordAuthentication(account ,password)
            }
        })
        session?.let {
            transport = it.transport.apply {
                connect("smtp.gmail.com" , senderMail , senderPassword)
            }
        }


    }

    fun send(){
        try {
            initSession()
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
            transport.addTransportListener(object : TransportListener {
                override fun messageDelivered(e: TransportEvent?) {
                    onSuccessCallBack("$subject send Succeed") }
                override fun messageNotDelivered(e: TransportEvent?) {
                    onFailCallBack("Error : messageNotDelivered ${e?.message}") }
                override fun messagePartiallyDelivered(e: TransportEvent?) {}
            })
            transport.sendMessage(message, arrayOf(InternetAddress(receiverMail)) )
            transport.close()

        } catch (e: Exception) {
            e.printStackTrace()
            onFailCallBack("Error : ${e.message}")
        }catch (e : AuthenticationFailedException){
            onFailCallBack("Error : ${e.message}")
        }catch (e : IllegalStateException){
            onFailCallBack("Error : ${e.message} ")

        }

    }

}