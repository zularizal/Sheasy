package de.jensklingenberg.sheasy.ui.settings

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import de.jensklingenberg.sheasy.App
import de.jensklingenberg.sheasy.R
import de.jensklingenberg.sheasy.ui.common.BaseAdapter
import de.jensklingenberg.sheasy.ui.common.BaseDataSourceItem
import de.jensklingenberg.sheasy.ui.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_apps.*


class SettingsFragment : BaseFragment(), SettingsContract.View {

    private val settingsAdapter = BaseAdapter()



    lateinit var presenter: SettingsContract.Presenter


    init {
        initializeDagger()
    }

    private fun initializeDagger() = App.appComponent.inject(this)

    override fun getLayoutId(): Int = R.layout.fragment_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView?.apply {
            adapter = settingsAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)
        }

        presenter = SettingsPresenter(this)
        presenter.onCreate()
    }

    override fun onItemClicked(payload: Any) {


    }


    override fun setData(items: List<BaseDataSourceItem<*>>) {
        settingsAdapter.dataSource.setItems(items)

    }


}
