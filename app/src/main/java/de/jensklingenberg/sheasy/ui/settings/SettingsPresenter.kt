package de.jensklingenberg.sheasy.ui.settings

import android.content.Context
import android.view.View
import de.jensklingenberg.sheasy.App
import de.jensklingenberg.sheasy.BuildConfig
import de.jensklingenberg.sheasy.R
import de.jensklingenberg.sheasy.network.SheasyPrefDataSource
import de.jensklingenberg.sheasy.ui.common.BaseDataSourceItem
import de.jensklingenberg.sheasy.ui.common.GenericListHeaderSourceItem
import de.jensklingenberg.sheasy.ui.common.GenericListItem
import de.jensklingenberg.sheasy.ui.common.OnEntryClickListener
import de.jensklingenberg.sheasy.ui.common.toSourceItem
import de.jensklingenberg.sheasy.utils.NetworkUtils
import javax.inject.Inject

class SettingsPresenter(val view: SettingsContract.View) : SettingsContract.Presenter, OnEntryClickListener {



    @Inject
    lateinit var context: Context

    @Inject
    lateinit var sheasyPrefDataSource: SheasyPrefDataSource

    init {
        initializeDagger()
    }

    private fun initializeDagger() = App.appComponent.inject(this)


    override fun onCreate() {

        val list = listOf<BaseDataSourceItem<*>>(
            GenericListHeaderSourceItem(
                "Server"
            ),
            GenericListItem(
                "IP Address",
                NetworkUtils.getIP(context),
                R.drawable.ic_info_outline_grey_700_24dp
            ).toSourceItem(),

            GenericListItem(
                context.getString(R.string.http_port),
                sheasyPrefDataSource.httpPort.toString(),
                R.drawable.ic_info_outline_grey_700_24dp
            ).toSourceItem()

            ,
            GenericListItem(
                context.getString(R.string.websocket_port),
                sheasyPrefDataSource.webSocketPort.toString(),
                R.drawable.ic_info_outline_grey_700_24dp
            ).toSourceItem(this)

        )

    view.setData(list)
    }

    override fun onMoreButtonClicked(view: View, payload: Any) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemClicked(payload: Any) {
            view.onItemClicked(payload)
    }

}