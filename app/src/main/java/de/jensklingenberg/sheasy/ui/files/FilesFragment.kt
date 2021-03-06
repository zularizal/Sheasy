package de.jensklingenberg.sheasy.ui.files

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.appcompat.itemClicks
import com.jakewharton.rxbinding3.appcompat.queryTextChanges
import de.jensklingenberg.sheasy.App
import de.jensklingenberg.sheasy.R
import de.jensklingenberg.sheasy.model.FileResponse
import de.jensklingenberg.sheasy.network.HTTPServerService
import de.jensklingenberg.sheasy.ui.common.BaseAdapter
import de.jensklingenberg.sheasy.ui.common.BaseDataSourceItem
import de.jensklingenberg.sheasy.ui.common.BaseFragment
import de.jensklingenberg.sheasy.ui.common.NoOrEmptyContentItem
import de.jensklingenberg.sheasy.ui.common.addTo
import de.jensklingenberg.sheasy.utils.PermissionUtils
import de.jensklingenberg.sheasy.utils.UseCase.MessageUseCase
import de.jensklingenberg.sheasy.utils.extension.requireView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_files.*
import java.io.File
import javax.inject.Inject


class FilesFragment : BaseFragment(), FilesContract.View {


    @Inject
    lateinit var permissionUtils: PermissionUtils

    @Inject
    lateinit var messageUseCase: MessageUseCase


    private val REQUEST_CAMERA_PERMISSION = 1


    private val baseAdapter = BaseAdapter()
    lateinit var presenter: FilesContract.Presenter

    var toolbarMenu: Menu? = null

    /****************************************** Lifecycle methods  */
    init {
        initializeDagger()
    }

    fun initializeDagger() = App.appComponent.inject(this)

    override fun getLayoutId() = R.layout.fragment_files

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            permissionUtils.requestPermission(this, REQUEST_CAMERA_PERMISSION)
        }

        presenter = FilesPresenter(this)

        parseArguments()
        recyclerView?.apply {
            adapter = baseAdapter.apply {
                dataSource.emptyView = NoOrEmptyContentItem("No Files",R.drawable.ic_insert_drive_file_grey_700_24dp).toSourceItem()
            }
            recyclerView.layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
        updateFolderPathInfo(presenter.filePath)

        presenter.loadFiles()
        folderUpIv.setOnClickListener { presenter.folderUp() }
    }

    private fun parseArguments() {
        arguments?.let {
            val fromBundle = FilesFragmentArgs.fromBundle(it)
            var filepath = fromBundle.filePath
            if (filepath.isNotBlank()) {

                filepath = filepath.replace("file://", "")
                if (filepath.contains(".")) {
                    filepath = filepath.replaceAfterLast("/", "")
                }
                presenter.filePath = filepath

            }

        }
    }


    /****************************************** Listener methods  */


    override fun showPopup(item: FileResponse?, view: View) {
        item?.let { appInfo ->
            val popup = PopupMenu(requireContext(), view)
                .apply {
                    menuInflater
                        .inflate(R.menu.files_actions, menu)
                }
                .also {
                    it.itemClicks()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { menuItem ->
                            when (menuItem.itemId) {
                                R.id.menu_share -> {
                                    presenter.share(File(item.path))
                                }
                                R.id.menu_share_to_server -> {
                                    presenter.hostFolder(item)
                                }
                            }
                        }.subscribe()
                }
            popup.show()


        }


    }

    override fun setData(list: List<BaseDataSourceItem<*>>) {
        baseAdapter.dataSource.setItems(list)
        baseAdapter.notifyDataSetChanged()
    }


    /****************************************** Class methods  */

    override fun updateFolderPathInfo(path: String) {
        folderPathLayout?.apply {
            title.text = path
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.fragment_apps_options_menu, menu)
        toolbarMenu = menu
        initSearchView(menu)

        val server = menu?.findItem(R.id.menu_server)

            HTTPServerService.serverRunning
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribeBy(onNext = { running ->
                    if (running) {
                        server?.setIcon(R.drawable.ic_router_green_700_24dp)
                    } else {
                        server?.setIcon(R.drawable.ic_router_black_24dp)
                    }

                }).addTo(compositeDisposable)



    }

    private fun initSearchView(menu: Menu?) {
        val search = menu?.findItem(R.id.search)?.actionView as SearchView
        search.queryTextChanges()
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { presenter.searchFile(it.toString()) }
            .subscribe()

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                getBaseActivity().mainActivityDrawer.toggleDrawer()
            }
            R.id.menu_server -> {
                when (item.isChecked) {
                    true -> {
                        requireContext().stopService(HTTPServerService.getIntent(requireContext()))
                        item.isChecked = false
                    }
                    false -> {
                        requireContext().startService(HTTPServerService.getIntent(requireContext()))
                        item.isChecked = true

                    }
                }
            }
        }

        return true
    }


    override fun showError(it: Throwable) {
        messageUseCase.show(requireView(), it.message.toString())
    }


}


