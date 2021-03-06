package de.jensklingenberg.sheasy.ui.apps

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.appcompat.itemClicks
import com.jakewharton.rxbinding3.appcompat.queryTextChanges
import de.jensklingenberg.sheasy.App
import de.jensklingenberg.sheasy.R
import de.jensklingenberg.sheasy.model.AppInfo
import de.jensklingenberg.sheasy.ui.common.BaseAdapter
import de.jensklingenberg.sheasy.ui.common.BaseDataSourceItem
import de.jensklingenberg.sheasy.ui.common.BaseFragment
import de.jensklingenberg.sheasy.ui.common.addTo
import de.jensklingenberg.sheasy.utils.UseCase.MessageUseCase
import de.jensklingenberg.sheasy.utils.extension.requireView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_apps.*
import javax.inject.Inject


class AppsFragment : BaseFragment(), AppsContract.View {


    private val baseAdapter = BaseAdapter()

    lateinit var presenter: AppsContract.Presenter


    @Inject
    lateinit var messageUseCase: MessageUseCase

    /****************************************** Lifecycle methods  */

    init {
        initializeDagger()
    }

    private fun initializeDagger() = App.appComponent.inject(this)

    override fun getLayoutId(): Int = R.layout.fragment_apps

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        recyclerView?.apply {
            adapter = baseAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        presenter = AppsPresenter(this)
        presenter.onCreate()


    }

    override fun setData(list: List<BaseDataSourceItem<*>>) {
        baseAdapter.dataSource.setItems(list)
        baseAdapter.notifyDataSetChanged()

    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.fragment_apps_options_menu, menu)
        initSearchView(menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    /****************************************** Class methods  */


    private fun initSearchView(menu: Menu?) {
        val search = menu?.findItem(R.id.search)?.actionView as SearchView
        search.queryTextChanges()
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { presenter.searchApp(it.toString()) }
            .subscribe().addTo(compositeDisposable)

    }


    /****************************************** Listener methods  */


    override fun showError(it: Throwable?) {
        messageUseCase.show(requireView(), it?.message ?: "")

    }


    override fun onMoreButtonClicked(view: View, payload: Any) {
        val appInfo = payload as? AppInfo
        appInfo?.let {
            val popup = PopupMenu(requireActivity(), view)
                .apply {
                    menuInflater
                        .inflate(R.menu.apps_actions, menu)
                }
                .also {
                    it.itemClicks()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { menuItem ->
                            when (menuItem.itemId) {
                                R.id.menu_share -> {
                                    presenter.shareApp(appInfo)
                                }
                                R.id.menu_extract -> {
                                    if (presenter.extractApp(appInfo)) {
                                        messageUseCase.show(requireView(), "Succes")
                                    }
                                }
                            }
                        }.subscribe()
                }
            popup.show()
        }

    }


}

