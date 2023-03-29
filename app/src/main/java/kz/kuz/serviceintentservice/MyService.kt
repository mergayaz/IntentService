package kz.kuz.serviceintentservice

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.TimeUnit

// сервисы нужно добавлять в манифест, так как они реагируют на интенты, как и активности
// код запуска службы добавляется во фрагмент (или активность)
class MyService : IntentService("MyService") {
    @RequiresApi(api = Build.VERSION_CODES.O) // необходим для создания ChannelId
    override fun onHandleIntent(intent: Intent?) {
        if (!isNetworkAvailableAndConnected) {
            // проверяем доступность сети
            return
        }
        Log.i("MyService", "Received an intent: $intent")
        val i = MainActivity.newIntent(this)
        val pi = PendingIntent.getActivity(this, 0, i, 0)
        // создаём notification, всплывающее оповещение

        // создаём ChannelId
        val mNotificationManager = getSystemService(
                NOTIFICATION_SERVICE) as NotificationManager
        val id = "notification1" // id of the channel
        val name: CharSequence = "ChannelName" // user-visible name of the channel
        val description = "ChannelDescription" // user-visible description of the channel
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(id, name, importance)
        mChannel.description = description // configure the notification channel
        mChannel.enableLights(true)
        // sets the notification light color for notifications posted to this channel,
        // if the device supports this feature
        mChannel.lightColor = Color.RED
        mChannel.enableVibration(true)
        mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        mNotificationManager.createNotificationChannel(mChannel)
        val notification = NotificationCompat.Builder(
                this, "notification1")
                .setSmallIcon(R.drawable.ic_menu_report_image)
                .setContentTitle("contentTitle")
                .setContentText("ContentText")
                .setOngoing(true) // без этого флага оповещение не показывается на Samsung
                .setContentIntent(pi) // pi будет вызываться при нажатии на оповещении
                .setAutoCancel(true) // оповещение будет удаляться после нажатия
                .build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, notification)
        // идентификатор оповещения уникальный в пределах приложения
        // если будет отправлено оповещение с тем же идентификатором, оно заменит предыдущее
        // так реализуются индикаторы прогресса и другие динамические визуальные эффекты
    }

    // для того, чтобы использовать метод getActiveNetworkInfo() необходимо получить разрешение
    // в манифесте
    private val isNetworkAvailableAndConnected: Boolean
        private get() {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val isNetworkAvailable = cm.activeNetworkInfo != null
            // для того, чтобы использовать метод getActiveNetworkInfo() необходимо получить разрешение
            // в манифесте
            return isNetworkAvailable && cm.activeNetworkInfo!!.isConnected
        }

    companion object {
        // IntentService является субклассом Context и реагирует на интенты
        // это не единственный, но рекомендуемый способ для создания служб
        private val INTERVAL = TimeUnit.MINUTES.toMillis(1) // 60 сек
        private fun newIntent(context: Context?): Intent {
            // добавление метода newIntent лишь общепринятая схема
            // каждый компонент, который хочет запустить эту службу должен использовать newIntent()
            return Intent(context, MyService::class.java)
        }

        // установить время срабатывания можно матодами Handler.sendMessageDelayed() и
        // Handler.postDelayed(), но они существуют пока работает процесс, поэтому мы используем
        // AlarmManager
        // метод статичный, чтобы разные компоненты вызывали один и тот же метод
        fun setServiceAlarm(context: Context, isOn: Boolean) {
            val i = newIntent(context)
            val pi = PendingIntent.getService(context, 0, i, 0)
            // PendingIntent - это объект-маркер, который запоминает, как нужно вызывать интент
            // его можно передать другой стороне и она будет его вызывать от нашего имени
            // для каждого объекта PendingIntent можно зарегистрировать только один сигнал
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            if (isOn) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                        INTERVAL, pi)
                // поскольку выбран базис времени AlarmManager.ELAPSED_REALTIME, то время старта
                // определяется исходя из прошедшего времени с последней загрузки телефона:
                // SystemClock.elapsedRealtime() - рекомендуется в документации
                // если бы базисом был выбран AlarmManager.RTC, то время старта определялось бы
                // абсолютным временем UTC (например System.currentTimeMillis()), при этом
                // не учитывается локальное время
                // setRepeating - неточное повторение (срабатывает один раз в 15 минут для всех
                // сигналов)
                // если нужны точные сигналы, тогда нужно использовать методы AlarmManager.setWindow()
                // или AlarmManager.setExact(), для одноразового сигнала
                // сигналы AlarmManager.ELAPSED_REALTIME и AlarmManager.RTC не сработают на устройстве
                // в спящем режиме, для этого нужно использовать AlarmManager.ELAPSED_REALTIME_WAKEUP и
                // AlarmManager.RTC_WAKEUP, но эти режимы нужно стараться избегать
            } else {
                alarmManager.cancel(pi)
                pi.cancel()
                // после отмены сигнала также отменяется и PendingIntent
            }
        }

        fun isServiceAlarmOn(context: Context?): Boolean {
            val i = newIntent(context)
            val pi = PendingIntent.getService(context, 0, i,
                    PendingIntent.FLAG_NO_CREATE)
            // существование PendingIntent означает наличие сигнала
            return pi != null
        }
    }
}