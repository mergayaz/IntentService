package kz.kuz.serviceintentservice

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {
    // методы фрагмента должны быть открытыми
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.setTitle(R.string.toolbar_title)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    // кнопка меню для запуска аларма
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_toggle -> {
                val shouldStartAlarm = !MyService.isServiceAlarmOn(activity)
                MyService.setServiceAlarm(requireActivity(), shouldStartAlarm)
                activity?.invalidateOptionsMenu() // обновление меню
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_main, menu)

        // меняем надпись в меню в зависимости от переключателя
        val toggleItem = menu.findItem(R.id.menu_item_toggle)
        if (MyService.isServiceAlarmOn(activity)) {
            toggleItem.title = "Stop Alarm"
        } else {
            toggleItem.title = "Start Alarm"
        }
    }
}