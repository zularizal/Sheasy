package de.jensklingenberg.sheasy.web.ui.files

import components.materialui.Button
import components.materialui.CircularProgress
import components.materialui.FormControl
import components.materialui.InputLabel
import components.materialui.Menu
import components.materialui.MenuItem
import components.materialui.icons.AndroidIcon
import de.jensklingenberg.sheasy.model.Error
import de.jensklingenberg.sheasy.model.FileResponse
import de.jensklingenberg.sheasy.model.Status
import de.jensklingenberg.sheasy.web.components.materialui.Input
import de.jensklingenberg.sheasy.web.components.materialui.icons.ArrowBackIcon
import de.jensklingenberg.sheasy.web.components.materialui.icons.ArrowBackIconImport
import de.jensklingenberg.sheasy.web.components.materialui.icons.CloudUploadIcon
import de.jensklingenberg.sheasy.web.data.FileDataSource
import de.jensklingenberg.sheasy.web.data.NetworkPreferences
import de.jensklingenberg.sheasy.web.data.repository.FileRepository
import de.jensklingenberg.sheasy.web.model.SourceItem
import de.jensklingenberg.sheasy.web.model.render
import de.jensklingenberg.sheasy.web.network.ReactHttpClient
import de.jensklingenberg.sheasy.web.ui.common.BaseComponent
import de.jensklingenberg.sheasy.web.ui.common.extension.selectedFile
import de.jensklingenberg.sheasy.web.ui.common.styleProps
import de.jensklingenberg.sheasy.web.ui.common.toolbar
import de.jensklingenberg.sheasy.web.usecase.MessageUseCase
import kotlinx.html.DIV
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import react.RBuilder
import react.RProps
import react.RState
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.img
import react.setState


interface FileViewState : RState {
    var snackbarMessage: String
    var status: Status
    var openMenu: Boolean
    var anchor: EventTarget?
    var selectedFile: FileResponse?
    var item: List<SourceItem>


}


class FileView : BaseComponent<RProps, FileViewState>(), FilesContract.View {


    val appsDataSource: FileDataSource = FileRepository(ReactHttpClient(NetworkPreferences()))
    var presenter = FilesPresenter(this, appsDataSource)
    val messageUseCase = MessageUseCase()

    /****************************************** React Lifecycle methods  */

    override fun FileViewState.init() {

        status = Status.LOADING
        openMenu = false
        anchor = null
        item = emptyList()

    }

    override fun componentDidMount() {
        presenter.getShared()

    }

    override fun RBuilder.render() {

      //  toolbar()

        div {
            setupBackButton(this)
            setupUploadButton(this)
        }
        setupSearchBar(this)
        div {
            Input {
                attrs {
                    styleProps(width = "100%")

                    type = InputType.search.realValue
                    value = presenter.folderPath
                }
            }
        }

        state.item.render(this)

        div {
            CircularProgress {
                attrs {
                    styleProps(display = progressVisibiity())
                }
            }
        }
        messageUseCase.showErrorSnackbar(this, state.snackbarMessage, errorsnackbarVisibility())

        setupContextMenu(this)

    }

    private fun setupContextMenu(rBuilder: RBuilder) {
        rBuilder.run {
            div {
                Menu {
                    attrs {
                        id = "simple-menu"
                        open = state.openMenu
                        anchorEl = state.anchor
                        onClose = {
                            run {
                                setState {
                                    openMenu = false
                                }
                            }
                        }


                    }
                    MenuItem {
                        +"Download"
                        attrs {
                            styleProps(textAlign = "right")
                            onClick = {
                                presenter.getFile(state.selectedFile)
                            }
                        }

                    }
                    MenuItem {
                        +"Profile"
                        attrs {
                            styleProps(textAlign = "right")
                            onClick = { event: Event -> handleMenuItemClick(event) }
                        }
                    }
                }
            }

        }
    }


    private fun setupUploadButton(rdomBuilder: RDOMBuilder<DIV>) {
        rdomBuilder.run {
            Input {
                attrs {
                    id = "outlined-button-file"
                    type = "file"
                    styleProps(display = "none")
                    onChange = { event -> handleFile(event) }

                }
            }
            InputLabel {
                attrs {
                    htmlFor = "outlined-button-file"

                }
                Button {
                    CloudUploadIcon{}
                    +"Upload"
                    attrs {
                        component = "span"
                        variant = "outlined"

                    }
                }
            }
        }

    }

    fun handleFile(event: Event) {
        event.target.selectedFile?.let {
            console.log(it)
            presenter.uploadFile(it)
        }

    }

    private fun setupBackButton(rdomBuilder: RDOMBuilder<DIV>) {
        rdomBuilder.run {
            Button {
                ArrowBackIcon{}

                +"Back"
                attrs {
                    variant = "outlined"
                    component = "span"

                    onClick = {
                        presenter.navigateUp()
                    }
                }

            }
        }
    }

    private fun setupSearchBar(rBuilder: RBuilder) {
        rBuilder.run {
            FormControl {
                Input {
                    attrs {
                        type = InputType.search.realValue
                        name = "search"
                        placeholder = "SEARCH HERE"
                        onChange = {
                            presenter.onSearch((it.target as HTMLInputElement).value)
                        }

                        fullWidth=true
                    }
                }
                attrs {
                    this.fullWidth=true
                }
            }
        }
    }


    /****************************************** Presenter methods  */



    override fun showError(error: Error) {
        setState {
            status = Status.ERROR
            snackbarMessage = error.message
        }
    }

    override fun showSnackBar(message: String) {
        setState {
            status = Status.ERROR
            snackbarMessage = message
        }

    }

    override fun setData(items: List<SourceItem>) {
        setState {
            status = Status.SUCCESS

            this.item = items
        }
    }

    /****************************************** Class methods  */

    fun errorsnackbarVisibility(): Boolean = state.status == Status.ERROR

    fun progressVisibiity(): String {
        return when (state.status) {
            Status.LOADING -> {
                ""
            }
            Status.SUCCESS, Status.ERROR -> {
                "none"
            }
        }
    }

   override fun handleClickListItem(event: Event, fileResponse: FileResponse) {
        val currentTarget = event.currentTarget
        setState {
            openMenu = !openMenu
            anchor = currentTarget
            selectedFile = fileResponse
        }

    }

    fun handleMenuItemClick(event: Event) {


        setState {
            openMenu = false
            anchor = null
        }
    }

}


